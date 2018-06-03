package com.ysrken.kamo.Service;

import javax.imageio.ImageIO;
import java.util.List;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.awt.Image.SCALE_SMOOTH;

public class CharacterRecognitionService {
    /** 文字認識用に引き伸ばす縦幅 */
    private static int ocrStretchHeight = 128;

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
        // 画像の選択範囲(％)を選択範囲(ピクセル)に変換
        final var rectX = (int)perToPixel(xPer, image.getWidth());
        final var rectY = (int)perToPixel(yPer, image.getHeight());
        final var rectW = (int)perToPixel(wPer, image.getWidth());
        final var rectH = (int)perToPixel(hPer, image.getHeight());
        // 画像をクロップし、縦幅を適当に引き伸ばしつつモノクロにする
        // java.awt.Image
        final var tempImage1 = image
                    .getSubimage(rectX, rectY, rectW, rectH)
                    .getScaledInstance(-1, ocrStretchHeight, SCALE_SMOOTH);
        final var tempImage2 = new BufferedImage(tempImage1.getWidth(null), tempImage1.getHeight(null), image.getType());
        tempImage2.getGraphics().drawImage(tempImage1, 0, 0, null);
        final var colorConvert = new ColorConvertOp(
                ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        final var tempImage3 = colorConvert.filter(tempImage2, null);
        try {
            ImageIO.write(tempImage3,"png", new File("temp1.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 色の反転・二値化処理を行う
        final var tempImage4 = new BufferedImage(tempImage3.getWidth(), tempImage3.getHeight(), tempImage3.getType());
        IntStream.range(0, tempImage2.getHeight()).boxed().forEach(y -> {
             IntStream.range(0, tempImage3.getWidth()).forEach(x -> {
                 int color = tempImage3.getRGB(x, y) & 0xFF;
                 if(reverseFlg){
                     if(color <= threshold){
                         tempImage4.setRGB(x, y, Color.black.getRGB());
                     }else{
                         tempImage4.setRGB(x, y, Color.white.getRGB());
                     }
                 }else{
                     if(color >= threshold){
                         tempImage4.setRGB(x, y, Color.white.getRGB());
                     }else{
                         tempImage4.setRGB(x, y, Color.black.getRGB());
                     }
                 }
            });
        });
        try {
            ImageIO.write(tempImage4, "png", new File("temp2.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 区切る位置を検出する
        final var blackCount = IntStream.range(0, tempImage4.getWidth()).map(x -> {
            return  (int)IntStream.range(0, tempImage4.getHeight()).filter(y -> (tempImage4.getRGB(x, y) & 0xFF) == 0).count();
        }).toArray();
        List<Rectangle> splitRectList = new ArrayList<>();
        for(int xBegin = 0; xBegin < tempImage4.getWidth(); ++xBegin){
            // まずは左端を検出する
            if(blackCount[xBegin] == 0)
                continue;
            // 次に右端を検出する
            final var temp = IntStream.range(xBegin + 1, tempImage4.getWidth()).filter(x -> blackCount[x] == 0).findFirst();
            final var xEnd = temp.isPresent() ? temp.getAsInt() : tempImage4.getWidth();
            splitRectList.add(new Rectangle(xBegin, 0, xEnd - xBegin, tempImage4.getHeight()));
            xBegin = xEnd;
        }
        // それぞれの数値を読み取る
        final var digit = new int[6];
        if(splitRectList.size() == 8){

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
