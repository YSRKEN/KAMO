package com.ysrken.kamo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.awt.Image.SCALE_SMOOTH;

/**
 * 画像がそのシーンたりうる証拠
 */
interface SceneEvidence{
    /**
     * 画像がその証拠とマッチするかを判定する
     * @param image 入力画像
     * @return マッチすればtrue
     */
    public boolean isMatchImage(BufferedImage image);
}

/**
 * 画像がそのシーンたりうる証拠(DifferenceHash版)
 */
class SceneEvidenceDH implements SceneEvidence{
    private double xPer, yPer, wPer, hPer;
    private long hash;
    /**
     * ％表記の割合(A)と100％時のピクセル値(B)から、ピクセルを出力する
     * ただし出力値は、[0, B - 1]にクロップされる
     * @param per 割合
     * @param pixel ピクセル
     * @return 割合値のピクセル
     */
    private static long perToPixel(double per, int pixel){
        final var rawPixel = per * pixel / 100;
        final var roundPixel = Math.round(rawPixel);
        return Math.min(Math.max(roundPixel, 0), pixel - 1);
    }
    /**
     * 画像の一部分におけるDifferenceHashを取得する(rectで指定する範囲は％単位)
     * @param image 画像
     * @return ハッシュ値
     */
    private long calcDifferenceHash(BufferedImage image) {
        // 画像の選択範囲(％)を選択範囲(ピクセル)に変換
        final var rectX = (int)perToPixel(xPer, image.getWidth());
        final var rectY = (int)perToPixel(yPer, image.getHeight());
        final var rectW = (int)perToPixel(wPer, image.getWidth());
        final var rectH = (int)perToPixel(hPer, image.getHeight());
        // 元画像を切り抜き、9x8ピクセルにリサイズ
        final var tempImage = image
                .getSubimage(rectX, rectY, rectW, rectH)
                .getScaledInstance(9, 8, SCALE_SMOOTH);
        // リサイズした画像を、グレースケール化しつつBufferedImageに書き出し
        final var canvas = new BufferedImage(9, 8, BufferedImage.TYPE_BYTE_GRAY);
        final var g = canvas.getGraphics();
        g.drawImage(tempImage, 0, 0, null);
        g.dispose();
        // 隣接ピクセルとの比較結果を符号化する
        long hash = 0;
        for (var y = 0; y < 8; ++y){
            for (var x = 0; x < 8; ++x){
                hash <<= 1;
                final var b1 = canvas.getRGB(x, y) & 0xFF;
                final var b2 = canvas.getRGB(x + 1, y) & 0xFF;
                if (b1 > b2)
                    hash |= 1;
            }
        }
        return hash;
    }
    /**
     * ビットカウント
     * 参考→http://developer.cybozu.co.jp/takesako/2006/11/binary_hacks.html
     * @param x long型(64bit)の値
     * @return ビットカウント後の数
     */
    private static long popcnt(long x) {
        x = ((x & 0xaaaaaaaaaaaaaaaaL) >> 1) + (x & 0x5555555555555555L);
        x = ((x & 0xccccccccccccccccL) >> 2) + (x & 0x3333333333333333L);
        x = ((x & 0xf0f0f0f0f0f0f0f0L) >> 4) + (x & 0x0f0f0f0f0f0f0f0fL);
        x = ((x & 0xff00ff00ff00ff00L) >> 8) + (x & 0x00ff00ff00ff00ffL);
        x = ((x & 0xffff0000ffff0000L) >> 16) + (x & 0x0000ffff0000ffffL);
        x = ((x & 0xffffffff00000000L) >> 32) + (x & 0x00000000ffffffffL);
        return x;
    }
    /**
     * hashｔのハミング距離を計算する
     * @param a 値
     * @return ハミング距離
     */
    private long calcHummingDistance(long a) {
        return popcnt(a ^ hash);
    }
    /**
     * コンストラクタ
     * @param xPer 左上X座標の％
     * @param yPer 左上Y座標の％
     * @param wPer 横幅の％
     * @param hPer 縦幅の％
     * @param hash ハッシュ値
     */
    public SceneEvidenceDH(double xPer, double yPer, double wPer, double hPer, long hash){
        this.xPer = xPer;
        this.yPer = yPer;
        this.wPer = wPer;
        this.hPer = hPer;
        this.hash = hash;
    }
    /**
     * 画像がその証拠とマッチするかを判定する
     * @param image 入力画像
     * @return マッチすればtrue
     */
    public boolean isMatchImage(BufferedImage image){
        final var hash = calcDifferenceHash(image);
        //System.out.println(hash);
        return calcHummingDistance(hash) < 20;
    }
}

/**
 * 画像がそのシーンたりうる証拠(AverageColor版)
 */
class SceneEvidenceAC implements SceneEvidence{
    private double xPer, yPer, wPer, hPer;
    private int r, g, b;
    /**
     * ％表記の割合(A)と100％時のピクセル値(B)から、ピクセルを出力する
     * ただし出力値は、[0, B - 1]にクロップされる
     * @param per 割合
     * @param pixel ピクセル
     * @return 割合値のピクセル
     */
    private static long perToPixel(double per, int pixel){
        final var rawPixel = per * pixel / 100;
        final var roundPixel = Math.round(rawPixel);
        return Math.min(Math.max(roundPixel, 0), pixel - 1);
    }
    /**
     * 画像の一部分における平均色を取得する(rectで指定する範囲は％単位)
     * @param image 画像
     * @return 平均色
     */
    private Color calcAverageColor(BufferedImage image){
        // 画像の選択範囲(％)を選択範囲(ピクセル)に変換
        final var rectX = (int)perToPixel(xPer, image.getWidth());
        final var rectY = (int)perToPixel(yPer, image.getHeight());
        final var rectW = (int)perToPixel(wPer, image.getWidth());
        final var rectH = (int)perToPixel(hPer, image.getHeight());
        // 画素値の平均色を計算する
        long rSum = 0, gSum = 0, bSum = 0;
        for(int y = rectY; y < rectY + rectH; ++y) {
            for (int x = rectX; x < rectX + rectW; ++x) {
                final var color = image.getRGB(x, y);
                rSum += (color >>> 16) & 0xFF;
                gSum += (color >>> 8) & 0xFF;
                bSum += color & 0xFF;
            }
        }
        int rAve = (int)Math.round(1.0 * rSum / rectW / rectH);
        int gAve = (int)Math.round(1.0 * gSum / rectW / rectH);
        int bAve = (int)Math.round(1.0 * bSum / rectW / rectH);
        // クロップ後に出力
        rAve = (rAve < 0 ? 0 : rAve > 255 ? 255 : rAve);
        gAve = (gAve < 0 ? 0 : gAve > 255 ? 255 : gAve);
        bAve = (bAve < 0 ? 0 : bAve > 255 ? 255 : bAve);
        return new Color(rAve, gAve, bAve);
    }
    /**
     * 色間のRGB色空間における距離を計算する
     * @param a 色
     * @return 距離
     */
    private int calcColorDistance(Color a){
        final var rDiff = a.getRed() - r;
        final var gDiff = a.getGreen() - g;
        final var bDiff = a.getBlue() - b;
        return rDiff * rDiff + gDiff * gDiff + bDiff * bDiff;
    }
    /**
     * コンストラクタ
     * @param xPer 左上X座標の％
     * @param yPer 左上Y座標の％
     * @param wPer 横幅の％
     * @param hPer 縦幅の％
     * @param r R値
     * @param g G値
     * @param b B値
     */
    public SceneEvidenceAC(double xPer, double yPer, double wPer, double hPer, int r, int g, int b){
        this.xPer = xPer;
        this.yPer = yPer;
        this.wPer = wPer;
        this.hPer = hPer;
        this.r = r;
        this.g = g;
        this.b = b;
    }
    /**
     * 画像がその証拠とマッチするかを判定する
     * @param image 入力画像
     * @return マッチすればtrue
     */
    public boolean isMatchImage(BufferedImage image){
        final var color = calcAverageColor(image);
        //System.out.println("" + color.getRed() + "," + color.getGreen() + "," + color.getBlue());
        return calcColorDistance(color) < 50;
    }
}

/**
 * 画面のシーン判定を行う
 */
public class SceneRecognitionService {
    /**
     * シーン一覧
     */
    private static Map<String, SceneEvidence[]> sceneList = new HashMap<String, SceneEvidence[]>();
    /**
     * 初期化コード
     */
    public static void initialize(){
        sceneList.put("昼戦後", new SceneEvidence[]{
                new SceneEvidenceDH(18.0 / 8, 2.0 / 4.8, 16.0 / 8, 16.0 / 4.8, 0x20000000L),
                new SceneEvidenceDH(433.0 / 8, 20.0 / 4.8, 20.0 / 8, 20.0 / 4.8, 0x8040C141C2620586L),
                new SceneEvidenceAC(407.0 / 8, 3.0 / 4.8, 20.0 / 8, 20.0 / 4.8, 50, 107, 158)
        });
        sceneList.put("夜戦後", new SceneEvidence[]{
                new SceneEvidenceDH(367.0 / 8, 6.0 / 4.8, 25.0 / 8, 25.0 / 4.8, 0xB97BF9FFAEE9282L),
                new SceneEvidenceDH( 93.0 / 8, 12.0 / 4.8, 18.0 / 8, 18.0 / 4.8, 0x400000000000000L),
                new SceneEvidenceAC(517.0 / 8, 15.0 / 4.8, 20.0 / 8, 20.0 / 4.8, 3, 26, 71)
        });
        sceneList.put("戦闘結果", new SceneEvidence[]{
                new SceneEvidenceDH(367.0 / 8, 6.0 / 4.8, 25.0 / 8, 25.0 / 4.8, 0xB97BF9FFAEE9282L),
                new SceneEvidenceDH( 93.0 / 8, 12.0 / 4.8, 18.0 / 8, 18.0 / 4.8, 0x400000000000000L),
                new SceneEvidenceAC(517.0 / 8, 15.0 / 4.8, 20.0 / 8, 20.0 / 4.8, 3, 26, 71)
        });
        sceneList.put("戦闘判定", new SceneEvidence[]{
                new SceneEvidenceDH(367.0 / 8, 6.0 / 4.8, 25.0 / 8, 25.0 / 4.8, 0xB97BF9FFAEE9282L),
                new SceneEvidenceDH( 93.0 / 8, 12.0 / 4.8, 18.0 / 8, 18.0 / 4.8, 0x400000000000000L),
                new SceneEvidenceAC(517.0 / 8, 15.0 / 4.8, 20.0 / 8, 20.0 / 4.8, 3, 26, 71)
        });
        sceneList.put("MVP", new SceneEvidence[]{
                new SceneEvidenceDH(367.0 / 8, 6.0 / 4.8, 25.0 / 8, 25.0 / 4.8, 0xB97BF9FFAEE9282L),
                new SceneEvidenceDH( 93.0 / 8, 12.0 / 4.8, 18.0 / 8, 18.0 / 4.8, 0x400000000000000L),
                new SceneEvidenceAC(517.0 / 8, 15.0 / 4.8, 20.0 / 8, 20.0 / 4.8, 3, 26, 71)
        });
        sceneList.put("マップ", new SceneEvidence[]{
                new SceneEvidenceDH(367.0 / 8, 6.0 / 4.8, 25.0 / 8, 25.0 / 4.8, 0xB97BF9FFAEE9282L),
                new SceneEvidenceDH( 93.0 / 8, 12.0 / 4.8, 18.0 / 8, 18.0 / 4.8, 0x400000000000000L),
                new SceneEvidenceAC(517.0 / 8, 15.0 / 4.8, 20.0 / 8, 20.0 / 4.8, 3, 26, 71)
        });
    }
    /**
     * シーン判定を行う
     * @param frame スクショ
     * @return シーンを表す文字列
     */
    public static String judgeScene(BufferedImage frame){

        return sceneList.entrySet().stream().filter(e ->
                Arrays.stream(e.getValue()).allMatch(se -> se.isMatchImage(frame))
        ).map(e -> e.getKey()).findFirst().orElse("");
    }
}
