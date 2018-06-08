package com.ysrken.kamo.Service;

import javafx.util.Pair;

import javax.imageio.ImageIO;
import java.util.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.awt.Image.SCALE_SMOOTH;

public class CharacterRecognitionService {
    /** 文字認識用に引き伸ばす縦幅 */
    private static int ocrStretchHeight1 = 64;
    /** 文字認識用に引き伸ばす縦幅 */
    private static int ocrStretchHeight2 = 32;
    private static List<Pair<BufferedImage, Integer>>  template = new ArrayList<>();

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

    /** 画像からRectを参照して切り出す */
    private static BufferedImage getSubimage(BufferedImage image, Rectangle rect){
        return image.getSubimage(rect.x, rect.y, rect.width, rect.height);
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

    /** 2枚の画像間のSSDを計算する */
    private static int getImageSSDForTemplate(BufferedImage a, BufferedImage b){
        return IntStream.range(0, ocrStretchHeight2).map(x -> {
            return IntStream.range(0, ocrStretchHeight2).map(y -> {
                int diff = (a.getRGB(x, y) & 0xFF) - (b.getRGB(x, y) & 0xFF);
                return diff * diff;
            }).sum();
        }).sum();
    }

    /**
     * 各桁の数字を読み取る
     * @param image 画像
     * @param xPer 左上座標(X)の割合％
     * @param yPer 左上座標(Y)の割合％
     * @param wPer 横幅の割合％
     * @param hPer 縦幅の割合％
     * @param threshold しきい値。未反転時ならこの値以上に明るい色を白色にし、反転時はこの値以下に暗い色を黒色にする
     * @param reverseFlg 色を反転させる場合はtrue(白文字用に使う)
     * @param limit 各桁の数字の上限。「0」とするとその桁の数字を読み取らず、「X」とすると0～X-1に値を制限する
     * @return 時間の各数字を配列で
     */
    private static int[] getNumberValue(BufferedImage image, double xPer, double yPer, double wPer, double hPer, int threshold, boolean reverseFlg, int[] limit){
        final boolean debugFlg = false;
        // 画像をクロップし、縦幅を適当に引き伸ばしつつモノクロにする
        final var tempImage1 = getGlayscaleImage(getScaledImage(getSubimagePer(image, xPer, yPer, wPer, hPer), -1, ocrStretchHeight1));
        if(debugFlg) saveImage(tempImage1, "temp1.png");
        // 色の反転・二値化処理を行う
        final var tempImage2 = getThresholdImage(tempImage1, threshold, reverseFlg);
        if(debugFlg) saveImage(tempImage2, "temp2.png");
        // 画像を自動的に分割する
        final var splitRectList = getSplitRect(tempImage2);
        // それぞれの数値を読み取る
        final var digit = new ArrayList<Integer>();
        if(splitRectList.size() == limit.length){
            for(int i = 0; i < splitRectList.size(); ++i){
                if(limit[i] == 0)
                    continue;
                // 分割操作
                final var splitRect = splitRectList.get(i);
                final var splitedImage = getSubimage(tempImage2, splitRect);
                if(debugFlg) saveImage(splitedImage, "temp3-" + (i + 1) + "-1.png");
                // 周囲をトリミング
                final var cropRect = getTrimmingRect(splitedImage);
                final var cropedImage = getSubimage(splitedImage, cropRect);
                if(debugFlg) saveImage(cropedImage, "temp3-" + (i + 1) + "-2.png");
                // 指定したサイズに拡大
                final var fixedImage = getScaledImage(cropedImage, ocrStretchHeight2, ocrStretchHeight2);
                if(debugFlg) saveImage(fixedImage, "temp3-" + (i + 1) + "-3.png");
                // テンプレと比較し、尤もらしい値を推定値とする
                final var ii = i;
                final var template_ = template.stream().filter(pair -> pair.getValue() < limit[ii]).collect(Collectors.toList());
                int minDiff = getImageSSDForTemplate(fixedImage, template_.get(0).getKey());
                int selectIndex = template_.get(0).getValue();
                for(int j = 1; j < template_.size(); ++j){
                    int diff = getImageSSDForTemplate(fixedImage, template_.get(j).getKey());
                    if(minDiff > diff){
                        minDiff = diff;
                        selectIndex = template_.get(j).getValue();
                    }
                }
                digit.add(selectIndex);
            }
        }
        return digit.stream().mapToInt(i -> i).toArray();
    }

    /** 初期化 */
    public static void initialize(){
        final boolean debugFlg = false;
        // テンプレート情報を用意する
        IntStream.range(0, 11).forEach(i -> {
            if(i == 1){
                final var tempImage = new BufferedImage(ocrStretchHeight2, ocrStretchHeight2, BufferedImage.TYPE_3BYTE_BGR);
                final var graphics = tempImage.getGraphics();
                graphics.setColor(Color.white);
                graphics.fillRect(0, 0, ocrStretchHeight2, ocrStretchHeight2);
                graphics.setColor(Color.black);
                graphics.fillRect(10, 0, 18, 32);
                graphics.fillRect(0, 2, 15, 4);
                if(debugFlg) saveImage(tempImage, "tprt-" + i + "-3.png");
                template.add(new Pair<>(tempImage, 1));
                return;
            }
            if(i == 10){
                final var tempImage = new BufferedImage(ocrStretchHeight2, ocrStretchHeight2, BufferedImage.TYPE_3BYTE_BGR);
                final var graphics = tempImage.getGraphics();
                graphics.setColor(Color.white);
                graphics.fillRect(0, 0, ocrStretchHeight2, ocrStretchHeight2);
                graphics.setColor(Color.black);
                graphics.fillRect(23, 0, 9, 32);
                graphics.fillRect(0, 3, 23, 4);
                graphics.fillRect(15, 0, 8, 6);
                if(debugFlg) saveImage(tempImage, "tprt-" + i + "-3.png");
                template.add(new Pair<>(tempImage, 1));
                return;
            }
            // バッファを初期化
            final var tempImage1 = new BufferedImage(ocrStretchHeight1 * 2, ocrStretchHeight1 * 2, BufferedImage.TYPE_3BYTE_BGR);
            // バッファを白く塗りつぶす
            final var graphics = tempImage1.getGraphics();
            graphics.setColor(Color.white);
            graphics.fillRect(0, 0, ocrStretchHeight1 * 2, ocrStretchHeight1 * 2);
            // 黒色で数字を書く
            graphics.setColor(Color.black);
            graphics.setFont(new Font("", Font.PLAIN, ocrStretchHeight1));
            graphics.drawString("" + i, ocrStretchHeight1 / 2, (ocrStretchHeight1 / 2 + ocrStretchHeight1));
            if(debugFlg) saveImage(tempImage1, "tprt-" + i + "-1.png");
            // 最小枠を検出して取り出す
            final var cropRect = getTrimmingRect(tempImage1);
            final var cropedImage = getSubimage(tempImage1, cropRect);
            if(debugFlg) saveImage(cropedImage, "tprt-" + i + "-2.png");
            // 指定したサイズに拡大
            final var fixedImage = getScaledImage(cropedImage, ocrStretchHeight2, ocrStretchHeight2);
            if(debugFlg) saveImage(fixedImage, "tprt-" + i + "-3.png");
            // 記憶
            template.add(new Pair<>(fixedImage, i));
        });
    }

    /** 画像から遠征残り時間を取り出す*/
    public static long getExpeditionRemainingTime(BufferedImage image){
        // 画像の一部分から遠征残り時間を出す
        final var digit = getNumberValue(image, 719.0 / 8, 383.0 / 4.8, 70.0 / 8, 20.0 / 4.8, 185, false, new int[]{8, 10, 0, 6, 10, 0, 6, 10});
        final long second = ((digit[0] * 10 + digit[1]) * 60 + digit[2] * 10 + digit[3]) * 60 + digit[4] * 10 + digit[5];
        return second;
    }

    /** 艦隊番号をKey、遠征IDをValueといった形式で取り出す */
    public static Map<Integer, String> getExpeditionFleetId(BufferedImage image){
        final var result = new HashMap<Integer, String>();
        // 左上の数字を読み取ることでオフセットを判断する
        final var digit = getNumberValue(image, 121.0 / 8, 167.0 / 4.8, 21.0 / 8, 17.0 / 4.8, 180, false, new int[]{4, 10});
        System.out.println(Arrays.toString(digit));
        return result;
    }
}
