package com.ysrken.kamo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static java.awt.Image.SCALE_SMOOTH;

public class SceneRecognitionService {
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
     * ハミング距離を計算する
     * @param a 値1
     * @param b 値2
     * @return ハミング距離
     */
    public static long calcHummingDistance(long a, long b) {
        return popcnt(a ^ b);
    }
    /**
     * 画像の一部分におけるDifferenceHashを取得する(rectで指定する範囲は％単位)
     * @param image 画像
     * @param rectPer rect(％表記)
     * @return ハッシュ値
     */
    private static long calcDifferenceHash(BufferedImage image, Rectangle2D.Double rectPer) {
        // 画像の選択範囲(％)を選択範囲(ピクセル)に変換
        final var rectX = (int)perToPixel(rectPer.getX(), image.getWidth());
        final var rectY = (int)perToPixel(rectPer.getY(), image.getHeight());
        final var rectW = (int)perToPixel(rectPer.getWidth(), image.getWidth());
        final var rectH = (int)perToPixel(rectPer.getHeight(), image.getHeight());
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
     * 画像の一部分における平均色を取得する(rectで指定する範囲は％単位)
     * @param image 画像
     * @param rectPer rect(％表記)
     * @return 平均色
     */
    private static Color calcAverageColor(BufferedImage image, Rectangle2D.Double rectPer){
        // 画像の選択範囲(％)を選択範囲(ピクセル)に変換
        final var rectX = (int)perToPixel(rectPer.getX(), image.getWidth());
        final var rectY = (int)perToPixel(rectPer.getY(), image.getHeight());
        final var rectW = (int)perToPixel(rectPer.getWidth(), image.getWidth());
        final var rectH = (int)perToPixel(rectPer.getHeight(), image.getHeight());
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
     * 画像間のRGB色空間における距離を計算する
     * @param a 色1
     * @param b 色2
     * @return 距離
     */
    private static int calcColorDistance(Color a, Color b){
        final var rDiff = a.getRed() - b.getRed();
        final var gDiff = a.getGreen() - b.getGreen();
        final var bDiff = a.getBlue() - b.getBlue();
        return rDiff * rDiff + gDiff * gDiff + bDiff * bDiff;
    }

    /**
     * シーン判定を行う
     * @param frame スクショ
     * @return シーンを表す文字列
     */
    public static String judgeScene(BufferedImage frame){
        /*try {
            frame = ImageIO.read(new File("pic\\2018-05-20 11-06-39-103.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        // スタブ
        final var rect1 = new Rectangle2D.Double(18.0 / 8, 2.0 / 4.8, 16.0 / 8, 16.0 / 4.8);
        final var hash1 = calcDifferenceHash(frame, rect1);
        final var rect2 = new Rectangle2D.Double(433.0 / 8, 20.0 / 4.8, 20.0 / 8, 20.0 / 4.8);
        final var hash2 = calcDifferenceHash(frame, rect2);
        if(calcHummingDistance(hash1, 0x20000000L) < 20 & calcHummingDistance(hash2, 0x8040C141C2620586L) < 20){
            final var rect3 = new Rectangle2D.Double(407.0 / 8, 3.0 / 4.8, 20.0 / 8, 20.0 / 4.8);
            final var color = calcAverageColor(frame, rect3);
            if(calcColorDistance(color, new Color(50, 107, 158)) < 50){
                return "昼戦後";
            }
        }

        return "";
    }
}
