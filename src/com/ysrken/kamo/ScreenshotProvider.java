package com.ysrken.kamo;

import javax.imageio.ImageIO;
import java.io.Console;
import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class ScreenshotProvider {
    /**
     * 撮影用のRobot
     */
    private static Robot robot = null;
    /**
     * 撮影範囲
     */
    private static Rectangle rect = null;

    /**
     * 初期化
     */
    public static void initialize(){}
    /**
     * 自動座標取得を試みる(成功したらtrue)
     */
    public static boolean trySearchGamePosition(){
        try {
            // グラフィックデバイスの情報を取得する
            GraphicsDevice[] all_gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
            for (GraphicsDevice gd : all_gd) {
                // 各グラフィックデバイスにおけるグラフィックス特性を取得する
                GraphicsConfiguration[] all_gc = gd.getConfigurations();
                // 各グラフィックス特性に従い、その座標を取得してスクショを撮る
                Robot robot = new Robot(gd);
                for (GraphicsConfiguration gc : all_gc) {
                    BufferedImage imageData = robot.createScreenCapture(gc.getBounds());
                    // 撮ったスクショに対して、ゲーム画面を検索する
                    List<Rectangle> rectList = searchGamePosition(imageData);
                    // 検索にヒットした場合、その座標を取得して結果を返す
                    if(rectList.size() > 0){
                        ScreenshotProvider.robot = robot;
                        ScreenshotProvider.rect = rectList.get(0);
                        return true;
                    }
                }
            }
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
     * ・領域のピクセル比率は、800:480＝5:3である
     * ただし、真面目にやると計算コストが重いので、ステップサーチによって計算量を削減している
     * @param image 画像データ
     * @return ゲーム画面の候補の一覧
     */
    public static List<Rectangle> searchGamePosition(BufferedImage image){
        final int minGameWidth = 800;
        final int stepCount = 4;
        final int stepWidth = minGameWidth / (stepCount + 1);
        final int minGameHeight = 480;
        final int stepHeight = minGameHeight / (stepCount + 1);
        final int maxGameWidth = 1800;
        final int maxGameHeight = 1080;
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
        final List<Integer> yList = new ArrayList<>();
        final List<Integer> aList = new ArrayList<>();
        final List<Integer> baseColorList = new ArrayList<>();
        final AtomicInteger yaCount = new AtomicInteger(0);
        IntStream.range(0, image.getHeight() - 4).forEach(y -> {
            IntStream.range(0, (image.getWidth() / stepWidth) - stepCount + 1)
                .map(i -> i * stepWidth)
                .filter(a -> {
                    int baseColor = image.getRGB(a, y);
                    if(IntStream.range(1, stepCount)
                        .map(j -> a + j * stepWidth)
                        .anyMatch(a2 -> image.getRGB(a2, y) != baseColor)) {
                        return false;
                    }
                    if(IntStream.range(0, stepCount)
                        .map(j -> a + j * stepWidth)
                        .allMatch(b -> image.getRGB(b, y + 1) == baseColor))
                        return false;
                    return true;
                    }).forEach(a -> {
                    yList.add(y);
                    aList.add(a);
                    baseColorList.add(image.getRGB(a, y));
                    yaCount.incrementAndGet();
            });
        });
        /**
         * 上辺の候補の左側を走査し、左辺となりうる辺を持ちうるかを調査する
         * ・上記のA1ピクセルより左側stepWidthピクセルの間に、「左上座標」の候補があると考えられる
         * ・ゆえに順番に1ピクセルづつ見ていき、縦方向の辺を持ちうるかをチェックする
         * ・候補になりうるかの判定には、上辺の検索と同じくステップサーチを用いる
         */
        final var yList2 = new ArrayList<Integer>();
        final var xList = new ArrayList<Integer>();
        final var baseColorList2 = new ArrayList<Integer>();
        final AtomicInteger xyCount = new AtomicInteger(0);
        IntStream.range(0, yaCount.get()).forEach(i -> {
            final var a = aList.get(i);
            final var y = yList.get(i);
            final var baseColor = baseColorList.get(i);
            IntStream.range(0, stepWidth).map(j -> a - j)
                .filter(x -> x >= 0)
                .takeWhile(x -> image.getRGB(x, y) == baseColor).forEach(x -> {
                    if(IntStream.range(1, stepCount).map(k -> y + k * stepHeight)
                        .filter(y2 -> y2 < image.getHeight())
                        .anyMatch(y2 -> image.getRGB(x, y2) != baseColor)) {
                        return;
                    }
                    if(IntStream.range(1, stepCount).map(k -> y + k * stepHeight)
                        .filter(y2 -> y2 < image.getHeight())
                        .allMatch(y2 -> image.getRGB(x + 1, y2) == baseColor)) {
                        return;
                    }
                    xList.add(x);
                    yList2.add(y);
                    baseColorList2.add(baseColor);
                    xyCount.incrementAndGet();
                });
        });
        /**
         * 上辺・左辺から決まる各候補について、Rectangleとしての条件を満たせるかをチェックする
         * ・左上座標候補からもう一度ステップサーチしていって、最小縦幅・横幅は大丈夫そうか調べる
         * ・下辺候補・右辺候補をステップサーチで調べる
         * ・最後は1ピクセルづつ舐めるように検索して正当性を確かめる
         */
        final List<Rectangle> rectList = new ArrayList<>();
        IntStream.range(0, xyCount.get()).forEach(i -> {
            final var x = xList.get(i);
            final var y = yList2.get(i);
            final var baseColor = baseColorList2.get(i);
            IntStream.range(x + minGameWidth + 1, Math.min(x + maxGameWidth + 2, image.getWidth()))
                .takeWhile(x2 -> image.getRGB(x2, y) == baseColor).forEach(x2 -> {
                    // 左下候補のy座標
                    final var y2 = y + (x2 - (x + 1)) * minGameHeight / minGameWidth + 1;
                    if(y2 >= image.getHeight())
                        return;
                    // 左下候補をチェック
                    if(image.getRGB(x2, y2) != baseColor)
                        return;
                    // ステップサーチで下辺と右辺をチェック
                    if(IntStream.range(0, stepCount)
                        .map(j -> x + j * stepWidth)
                        .anyMatch(x3 -> image.getRGB(x3, y2) != baseColor)) {
                        return;
                    }
                    if(IntStream.range(0, stepCount)
                            .map(j -> x + j * stepWidth)
                            .allMatch(x3 -> image.getRGB(x3, y2 - 1) == baseColor)) {
                        return;
                    }
                    if(IntStream.range(1, stepCount).map(j -> y + j * stepHeight)
                            .anyMatch(y3 -> image.getRGB(x2, y3) != baseColor)) {
                        return;
                    }
                    if(IntStream.range(1, stepCount).map(j -> y + j * stepHeight)
                            .allMatch(y3 -> image.getRGB(x2 - 1, y3) == baseColor)) {
                        return;
                    }
                    // 最終チェック
                    if(IntStream.range(x, x2).anyMatch(x3 -> image.getRGB(x3, y) != baseColor)
                        || IntStream.range(x, x2).anyMatch(x3 -> image.getRGB(x3, y2) != baseColor)
                        || IntStream.range(y, y2).anyMatch(y3 -> image.getRGB(x, y3) != baseColor)
                        || IntStream.range(y, y2).anyMatch(y3 -> image.getRGB(x2, y3) != baseColor))
                        return;
                    // リストに追加
                    rectList.add(new Rectangle(x + 1, y + 1, x2 - x - 1, y2 - y - 1));
            });
        });
        return rectList;
    }
    /**
     * 取得した座標を返す
     */
    public static Rectangle getPosition(){
        return rect;
    }
    /**
     * スクリーンショットを取得できる状態ならtrue
     */
    public static boolean canGetScreenshot(){
        return (rect != null);
    }

    /**
     * スクリーンショットを取得する
     */
    public static BufferedImage getScreenshot(){
        return robot.createScreenCapture(rect);
    }
}
