package com.ysrken.kamo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ysrken.kamo.BitmapImage;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 画面のシーン判定を行う
 * @author ysrken
 */
@Component
public class SceneRecognitionService {

	/**
	 * 各種サービス
	 */
	@Autowired
    private UtilityService utility;
	@Autowired
    private CharacterRecognitionService characterRecognition;
	
	/**
	 * 画像がそのシーンたりうる証拠
	 */
    private interface SceneEvidence{
        /**
         * 画像がその証拠とマッチするかを判定する
         * @param image 入力画像
         * @return マッチすればtrue
         */
        public boolean isMatchImage(BufferedImage image);
    }
    
    /**
     * 画像がそのシーンたりうる証拠(DifferenceHash版
     */
    private class SceneEvidenceDH implements SceneEvidence{
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
            final long hash = BitmapImage.of(image).calcDifferenceHash(xPer, yPer, wPer, hPer);
            System.out.println(utility.calcHummingDistance(this.hash, hash));
            return utility.calcHummingDistance(this.hash, hash) < 25;
        }
    }
    
    /**
     * 画像がそのシーンたりうる証拠(AverageColor版)
     */
    private class SceneEvidenceAC implements SceneEvidence{
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
            final Color color = BitmapImage.of(image).calcAverageColor(xPer, yPer, wPer, hPer);
            return utility.calcColorDistance(this.color, color) < 150;
        }
    }

    /**
     * シーン一覧
     */
    private List<Pair<String, SceneEvidence[]>> homeSceneList = new ArrayList<>();
    private List<Pair<String, SceneEvidence[]>> otherSceneList = new ArrayList<>();

    public SceneRecognitionService() {
    	System.out.println("DEBUG MainApp - SceneRecognitionService#SceneRecognitionService");
    }
    
    /**
     * 初期化
     */
    public void initialize() {
    	System.out.println("DEBUG MainApp - SceneRecognitionService#initialize");
    	
    	// JSONを読み込む
		ObjectMapper mapper = new ObjectMapper();
        try(final InputStream is = ClassLoader.getSystemResourceAsStream("scene_parameter.json")) {
            JsonNode root = mapper.readTree(is);
            for(JsonNode evidenceJsonData: root) {
                // 保存用の配列を用意する
                final ArrayList<SceneEvidence> evidenceList = new ArrayList<>();

                // シーンの名称を読み込む
                final String sceneName = evidenceJsonData.get("name").textValue();

                // DifferenceHashについての処理
                for(JsonNode differenceHash: evidenceJsonData.get("differenceHash")) {
                    final double xPer = utility.parseFormula(differenceHash.get("xPer").textValue());
                    final double yPer = utility.parseFormula(differenceHash.get("yPer").textValue());
                    final double wPer = utility.parseFormula(differenceHash.get("wPer").textValue());
                    final double hPer = utility.parseFormula(differenceHash.get("hPer").textValue());
                    final long hash = Long.parseUnsignedLong(differenceHash.get("hash").textValue(), 16);
                    final SceneEvidenceDH data = new SceneEvidenceDH(xPer, yPer, wPer, hPer, hash);
                    evidenceList.add(data);
                }

                // AverageColorについての処理
                for(JsonNode averageColor: evidenceJsonData.get("averageColor")) {
                    final double xPer = utility.parseFormula(averageColor.get("xPer").textValue());
                    final double yPer = utility.parseFormula(averageColor.get("yPer").textValue());
                    final double wPer = utility.parseFormula(averageColor.get("wPer").textValue());
                    final double hPer = utility.parseFormula(averageColor.get("hPer").textValue());
                    final int r = averageColor.get("r").intValue();
                    final int g = averageColor.get("g").intValue();
                    final int b = averageColor.get("b").intValue();
                    final SceneEvidenceAC data = new SceneEvidenceAC(xPer, yPer, wPer, hPer, r, g, b);
                    evidenceList.add(data);
                }

                if (sceneName.matches("ほぼ母港.*")){
                    homeSceneList.add(new Pair<>(sceneName, evidenceList.toArray(new SceneEvidence[0])));
                }else{
                    otherSceneList.add(new Pair<>(sceneName, evidenceList.toArray(new SceneEvidence[0])));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * シーン判定を行う
     * @param frame スクショ
     * @return シーンを表す文字列
     */
    public String judgeScene(BufferedImage frame){
        for(Pair<String, SceneEvidence[]> e : otherSceneList){
            SceneEvidence[] seList = e.getValue();
            boolean flg = true;
            for(SceneEvidence se : seList){
                if (!se.isMatchImage(frame)){
                    flg = false;
                    break;
                }
            }
            if (flg){
                return e.getKey();
            }
        }
        return "";
    }
    
    /**
     * 「ほぼ母港画面」であるかを判定する。ここで「ほぼ母港画面」とは、
     * 母港画面などにある画面上の構造物(名前・資源表示・各種メニュー)が
     * 見えている全ての状況を指す
     * @param frame スクショ
     * @return 母港の書類を表す文字列
     */
    public String judgeHomeType(BufferedImage frame){
        for(Pair<String, SceneEvidence[]> e : homeSceneList){
            System.out.println(e.getKey());
            SceneEvidence[] seList = e.getValue();
            boolean flg = true;
            for(SceneEvidence se : seList){
                if (!se.isMatchImage(frame)){
                    flg = false;
                    break;
                }
            }
            if (flg){
                return e.getKey().replaceAll("ほぼ母港\\((.+)\\)", "$1");
            }
        }
        return "";
    }

    /**
     * 画像の情報を算出して返す
     */
    public void testSceneRecognition(BufferedImage image){
        final String scene = judgeScene(image);
        final String homeType = judgeHomeType(image);

        StringBuilder contentText = new StringBuilder(String.format("シーン判定：%s%nほぼ母港か？：%s", scene.isEmpty() ? "[不明]" : scene, homeType.isEmpty() ? "No" : "Yes(" + homeType + ")"));
        if(scene.equals("遠征個別") || scene.equals("遠征中止")){
            final long duration = characterRecognition.getExpeditionRemainingTime(image);
            if(duration >= 0) {
                contentText.append(String.format("%n残り時間：%s", UtilityService.getDateStringShortFromLong(duration)));
            }else{
                contentText.append(String.format("%n残り時間：不明"));
            }
            final Map<Integer, String> result = characterRecognition.getExpeditionFleetId(image);
            if(result.size() > 0){
                contentText.append(String.format("%n遠征艦隊番号："));
                for(Map.Entry<Integer, String> pair : result.entrySet()){
                    contentText.append(String.format("%n　第%d艦隊→%s", pair.getKey(), pair.getValue()));
                }
            }
            final String expeditionId = characterRecognition.getSelectedExpeditionId(image);
            contentText.append(String.format("%n遠征ID：%s", expeditionId));
        }
        if (scene.equals("MVP")) {
            boolean hardDamageFlg = judgeHardDamage(image);
            contentText.append(String.format("%n大破艦がいるか？：%s", (hardDamageFlg ? "Yes" : "No")));
        }
        utility.showDialog(contentText.toString(), "画像認識結果");
    }

    /**
     * MVP画面を見て、大破艦が存在していた場合はtrue
     * @param image MVP画面の画像
     */
    public boolean judgeHardDamage(BufferedImage image) {
        final Color color1Sample = new Color(236, 94, 92);
        final Color color2Sample = new Color(237, 99, 98);
        List<Boolean> hardDamageFlg = IntStream.range(0, 6)
                .boxed().map(index -> {
                    final double posX1Per = 1.0 * 522 / 12;
                    final double posY1Per = 1.0 * (337 + index * 68) / 7.2;
                    final double posX2Per = 1.0 * 490 / 12;
                    final double posY2Per = 1.0 * (287 + index * 68) / 7.2;
                    final Color color1 = BitmapImage.of(image).calcAverageColor(posX1Per, posY1Per, 2.0 / 12, 2.0 / 7.2);
                    final Color color2 = BitmapImage.of(image).calcAverageColor(posX2Per, posY2Per, 2.0 / 12, 2.0 / 7.2);
                    //System.out.println(color1.toString() + " " + utility.calcColorDistance(color1, color1Sample));
                    //System.out.println(color2.toString() + " " + utility.calcColorDistance(color2, color2Sample) + "\n");
                    return (utility.calcColorDistance(color1, color1Sample) < 500 && utility.calcColorDistance(color2, color2Sample) < 500);
                }).collect(Collectors.toList());
        //System.out.println("hardDamageFlg : " + hardDamageFlg.get(0) + " " + hardDamageFlg.get(1) + " " + hardDamageFlg.get(2) + " " + hardDamageFlg.get(3) + " " + hardDamageFlg.get(4) + " " + hardDamageFlg.get(5) + "");
        return hardDamageFlg.stream().anyMatch(flg -> flg);
    }
}
