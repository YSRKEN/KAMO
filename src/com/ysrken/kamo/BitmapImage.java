package com.ysrken.kamo;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;

/** 自作の画像クラス */
public class BitmapImage {
    /** 画像データを格納するフィールド */
    private BufferedImage image;

    /** BufferedImageから変換 */
    public static BitmapImage of(BufferedImage image){
        final var bitmap = new BitmapImage();
        bitmap.image = image;
        return bitmap;
    }

    /** 指定した位置・サイズに切り抜く */
    public BitmapImage crop(int x, int y, int w, int h){
        return BitmapImage.of(image.getSubimage(x, y, w, h));
    }

    /** 指定した位置・サイズ(割合指定)に切り抜く */
    public BitmapImage crop(double xPer, double yPer, double wPer, double hPer){
        final var x = (int)Math.round(xPer * image.getWidth() / 100);
        final var y = (int)Math.round(yPer * image.getHeight() / 100);
        final var w = (int)Math.round(wPer * image.getWidth() / 100);
        final var h = (int)Math.round(hPer * image.getHeight() / 100);
        return BitmapImage.of(image.getSubimage(x, y, w, h));
    }

    /** 指定したサイズにリサイズする */
    public BitmapImage resize(int x, int y){
        final var buffered = new BufferedImage(x, y, image.getType());
        final var g = buffered.getGraphics();
        g.drawImage(image.getScaledInstance(x, y, Image.SCALE_SMOOTH), 0, 0, null);
        g.dispose();
        return BitmapImage.of(buffered);
    }

    /** モノクロに変換する */
    public BitmapImage mono(){
        final var colorConvert = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        return BitmapImage.of(colorConvert.filter(image, null));
    }

    /** ある点のR値を取得する */
    public int getRed(int x, int y){
        return (image.getRGB(x, y) >>> 16) & 0xFF;
    }

    /** ある点のG値を取得する */
    public int getGreen(int x, int y){
        return (image.getRGB(x, y) >>> 8) & 0xFF;
    }

    /** ある点のB値を取得する */
    public int getBlue(int x, int y){
        return image.getRGB(x, y) & 0xFF;
    }

    /** 指定した位置・サイズ(割合指定)におけるDifferenceHashを計算する */
    public long calcDifferenceHash(double xPer, double yPer, double wPer, double hPer){
        // 元画像を切り抜き、9x8ピクセルにリサイズした後にグレースケール化
        final var tempImage = this.crop(xPer, yPer, wPer, hPer).resize(9, 8).mono();
        // 隣接ピクセルとの比較結果を符号化する
        long hash = 0;
        for (var y = 0; y < 8; ++y){
            for (var x = 0; x < 8; ++x){
                hash <<= 1;
                final var b1 = tempImage.getBlue(x, y);
                final var b2 = tempImage.getBlue(x + 1, y);
                if (b1 > b2)
                    hash |= 1;
            }
        }
        return hash;
    }

    /** 指定した位置・サイズ(割合指定)におけるAverageColorを計算する */
    public Color calcAverageColor(double xPer, double yPer, double wPer, double hPer){
        final var tempImage = this.crop(xPer, yPer, wPer, hPer);
        int width = tempImage.image.getWidth();
        int height = tempImage.image.getHeight();
        // 画素値の平均色を計算する
        long rSum = 0, gSum = 0, bSum = 0;
        for(int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                final var color = tempImage.image.getRGB(x, y);
                rSum += (color >>> 16) & 0xFF;
                gSum += (color >>> 8) & 0xFF;
                bSum += color & 0xFF;
            }
        }
        int rAve = (int)Math.min(Math.max(Math.round(1.0 * rSum / width / height), 0), 255);
        int gAve = (int)Math.min(Math.max(Math.round(1.0 * gSum / width / height), 0), 255);
        int bAve = (int)Math.min(Math.max(Math.round(1.0 * bSum / width / height), 0), 255);
        return new Color(rAve, gAve, bAve);
    }
}
