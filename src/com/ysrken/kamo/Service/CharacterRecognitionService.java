package com.ysrken.kamo.Service;

import javax.imageio.ImageIO;
import java.util.Arrays;
import java.util.List;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.stream.Collector;
import java.util.stream.IntStream;

import static java.awt.Image.SCALE_SMOOTH;

public class CharacterRecognitionService {
    /** 文字認識用に引き伸ばす縦幅 */
    private static int ocrStretchHeight1 = 64;
    /** 文字認識用に引き伸ばす縦幅 */
    private static int ocrStretchHeight2 = 32;

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

    /** 周囲から最小サイズを切り出すためにRectを割り出す */
    private static Rectangle getTrimmingRect(BufferedImage image){
        // 水平方向から縦に見たヒストグラム
        final var blackCountWidth = IntStream.range(0, image.getWidth()).map(x -> {
            return  (int)IntStream.range(0, image.getHeight()).filter(y -> (image.getRGB(x, y) & 0xFF) == 0).count();
        }).toArray();
        // 垂直方向から横に見たヒストグラム
        final var blackCountHeight = IntStream.range(0, image.getHeight()).map(y -> {
            return  (int)IntStream.range(0, image.getWidth()).filter(x -> (image.getRGB(x, y) & 0xFF) == 0).count();
        }).toArray();
        // Rectを割り出す
        final var rect = new Rectangle(0, 0, image.getWidth(), image.getHeight());
        rect.x = IntStream.range(0, image.getWidth()).filter(i -> blackCountWidth[i] != 0).findFirst().orElse(0);
        rect.y = IntStream.range(0, image.getHeight()).filter(i -> blackCountHeight[i] != 0).findFirst().orElse(0);
        rect.width = IntStream.range(0, image.getWidth()).map(i -> image.getWidth() - i - 1).filter(i -> blackCountWidth[i] != 0)
                .findFirst().orElse(image.getWidth() - 1) - rect.x + 1;
        rect.height = IntStream.range(0, image.getHeight()).map(i -> image.getHeight() - i - 1).filter(i -> blackCountHeight[i] != 0)
                .findFirst().orElse(image.getHeight() - 1) - rect.y + 1;
        return rect;
    }

    /** 画像から割合(％)で画像を切り出す */
    private static BufferedImage getSubimagePer(BufferedImage image, double xPer, double yPer, double wPer, double hPer){
        // 画像の選択範囲(％)を選択範囲(ピクセル)に変換
        final var rectX = (int)perToPixel(xPer, image.getWidth());
        final var rectY = (int)perToPixel(yPer, image.getHeight());
        final var rectW = (int)perToPixel(wPer, image.getWidth());
        final var rectH = (int)perToPixel(hPer, image.getHeight());
        // 画像をクロップ
        return image.getSubimage(rectX, rectY, rectW, rectH);
    }

    /** 画像をリサイズする */
    private static BufferedImage getScaledImage(BufferedImage image, int width, int height){
        final var tempImage1 = image.getScaledInstance(width, height, SCALE_SMOOTH);
        final var tempImage2 = new BufferedImage(tempImage1.getWidth(null), tempImage1.getHeight(null), image.getType());
        tempImage2.getGraphics().drawImage(tempImage1, 0, 0, null);
        return tempImage2;
    }

    /** 画像をグレースケールに変換する */
    private static BufferedImage getGlayscaleImage(BufferedImage image){
        final var colorConvert = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        return colorConvert.filter(image, null);
    }

    /** 画像をpng形式で保存する */
    private static boolean saveImage(BufferedImage image, String path){
        try {
            ImageIO.write(image,"png", new File(path));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 画像を指定したしきい値で二値化する。
     * ・色を反転しない際は、しきい値以上に明るい色を白色にする
     * ・色を判定する際は、しきい値以下に暗い色を黒色にする
     * @param image 画像
     * @param threshold しきい値
     * @param reverseFlg 色を反転させるか？
     * @return
     */
    private static BufferedImage getThresholdImage(BufferedImage image, int threshold, boolean reverseFlg){
        final var tempImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        IntStream.range(0, image.getHeight()).boxed().forEach(y -> {
            IntStream.range(0, image.getWidth()).forEach(x -> {
                int color = image.getRGB(x, y) & 0xFF;
                if(reverseFlg){
                    if(color <= threshold){
                        tempImage.setRGB(x, y, Color.black.getRGB());
                    }else{
                        tempImage.setRGB(x, y, Color.white.getRGB());
                    }
                }else{
                    if(color >= threshold){
                        tempImage.setRGB(x, y, Color.white.getRGB());
                    }else{
                        tempImage.setRGB(x, y, Color.black.getRGB());
                    }
                }
            });
        });
        return tempImage;
    }

    /**
     * 画像を横方向に見て、区切れる場所で分割する
     * @param image 画像
     * @return 区切った後の画像群
     */
    private static List<Rectangle> getSplitRect(BufferedImage image){
        // バッファを用意する
        final var list = new ArrayList<Rectangle>();
        // 水平方向のヒストグラムを割り出す
        final var blackCountWidth = IntStream.range(0, image.getWidth()).map(x -> {
            return  (int)IntStream.range(0, image.getHeight()).filter(y -> (image.getRGB(x, y) & 0xFF) == 0).count();
        }).toArray();
        // 切り出しつつ、最小枠を検出してバッファに保存する
        for(int xBegin = 0; xBegin < image.getWidth(); ++xBegin){
            // まずは左端を検出する
            if(blackCountWidth[xBegin] == 0)
                continue;
            // 次に右端を検出する
            final var temp = IntStream.range(xBegin + 1, image.getWidth()).filter(x -> blackCountWidth[x] == 0).findFirst();
            final var xEnd = temp.isPresent() ? temp.getAsInt() : image.getWidth();
            list.add(new Rectangle(xBegin, 0, xEnd - xBegin, image.getHeight()));
            xBegin = xEnd;
        }
        return list;
    }

    /**
     * 「00:00:00」形式の画像から、数字部分の位置を検出して正確に取り出す
     * @param image 画像
     * @param xPer 左上座標(X)の割合％
     * @param yPer 左上座標(Y)の割合％
     * @param wPer 横幅の割合％
     * @param hPer 縦幅の割合％
     * @param threshold しきい値。未反転時ならこの値以上に明るい色を白色にし、反転時はこの値以下に暗い色を黒色にする
     * @param reverseFlg 色を反転させる場合はtrue(白文字用に使う)
     * @return 時間の各数字を配列で
     */
    private static int[] getRemainingTime(BufferedImage image, double xPer, double yPer, double wPer, double hPer, int threshold, boolean reverseFlg) {
        final boolean debugFlg = true;
        // 画像をクロップし、縦幅を適当に引き伸ばしつつモノクロにする
        final var tempImage1 = getGlayscaleImage(getScaledImage(getSubimagePer(image, xPer, yPer, wPer, hPer), -1, ocrStretchHeight1));
        if(debugFlg) saveImage(tempImage1, "temp1.png");
        // 色の反転・二値化処理を行う
        final var tempImage2 = getThresholdImage(tempImage1, threshold, reverseFlg);
        if(debugFlg) saveImage(tempImage2, "temp2.png");
        // 画像を自動的に分割する
        final var splitRectList = getSplitRect(tempImage2);
        // それぞれの数値を読み取る
        final var digit = IntStream.range(0, 6).map(i -> i == 5 ? -1 : 0).toArray();
        if(splitRectList.size() == 8){
            final var indexList = new int[]{0, 1, 3, 4, 6, 7};
            for(int i = 0; i < indexList.length; ++i){
                final var splitRect = splitRectList.get(indexList[i]);
                final var splitedImage = tempImage2.getSubimage(splitRect.x, splitRect.y, splitRect.width, splitRect.height);
                if(debugFlg) saveImage(splitedImage, "temp3-" + (i + 1) + "-1.png");
                final var cropRect = getTrimmingRect(splitedImage);
                final var cropedImage = splitedImage.getSubimage(cropRect.x, cropRect.y, cropRect.width, cropRect.height);
                if(debugFlg) saveImage(cropedImage, "temp3-" + (i + 1) + "-2.png");
            }
        }
        return digit;
    }
    /** 画像から遠征残り時間を取り出す*/
    public static Duration getExpeditionRemainingTime(BufferedImage image){
        // 画像の一部分から遠征残り時間を出す
        final var digit = getRemainingTime(image, 719.0 / 8, 383.0 / 4.8, 70.0 / 8, 20.0 / 4.8, 185, false);
        return Duration.parse("PT3H4M5S");
    }
}
