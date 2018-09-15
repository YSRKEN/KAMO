package com.ysrken.kamo;

import com.ysrken.kamo.service.UtilityService;
import com.ysrken.kamo.stage.ExtraStageImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

/** 自作の画像クラス */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BitmapImage {
    /** 画像データを格納するフィールド */
    private BufferedImage image;

    /**
     * 各種サービス
     */
    @Autowired
    private UtilityService utility;
    
    /** BufferedImageから変換 */
    public static BitmapImage of(BufferedImage image){
        if (MainApp.getApplicationContext() != null) {
            final BitmapImage bitmap = MainApp.getApplicationContext().getBean(BitmapImage.class);
            bitmap.image = image;
            return bitmap;
        }else{
            final BitmapImage bitmap = new BitmapImage();
            bitmap.image = image;
            return bitmap;
        }
    }

    /** 画像の横幅 */
    public int getWidth(){
        return image.getWidth();
    }

    /** 画像の縦幅 */
    public int getHeight(){
        return image.getHeight();
    }

    /** 画像の縦幅 */
    public int getType(){
        return image.getType();
    }

    /** 画像部分を取り出す */
    public  BufferedImage getImage(){
        return image;
    }

    /** 指定した位置・サイズに切り抜く */
    public BitmapImage crop(int x, int y, int w, int h){
        return BitmapImage.of(image.getSubimage(x, y, w, h));
    }

    /** 指定した位置・サイズに切り抜く */
    public BitmapImage crop(Rectangle rect){ return BitmapImage.of(image.getSubimage(rect.x, rect.y, rect.width, rect.height)); }

    /** 指定した位置・サイズ(割合指定)に切り抜く */
    public BitmapImage crop(double xPer, double yPer, double wPer, double hPer){
        final int x = (int)Math.round(xPer * image.getWidth() / 100);
        final int y = (int)Math.round(yPer * image.getHeight() / 100);
        final int w = (int)Math.round(wPer * image.getWidth() / 100);
        final int h = (int)Math.round(hPer * image.getHeight() / 100);
        return BitmapImage.of(image.getSubimage(x, y, w, h));
    }

    /**
     * 指定したサイズにリサイズする。ただしxかyが-1だった場合、両方-1でなければ、-1じゃない方に辺を合わせてリサイズする
     * @param x 横幅
     * @param y 縦幅
     * @return リサイズ後の画像
     */
    public BitmapImage resize(int x, int y){
        final Image instance = image.getScaledInstance(x, y, Image.SCALE_SMOOTH);
        final BufferedImage buffered = new BufferedImage(instance.getWidth(null), instance.getHeight(null), image.getType());
        final Graphics g = buffered.getGraphics();
        g.drawImage(instance, 0, 0, null);
        g.dispose();
        return BitmapImage.of(buffered);
    }

    /** 画像を指定した位置に貼り付ける */
    public BitmapImage paste(BitmapImage other, int x, int y){
        final BufferedImage buffered = new BufferedImage(this.getWidth(), this.getHeight(), this.getType());
        final Graphics g = buffered.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.drawImage(other.image, x, y, null);
        g.dispose();
        return BitmapImage.of(buffered);
    }

    /** モノクロに変換する */
    public BitmapImage mono(){
        final ColorConvertOp colorConvert = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
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

    /** ある点の色を取得する */
    public Color getColor(int x, int y){
        final int rgb = image.getRGB(x, y);
        final int r = (rgb >>> 16) & 0xFF;
        final int g = (rgb >>> 8) & 0xFF;
        final int b = rgb & 0xFF;
        return new Color(r, g, b);
    }

    /** 指定した位置・サイズ(割合指定)におけるDifferenceHashを計算する */
    public long calcDifferenceHash(double xPer, double yPer, double wPer, double hPer){
        // 元画像を切り抜き、9x8ピクセルにリサイズした後にグレースケール化
        final BitmapImage tempImage = this.crop(xPer, yPer, wPer, hPer).resize(9, 8).mono();
        // 隣接ピクセルとの比較結果を符号化する
        long hash = 0;
        for (int y = 0; y < 8; ++y){
            for (int x = 0; x < 8; ++x){
                hash <<= 1;
                final int b1 = tempImage.getBlue(x, y);
                final int b2 = tempImage.getBlue(x + 1, y);
                if (b1 > b2)
                    hash |= 1;
            }
        }
        return hash;
    }

    /** 指定した位置・サイズ(割合指定)におけるAverageColorを計算する */
    public Color calcAverageColor(double xPer, double yPer, double wPer, double hPer){
        final BitmapImage tempImage = this.crop(xPer, yPer, wPer, hPer);
        int width = tempImage.image.getWidth();
        int height = tempImage.image.getHeight();
        // 画素値の平均色を計算する
        long rSum = 0, gSum = 0, bSum = 0;
        for(int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                final int color = tempImage.image.getRGB(x, y);
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

    /** 周囲の黒色じゃない領域をトリミングできる範囲を取り出す */
    public Rectangle calcTrimmingRect(){
        // 水平方向から縦に見たヒストグラム
        final int[] blackCountWidth = IntStream.range(0, image.getWidth()).map(x -> {
            return  (int)IntStream.range(0, image.getHeight()).filter(y -> (image.getRGB(x, y) & 0xFF) == 0).count();
        }).toArray();
        // 垂直方向から横に見たヒストグラム
        final int[] blackCountHeight = IntStream.range(0, image.getHeight()).map(y -> {
            return  (int)IntStream.range(0, image.getWidth()).filter(x -> (image.getRGB(x, y) & 0xFF) == 0).count();
        }).toArray();
        // Rectを割り出す
        final Rectangle rect = new Rectangle(0, 0, image.getWidth(), image.getHeight());
        rect.x = IntStream.range(0, image.getWidth()).filter(i -> blackCountWidth[i] != 0).findFirst().orElse(0);
        rect.y = IntStream.range(0, image.getHeight()).filter(i -> blackCountHeight[i] != 0).findFirst().orElse(0);
        rect.width = IntStream.range(0, image.getWidth()).map(i -> image.getWidth() - i - 1).filter(i -> blackCountWidth[i] != 0)
                .findFirst().orElse(image.getWidth() - 1) - rect.x + 1;
        rect.height = IntStream.range(0, image.getHeight()).map(i -> image.getHeight() - i - 1).filter(i -> blackCountHeight[i] != 0)
                .findFirst().orElse(image.getHeight() - 1) - rect.y + 1;
        return rect;
    }

    /** 画像をpng形式で保存する */
    public boolean save(String path){
        try {
            ImageIO.write(image,"png", new File(path));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 画像を指定したしきい値で二値化する。B値を読んで、
     * ・しきい値以上に明るさの色を白色にする
     * ・しきい値未満の明るさの色を黒色にする
     */
    public BitmapImage threshold(int threshold){
        final BufferedImage tempImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        IntStream.range(0, image.getHeight()).boxed().forEach(y -> {
            IntStream.range(0, image.getWidth()).forEach(x -> {
                int b = this.getBlue(x, y);
                if(b >= threshold){
                    tempImage.setRGB(x, y, Color.white.getRGB());
                }else{
                    tempImage.setRGB(x, y, Color.black.getRGB());
                }
            });
        });
        return BitmapImage.of(tempImage);
    }

    /**
     * 画像を指定したしきい値・しきい色で二値化する。
     * 画素が色colorより距離がthreshold未満なら黒色にし、そうでない場合は白色にする
     */
    public BitmapImage threshold(Color color, int threshold){
        final BufferedImage tempImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        IntStream.range(0, image.getHeight()).boxed().forEach(y -> {
            IntStream.range(0, image.getWidth()).forEach(x -> {
                final Color color2 = this.getColor(x, y);
                if(utility.calcColorDistance(color, color2) >= threshold){
                    tempImage.setRGB(x, y, Color.white.getRGB());
                }else{
                    tempImage.setRGB(x, y, Color.black.getRGB());
                }
            });
        });
        return BitmapImage.of(tempImage);
    }

    /** 画像を横方向に見て、区切れる場所で分割する */
    public List<Rectangle> getSplitRect(){
        // バッファを用意する
        final List<Rectangle> list = new ArrayList<Rectangle>();
        // 水平方向のヒストグラムを割り出す
        final int[] blackCountWidth = IntStream.range(0, image.getWidth()).map(x -> {
            return  (int)IntStream.range(0, image.getHeight())
                    .filter(y -> this.getBlue(x, y) == 0).count();
        }).toArray();
        // 切り出しつつ、最小枠を検出してバッファに保存する
        for(int xBegin = 0; xBegin < image.getWidth(); ++xBegin){
            // まずは左端を検出する
            if(blackCountWidth[xBegin] == 0)
                continue;
            // 次に右端を検出する
            final OptionalInt temp = IntStream.range(xBegin + 1, image.getWidth()).filter(x -> blackCountWidth[x] == 0).findFirst();
            final int xEnd = temp.isPresent() ? temp.getAsInt() : image.getWidth();
            list.add(new Rectangle(xBegin, 0, xEnd - xBegin, image.getHeight()));
            xBegin = xEnd;
        }
        return list;
    }

    /** 2枚の画像間のSSDを計算する */
    public int getSSD(BitmapImage other){
        return IntStream.range(0, this.image.getWidth()).map(x -> {
            return IntStream.range(0, this.image.getHeight()).map(y -> {
                int diff = this.getBlue(x, y) - other.getBlue(x, y);
                return diff * diff;
            }).sum();
        }).sum();
    }

    /** 画像を反転処理する */
    public BitmapImage reverse(){
        final BufferedImage tempImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        IntStream.range(0, image.getHeight()).boxed().forEach(y -> {
            IntStream.range(0, image.getWidth()).forEach(x -> {
                final int color = this.image.getRGB(x, y);
                final int r = (color >>> 16) & 0xFF;
                final int g = (color >>> 8) & 0xFF;
                final int b = color & 0xFF;
                final int color2 = (0xFF << 24) | (r << 16) | (g << 8) | b;
                tempImage.setRGB(x, y, color2);
            });
        });
        return BitmapImage.of(tempImage);
    }
}
