package com.ysrken.kamo.Service;

import com.ysrken.kamo.BitmapImage;
import com.ysrken.kamo.Utility;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static java.awt.Image.SCALE_SMOOTH;

/** 画面のシーン判定を行う */
public class SceneRecognitionService {
    /** 画像がそのシーンたりうる証拠 */
    static interface SceneEvidence{
        /**
         * 画像がその証拠とマッチするかを判定する
         * @param image 入力画像
         * @return マッチすればtrue
         */
        public boolean isMatchImage(BufferedImage image);
    }
    /** 画像がそのシーンたりうる証拠(DifferenceHash版 */
    static class SceneEvidenceDH implements SceneEvidence{
        private double xPer, yPer, wPer, hPer;
        private long hash;
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
            final var hash = BitmapImage.of(image).calcDifferenceHash(xPer, yPer, wPer, hPer);
            return Utility.calcHummingDistance(this.hash, hash) < 20;
        }
    }
    /** 画像がそのシーンたりうる証拠(AverageColor版) */
    static class SceneEvidenceAC implements SceneEvidence{
        private double xPer, yPer, wPer, hPer;
        private Color color;
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
            this.color = new Color(r, g, b);
        }
        /**
         * 画像がその証拠とマッチするかを判定する
         * @param image 入力画像
         * @return マッチすればtrue
         */
        public boolean isMatchImage(BufferedImage image){
            final var color = BitmapImage.of(image).calcAverageColor(xPer, yPer, wPer, hPer);
            return Utility.calcColorDistance(this.color, color) < 50;
        }
    }

    /** シーン一覧 */
    private static Map<String, SceneEvidence[]> sceneList = new HashMap<String, SceneEvidence[]>();
    private static SceneEvidence[] nearlyHomeScene = new SceneEvidence[]{};

    /** 初期化コード */
    public static void initialize(){
        // JSONを読み込む
        // 参考→https://symfoware.blog.fc2.com/blog-entry-2094.html
        final var manager = new ScriptEngineManager();
        final var engine = manager.getEngineByName("javascript");
        final BiFunction<ScriptObjectMirror, String, Double> toDouble = ((so, key) -> {
            try {
                return (Double)engine.eval((String)so.get(key));
            } catch (ScriptException e) {
                e.printStackTrace();
                return 0.0;
            }
        });
        try(final var is = ClassLoader.getSystemResourceAsStream("com/ysrken/kamo/File/SceneParameter.json");
            final var isr = new InputStreamReader(is, Charset.forName("UTF-8"));
            final var br = new BufferedReader(isr)){
            // テキストデータを用意
            final var jsonText = br.lines().collect(Collectors.joining());
            // テキストデータをJSONとしてパース
            final var json = (ScriptObjectMirror) engine.eval("JSON");
            final var result = json.callMember("parse", jsonText);
            // mapに変換
            final var m =  (ScriptObjectMirror)result;
            // 各シーン毎に処理
            for(var pair : m.to(ScriptObjectMirror[].class)){
                final var evidenceList = new ArrayList<SceneEvidence>();
                final var sceneName = (String)pair.get("name");
                // DifferenceHashについての処理
                final var differenceHashList = ((ScriptObjectMirror)pair.get("differenceHash")).to(ScriptObjectMirror[].class);
                for(var differenceHash : differenceHashList){
                    final var xPer = toDouble.apply(differenceHash, "xPer");
                    final var yPer = toDouble.apply(differenceHash, "yPer");
                    final var wPer = toDouble.apply(differenceHash, "wPer");
                    final var hPer = toDouble.apply(differenceHash, "hPer");
                    final var hash = Long.parseUnsignedLong((String)differenceHash.get("hash"), 16);
                    final var data = new SceneEvidenceDH(xPer, yPer, wPer, hPer, hash);
                    evidenceList.add(data);
                }
                // AverageColorについての処理
                final var averageColorList =  ((ScriptObjectMirror)pair.get("averageColor")).to(ScriptObjectMirror[].class);
                for(var averageColor : averageColorList){
                    final var xPer = toDouble.apply(averageColor, "xPer");
                    final var yPer = toDouble.apply(averageColor, "yPer");
                    final var wPer = toDouble.apply(averageColor, "wPer");
                    final var hPer = toDouble.apply(averageColor, "hPer");
                    final var r = (Integer)averageColor.get("r");
                    final var g = (Integer)averageColor.get("g");
                    final var b = (Integer)averageColor.get("b");
                    final var data = new SceneEvidenceAC(xPer, yPer, wPer, hPer, r, g, b);
                    evidenceList.add(data);
                }
                // 特殊処理
                if(sceneName.equals("ほぼ母港")){
                    nearlyHomeScene = evidenceList.toArray(new SceneEvidence[0]);
                }else {
                    sceneList.put(sceneName, evidenceList.toArray(new SceneEvidence[0]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        } catch (ScriptException e) {
            e.printStackTrace();
        }
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
    /**
     * 「ほぼ母港画面」であるかを判定する。ここで「ほぼ母港画面」とは、
     * 母港画面などにある画面上の構造物(名前・資源表示・各種メニュー)が
     * 見えている全ての状況を指す
     * @param frame 画像
     */
    public static boolean isNearlyHomeScene(BufferedImage frame){
        return Arrays.stream(nearlyHomeScene).allMatch(se -> se.isMatchImage(frame));
    }
    /** 画像の情報を算出して返す */
    public static void testSceneRecognition(BufferedImage image){
        final var scene = SceneRecognitionService.judgeScene(image);
        final var isNearlyHomeFlg = SceneRecognitionService.isNearlyHomeScene(image);
        var contentText = String.format("シーン判定：%s%nほぼ母港か？：%s", scene.isEmpty() ? "不明" : scene, isNearlyHomeFlg ? "Yes" : "No");
        if(scene.equals("遠征一覧") || scene.equals("遠征中止")){
            final var duration = CharacterRecognitionService.getExpeditionRemainingTime(image);
            if(duration >= 0) {
                contentText += String.format("%n残り時間：%s", Utility.LongToDateStringShort(duration));
            }else{
                contentText += "%n残り時間：不明";
            }
            final var result = CharacterRecognitionService.getExpeditionFleetId(image);
            if(result.size() > 0){
                contentText += String.format("%n遠征艦隊番号：");
                for(var pair : result.entrySet()){
                    contentText += String.format("%n　第%d艦隊→%s", pair.getKey(), pair.getValue());
                }
            }
            final var expeditionId = CharacterRecognitionService.getSelectedExpeditionId(image);
            contentText += String.format("%n遠征ID：%s", expeditionId);
        }
        Utility.showDialog(contentText, "画像認識結果");
    }
}
