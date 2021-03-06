package com.ysrken.kamo.service;

import one.util.streamex.IntStreamEx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class ScreenshotService {
    /**
     * 撮影用のRobot
     */
    private Robot robot = null;

    /**
     * 撮影範囲
     */
    private Rectangle rect = null;
    
    /**
     * 位置ズレ確認用のrect
     */
    private Rectangle rectForCheck = null;
    
    /**
     * 枠線の色
     */
    private int frameColor = 0;

    /**
     * 画素情報を保持するための構造体
     * @author ysrken
     */
    private class ColorPoint{
        public int X;
        public int Y;
        public int Color;
    }
	
    /**
     * 各種サービス
     */
	@Autowired
    private LoggerService logger;
	@Autowired
	UtilityService utility;
    @Autowired
    private SettingService setting;
    @Autowired
    private  SprcialScreenShotService sprcialScreenShot;
	
    /**
     * コンストラクタ
     * @throws IOException 
     */
    public ScreenshotService() throws IOException {
    	System.out.println("DEBUG MainApp - ScreenshotService#ScreenshotService");
    	// picフォルダを作成する
        final File folder = new File("pic");
        if(!folder.exists()){
            if(!folder.mkdir()){
                throw new IOException();
            }
        }
    }
    
    /**
     * 自動座標取得を試みる(成功したらtrue)
     */
    public boolean trySearchGamePosition(){
        try {
            // グラフィックデバイスの情報を取得する
            final GraphicsDevice[] allGD = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
            for (GraphicsDevice gd : allGD) {
                // 各グラフィックデバイスにおけるグラフィックス特性を取得する
                final GraphicsConfiguration[] allGC = gd.getConfigurations();
                
                // 各グラフィックス特性に従い、その座標を取得してスクショを撮る
                final Robot robot = new Robot(gd);
                for (GraphicsConfiguration gc : allGC) {
                    final Rectangle gcBounds = gc.getBounds();
                    final BufferedImage imageData = robot.createScreenCapture(gcBounds);
                    
                    // 撮ったスクショに対して、ゲーム画面を検索する
                    final List<Rectangle> rectList = searchGamePosition(imageData);

                    // 検索にヒットした場合、その座標を取得して結果を返す
                    if(rectList.size() > 0){
                        this.robot = robot;
                        // マルチディスプレイ対策に左上座標を編集している
                        final Rectangle selectRect = rectList.get(0);
                        final int x = gcBounds.x + selectRect.x;
                        final int y = gcBounds.y + selectRect.y;
                        final int width = selectRect.width;
                        final int height = selectRect.height;
                        this.rect = new Rectangle(x, y, width, height);
                        this.rectForCheck = new Rectangle(x - 1, y - 1, width + 2, height + 2);

                        // 枠線の色も記憶しておく
                        this.frameColor = imageData.getRGB((int)selectRect.getX() - 1, (int)selectRect.getY() - 1);

                        // 特殊なスクリーンショットルーチンが使えるかを判定しておく
                        if(setting.<Boolean>getSetting("SpecialGetPosFlg") && utility.isWindows()){
                            if(sprcialScreenShot.trySearchGamePosition(rect, rectForCheck, frameColor)){
                                logger.debug("※特殊な座標取得手法が有効になっています");
                            }
                        }
                        return true;
                    }
                }
            }
            rect = null;
            return false;
        } catch (AWTException e) {
            e.printStackTrace();
            rect = null;
            return false;
        }
    }
    
    /**
     * 画像データから、ゲーム画面の候補を抽出する。
     * ここで言う「候補」とは、以下の条件を満たすものである。
     * ・長方形の領域である
     * ・領域の1ピクセル外側は、全て同じ色(A)である
     * ・↑の1ピクセル内側に、色Aと異なる色が1ピクセル以上存在する
     * ・領域のピクセル比率は、1200:720＝5:3である
     * ただし、真面目にやると計算コストが重いので、ステップサーチによって計算量を削減している
     * @param image 画像データ
     * @return ゲーム画面の候補の一覧
     */
    public List<Rectangle> searchGamePosition(BufferedImage image){
        final int minGameWidth = 600;
        final int stepCount = 4;
        final int stepWidth = minGameWidth / (stepCount + 1);
        final int minGameHeight = 360;
        final int stepHeight = minGameHeight / (stepCount + 1);
        final int maxGameWidth = 2400;
        
        /**
         * 第一段階：上辺の候補を検索する
         * 1. stepWidthピクセルごとに画素を読み取る(Y=yとY=y+1)
         * 2. 以下の2配列の中で、「A1～A{stepCount}は全部同じ色」かつ
         *    「B1～B{stepCount}のどれかはAxと違う色」である箇所を見つける
         *   Y=y  [..., A1, A2, .., A{stepCount}, ...]
         *   Y=y+1[..., B1, B2, .., B{stepCount}, ...]
         * ここでstepWidthを下記計算式にしているのは、Y=yにおいて確実に
         * 「位置A1～A{stepCount}」の区間の長さ⊆ゲーム画面の最小横幅(minGameWidth)
         * とするためである。「⊆」が満たされないと取りこぼしが発生しかねない。
         * また、「B1～B{stepCount}のどれかはAxと違う色」でないと、関数定義における
         * 「↑の1ピクセル内側に、色Aと異なる色が1ピクセル以上存在する」を満たせない
         * 可能性が生じる(ステップサーチなので「可能性」で弾いている)。
         */
        final Stream<ColorPoint> step1Stream =
        // yは探索範囲の上辺の候補のy座標を表す
        // ここでboxedしているのは、その次のflatMapで参照型のStreamを返したいがため
        IntStream.range(0, image.getHeight() - 4).boxed().flatMap(y ->
            // range→mapにより、ステップサーチするx座標を生成している
            IntStream.range(0, (image.getWidth() / stepWidth) - stepCount + 1).map(i -> i * stepWidth)
                // フィルタ処理してからStream<ColorPoint>に変換する
                // (これが前述のflarMapで1つに纏められる)
                .filter(a -> {
                    // 枠線(候補)の色を記憶する
                    final int baseColor = image.getRGB(a, y);
                    
                    // 枠線(候補)に、枠線の色と異なる色があった場合は弾く
                    if(IntStream.range(1, stepCount)
                        .map(j -> a + j * stepWidth)
                        .anyMatch(a2 -> image.getRGB(a2, y) != baseColor)) {
                        return false;
                    }
                    
                    // 枠線(候補)の1ピクセル内側が、全て枠線の色と同色だった場合は弾く
                    if(IntStream.range(0, stepCount)
                        .map(j -> a + j * stepWidth)
                        .allMatch(b -> image.getRGB(b, y + 1) == baseColor)) {
                        return false;
                    }
                    return true;
                }).mapToObj(a -> {
                    // Javaに構造体はないのでクラスを生成するコードになってしまう。
                    // また、ラムダ式内で宣言したローカル変数baseColorは
                    // 次のラムダ式に引き継げないので、再度getRGBしている
                    return new ColorPoint(){{
                        X = a;
                        Y = y;
                        Color = image.getRGB(a, y);
                    }};
                })
        );

        /**
         * 上辺の候補の左側を走査し、左辺となりうる辺を持ちうるかを調査する
         * ・上記のA1ピクセルより左側stepWidthピクセルの間に、「左上座標」の候補があると考えられる
         * ・ゆえに順番に1ピクセルづつ見ていき、縦方向の辺を持ちうるかをチェックする
         * ・候補になりうるかの判定には、上辺の検索と同じくステップサーチを用いる
         */
        final Stream<ColorPoint> step2Stream =
            // Streamから終端操作を行わずにStreamに変換するので合法
            step1Stream.flatMap(point ->
                // (a,y)から左方向に0～stepWidth-1ピクセル戻るが、画像の範囲からはみ出さない
                // ということを表現するため、こうした中間操作になっている
                IntStreamEx.range(0, stepWidth).map(j -> point.X - j).filter(x -> x >= 0)
                    // (a,y)の画素色と共通の範囲までしか左に戻らない、ということを表現するため、
                    // Java9のtakeWhileを使用した
                    .takeWhile(x -> image.getRGB(x, point.Y) == point.Color)
                    
                    // 左辺が不適当だと判定した場合は弾く
                    .filter(x -> {
                        // 枠線(候補)に、枠線の色と異なる色があった場合は弾く
                        if(IntStream.range(1, stepCount).map(k -> point.Y + k * stepHeight)
                            .filter(y2 -> y2 < image.getHeight())
                            .anyMatch(y2 -> image.getRGB(x, y2) != point.Color)) {
                            return false;
                        }
                        
                        // 枠線(候補)の1ピクセル内側が、全て枠線の色と同色だった場合は弾く
                        if(IntStream.range(1, stepCount).map(k -> point.Y + k * stepHeight)
                                .filter(y2 -> y2 < image.getHeight())
                                .allMatch(y2 -> image.getRGB(x + 1, y2) == point.Color)) {
                            return false;
                        }
                        return true;
                    })
                    .mapToObj(x -> {
                        // 再び座標・画素値に変換する
                        return new ColorPoint(){{
                            X = x;
                            Y = point.Y;
                            Color = point.Color;
                        }};
                    })
        );

        /**
         * 上辺・左辺から決まる各候補について、Rectangleとしての条件を満たせるかをチェックする
         * ・左上座標候補からもう一度ステップサーチしていって、最小縦幅・横幅は大丈夫そうか調べる
         * ・下辺候補・右辺候補をステップサーチで調べる
         * ・最後は1ピクセルづつ舐めるように検索して正当性を確かめる
         */
        final List<ColorPoint> step2List = step2Stream.collect(Collectors.toList());
        final List<Rectangle> step3List = new ArrayList<>();
        for(ColorPoint point : step2List){
            // つまり、「左上座標から右にminGameWidth+1ピクセル進んだ位置」から
            // 「min(左上座標から右にmaxGameWidth + 1ピクセル進んだ位置, 画像の右端)」まで。
            // forは終端(第二引数)を含まないので、+1ではなく+2表記なことに注意
            for(int x2 = point.X + minGameWidth + 1; x2 < Math.min(point.X + maxGameWidth + 2, image.getWidth()); ++x2){
                //　ここのifは、右上座標を右に1つづつ見ていく際に、右上座標が枠線の色と
                // 異なってしまった場合、それ以上探索するのは無駄ということから来ている
                if (image.getRGB(x2, point.Y) != point.Color){
                    break;
                }
                for(int k = -1; k <= 1; ++k){
                    // 左下候補のy座標
                    final int y2 = point.Y + (x2 - (point.X + 1)) * minGameHeight / minGameWidth + 1 + k;
                    if(y2 >= image.getHeight())
                        continue;

                    // 左下候補をチェック
                    if(image.getRGB(x2, y2) != point.Color)
                        continue;

                    // ステップサーチで下辺をチェック
                    if(IntStream.range(0, stepCount).map(j -> point.X + j * stepWidth)
                            .anyMatch(x3 -> image.getRGB(x3, y2) != point.Color)) {
                        continue;
                    }
                    if(IntStream.range(0, stepCount).map(j -> point.X + j * stepWidth)
                            .allMatch(x3 -> image.getRGB(x3, y2 - 1) == point.Color)) {
                        continue;
                    }

                    // ステップサーチで右辺をチェック
                    final int x2_ = x2;
                    if(IntStream.range(1, stepCount).map(j -> point.Y + j * stepHeight)
                            .anyMatch(y3 -> image.getRGB(x2_, y3) != point.Color)) {
                        continue;
                    }
                    if(IntStream.range(1, stepCount).map(j -> point.Y + j * stepHeight)
                            .allMatch(y3 -> image.getRGB(x2_ - 1, y3) == point.Color)) {
                        continue;
                    }

                    // 最終チェック
                    if(IntStream.range(point.X, x2).anyMatch(x3 -> image.getRGB(x3, point.Y) != point.Color)
                            || IntStream.range(point.X, x2).anyMatch(x3 -> image.getRGB(x3, y2) != point.Color)
                            || IntStream.range(point.Y, y2).anyMatch(y3 -> image.getRGB(point.X, y3) != point.Color)
                            || IntStream.range(point.Y, y2).anyMatch(y3 -> image.getRGB(x2_, y3) != point.Color))
                        continue;
                    step3List.add(new Rectangle(point.X + 1, point.Y + 1, x2 - point.X - 1, y2 - point.Y - 1));
                }
            }
        }
        return step3List;
    }
    
    /**
     * 取得した座標を返す
     */
    public Rectangle getPosition(){
        return rect;
    }
    
    /**
     * スクリーンショットを取得できる状態ならtrue
     */
    public boolean canGetScreenshot(){
        return (rect != null);
    }
    
    /**
     * スクリーンショットを取得する
     */
    public BufferedImage getScreenshot(){
        if(setting.<Boolean>getSetting("SpecialGetPosFlg") && utility.isWindows() && sprcialScreenShot.canGetScreenshot()){
            return sprcialScreenShot.getScreenshot();
        /*}else if(utility.isWindows()){
        }else if(setting.<Boolean>getSetting("SpecialGetPosFlg")){
            return sprcialScreenShot.getBasicScreenshot(rect);
        */} else {
            return robot.createScreenCapture(rect);
        }
    }
    
    /**
     * ゲーム画面の位置が動いたならtrue
     * @return
     */
    public boolean isMovedPosition(){
        // スクショを取得し、枠線の色が正しいかを確認する
        BufferedImage sampleImage = null;
        if(setting.<Boolean>getSetting("SpecialGetPosFlg") && utility.isWindows() && sprcialScreenShot.canGetScreenshot()){
            sampleImage = sprcialScreenShot.getScreenshotForCheck();
        }else{
            sampleImage = robot.createScreenCapture(rectForCheck);
        }
        final int x2 = sampleImage.getWidth() - 1;
        final int y2 = sampleImage.getHeight() - 1;
        final BufferedImage sampleImage_ = sampleImage;
        return (IntStream.range(0, sampleImage.getWidth()).anyMatch(x -> sampleImage_.getRGB(x, 0) != frameColor)
        || IntStream.range(0, sampleImage.getWidth()).anyMatch(x -> sampleImage_.getRGB(x, y2) != frameColor)
        || IntStream.range(0, sampleImage.getHeight()).anyMatch(y -> sampleImage_.getRGB(0, y) != frameColor)
        || IntStream.range(0, sampleImage.getHeight()).anyMatch(y -> sampleImage_.getRGB(x2, y) != frameColor));
    }
}
