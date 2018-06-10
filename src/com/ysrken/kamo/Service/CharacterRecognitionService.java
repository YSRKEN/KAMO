package com.ysrken.kamo.Service;

import com.ysrken.kamo.BitmapImage;
import com.ysrken.kamo.Utility;
import javafx.util.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CharacterRecognitionService {
    /** 文字認識用に引き伸ばす縦幅 */
    private static int ocrStretchHeight1 = 64;
    /** 文字認識用に引き伸ばす縦幅 */
    private static int ocrStretchHeight2 = 32;
    /** 文字認識に使用するテンプレート */
    private static List<Pair<BitmapImage, Integer>>  template = new ArrayList<>();
    /** 遠征情報を検索するマップ。遠征ID→(遠征名, ハッシュ値) */
    private static Map<String, Pair<String, Long>> expeditionDataMap = new HashMap<>();

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
        final var tempImage1 = BitmapImage.of(image).crop(xPer, yPer, wPer, hPer).resize(-1, ocrStretchHeight1).mono();
        if(debugFlg) tempImage1.save("temp1.png");
        // 色の反転・二値化処理を行う
        final var tempImage2 = (reverseFlg ? tempImage1.reverse().threshold(threshold) : tempImage1.threshold(threshold));
        if(debugFlg) tempImage2.save("temp2.png");
        // 画像を自動的に分割する
        final var splitRectList = tempImage2.getSplitRect();
        // それぞれの数値を読み取る
        final var digit = new ArrayList<Integer>();
        if(splitRectList.size() == limit.length){
            for(int i = 0; i < splitRectList.size(); ++i){
                if(limit[i] == 0)
                    continue;
                // 分割操作
                final var splitedImage = tempImage2.crop(splitRectList.get(i));
                if(debugFlg) splitedImage.save("temp3-" + (i + 1) + "-1.png");
                // 周囲をトリミング
                final var cropedImage = splitedImage.crop(splitedImage.calcTrimmingRect());
                if(debugFlg) cropedImage.save("temp3-" + (i + 1) + "-2.png");
                // 指定したサイズに拡大
                final var fixedImage = cropedImage.resize(ocrStretchHeight2, ocrStretchHeight2);
                if(debugFlg) fixedImage.save("temp3-" + (i + 1) + "-3.png");
                // テンプレと比較し、尤もらしい値を推定値とする
                final var ii = i;
                final var template_ = template.stream().filter(pair -> pair.getValue() < limit[ii]).collect(Collectors.toList());
                int minDiff = fixedImage.getSSD(template_.get(0).getKey());
                int selectIndex = template_.get(0).getValue();
                for(int j = 1; j < template_.size(); ++j){
                    int diff =  fixedImage.getSSD(template_.get(j).getKey());
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
                final var tempImageBi = BitmapImage.of(tempImage);
                if(debugFlg) tempImageBi.save("tprt-" + i + "-3.png");
                template.add(new Pair<>(tempImageBi, 1));
                return;
            }else if(i == 10){
                final var tempImage = new BufferedImage(ocrStretchHeight2, ocrStretchHeight2, BufferedImage.TYPE_3BYTE_BGR);
                final var graphics = tempImage.getGraphics();
                graphics.setColor(Color.white);
                graphics.fillRect(0, 0, ocrStretchHeight2, ocrStretchHeight2);
                graphics.setColor(Color.black);
                graphics.fillRect(23, 0, 9, 32);
                graphics.fillRect(0, 3, 23, 4);
                graphics.fillRect(15, 0, 8, 6);
                final var tempImageBi = BitmapImage.of(tempImage);
                if(debugFlg) tempImageBi.save("tprt-" + i + "-3.png");
                template.add(new Pair<>(tempImageBi, 1));
                return;
            }else{
                // バッファを初期化
                final var tempImage = new BufferedImage(ocrStretchHeight1 * 2, ocrStretchHeight1 * 2, BufferedImage.TYPE_3BYTE_BGR);
                // バッファを白く塗りつぶす
                final var graphics = tempImage.getGraphics();
                graphics.setColor(Color.white);
                graphics.fillRect(0, 0, ocrStretchHeight1 * 2, ocrStretchHeight1 * 2);
                // 黒色で数字を書く
                graphics.setColor(Color.black);
                graphics.setFont(new Font("", Font.PLAIN, ocrStretchHeight1));
                graphics.drawString("" + i, ocrStretchHeight1 / 2, (ocrStretchHeight1 / 2 + ocrStretchHeight1));
                final  var tempImageBi = BitmapImage.of(tempImage);
                if(debugFlg) tempImageBi.save("tprt-" + i + "-1.png");
                // 最小枠を検出して取り出す
                final var cropedImage = tempImageBi.crop(tempImageBi.calcTrimmingRect());
                if(debugFlg) cropedImage.save("tprt-" + i + "-2.png");
                // 指定したサイズに拡大
                final var fixedImage = cropedImage.resize(ocrStretchHeight2, ocrStretchHeight2);
                if(debugFlg) fixedImage.save("tprt-" + i + "-3.png");
                // 記憶
                template.add(new Pair<>(fixedImage, i));
            }
        });
        // 遠征情報を用意する
        try(final var is = ClassLoader.getSystemResourceAsStream("com/ysrken/kamo/File/ExpeditionList.csv");
            final var isr = new InputStreamReader(is, Charset.forName("UTF-8"));
            final var br = new BufferedReader(isr)) {
            // テキストデータを用意し、1行ごとに処理を行う
            br.lines().forEach(getLine -> {
                // カンマで分割
                final var split = getLine.split(",");
                // 使わないデータを無視
                if(split.length < 3)
                    return;
                if(split[0].equals("id"))
                    return;
                // 読み取り
                expeditionDataMap.put(split[0], new Pair<>(split[1], Long.parseUnsignedLong(split[2], 16)));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /** 画像から遠征残り時間を取り出す*/
    public static long getExpeditionRemainingTime(BufferedImage image){
        // 画像の一部分から遠征残り時間を出す
        final var digit = getNumberValue(image, 719.0 / 8, 383.0 / 4.8, 70.0 / 8, 20.0 / 4.8, 185, false, new int[]{8, 10, 0, 6, 10, 0, 6, 10});
        if(digit.length >= 6) {
            final long second = ((digit[0] * 10 + digit[1]) * 60 + digit[2] * 10 + digit[3]) * 60 + digit[4] * 10 + digit[5];
            return second;
        }else{
            return -1;
        }
    }
    /** 艦隊番号をKey、遠征IDをValueといった形式で取り出す */
    public static Map<Integer, String> getExpeditionFleetId(BufferedImage image){
        final boolean debugFlg = false;
        final var result = new HashMap<Integer, String>();
        // 左上の数字を読み取ることでオフセットを判断する
        final var digit = getNumberValue(image, 121.0 / 8, 167.0 / 4.8, 21.0 / 8, 17.0 / 4.8, 180, false, new int[]{4, 10});
        if(digit.length < 2){
            return result;
        }
        final var offset = digit[0] * 10 + digit[1];
        // 艦隊番号を読み取る
        for(int i = 0; i < 8; ++i){
            // フラッグが存在するかをざっくり判断する
            final var pos1 = BitmapImage.of(image).calcAverageColor(533.0 / 8.0, (172.0 + 30.0 * i) / 4.8, 1.0 / 8.0, 1.0 / 4.8);
            final var temp1 = new Color(241, 234, 221); //背景
            final var temp2 = new Color(35, 159, 160);  //緑
            final var temp3 = new Color(255, 246, 242);  //白
            final var dist1 = Utility.calcColorDistance(pos1, temp1);   //背景からの距離
            final var dist2 = Utility.calcColorDistance(pos1, temp2);   //緑からの距離
            if(dist1 <= dist2)
                continue;
            final var num = offset + i;
            var numStr = (num < 10 ? "0" : "") + num;
            if(offset <= 4 && num > 8){
                numStr = "A" + (num - 8);
            }else if(offset <= 11 && num > 16){
                numStr = "B" + (num - 16);
            }
            // 艦隊番号を判断する
            final var crop  = BitmapImage.of(image).crop(517.0 / 8.0, (165.0 + 30.0 * i) / 4.8,18.0 / 8.0, 22.0 / 4.8);
            if(debugFlg) crop.save("pic/temp-exp" + i + "-1.png");
            final var threshold = crop.threshold(temp3, 320);
            if(debugFlg) threshold.save("pic/temp-exp" + i + "-2.png");
            // 数字認識を行う
            // 周囲をトリミング
            final var crop2 = threshold.crop(threshold.calcTrimmingRect());
            if(debugFlg) crop2.save("pic/temp-exp-" + i + "-3.png");
            // 指定したサイズに拡大
            final var fixed = crop2.resize(ocrStretchHeight2, ocrStretchHeight2);
            if(debugFlg) fixed.save("pic/temp-exp-" + i + "-4.png");
            // テンプレと比較し、尤もらしい値を推定値とする
            final var template_ = template.stream().filter(pair -> pair.getValue() >= 2 && pair.getValue() <= 4).collect(Collectors.toList());
            int minDiff = fixed.getSSD(template_.get(0).getKey());
            int selectIndex = template_.get(0).getValue();
            for(int j = 1; j < template_.size(); ++j){
                int diff = fixed.getSSD(template_.get(j).getKey());
                if(minDiff > diff){
                    minDiff = diff;
                    selectIndex = template_.get(j).getValue();
                }
            }
            result.put(selectIndex, numStr);
        }
        return result;
    }
    /** 選択されている遠征の遠征IDを取り出す */
    public static String getSelectedExpeditionId(BufferedImage image){
        final var hash = BitmapImage.of(image).calcDifferenceHash(577.0 / 8, 104.0 / 4.8, 103.0 / 8, 19.0 / 4.8);
        String fleetId = "";
        var minDiff = Long.MAX_VALUE;
        for(var pair : expeditionDataMap.entrySet()){
            final var diff = Utility.calcHummingDistance(hash, pair.getValue().getValue());
            if(minDiff > diff){
                fleetId = pair.getKey();
                minDiff = diff;
            }
        }
        return fleetId;
    }
    /** 指定した遠征IDの遠征の名前を取り出す */
    public static  String getExpeditionNameById(String id){
        if(expeditionDataMap.containsKey(id)){
            return expeditionDataMap.get(id).getKey();
        }else{
            return "？";
        }
    }
}
