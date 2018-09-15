package com.ysrken.kamo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ysrken.kamo.BitmapImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
            return utility.calcHummingDistance(this.hash, hash) < 20;
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
            return utility.calcColorDistance(this.color, color) < 50;
        }
    }

    /**
     * シーン一覧
     */
    private Map<String, SceneEvidence[]> homeSceneList = new HashMap<String, SceneEvidence[]>();
    private Map<String, SceneEvidence[]> otherSceneList = new HashMap<String, SceneEvidence[]>();

    public SceneRecognitionService() {
    	System.out.println("DEBUG MainApp - SceneRecognitionService#SceneRecognitionService");
    }
    
    /**
     * 初期化
     * @throws IOException 
     * @throws JsonProcessingException 
     */
    public void initialize() throws JsonProcessingException, IOException {
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
                    homeSceneList.put(sceneName, evidenceList.toArray(new SceneEvidence[0]));
                }else{
                    otherSceneList.put(sceneName, evidenceList.toArray(new SceneEvidence[0]));
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
        for(Map.Entry<String, SceneEvidence[]> e : otherSceneList.entrySet()){
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
        for(Map.Entry<String, SceneEvidence[]> e : homeSceneList.entrySet()){
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

        String contentText = String.format("シーン判定：%s%nほぼ母港か？：%s", scene.isEmpty() ? "[不明]": scene, homeType.isEmpty() ? "No" : "Yes(" + homeType + ")");
        if(scene.equals("遠征個別") || scene.equals("遠征中止")){
            final long duration = characterRecognition.getExpeditionRemainingTime(image);
            if(duration >= 0) {
                contentText += String.format("%n残り時間：%s", utility.getDateStringShortFromLong(duration));
            }else{
                contentText += String.format("%n残り時間：不明");
            }
            final Map<Integer, String> result = characterRecognition.getExpeditionFleetId(image);
            if(result.size() > 0){
                contentText += String.format("%n遠征艦隊番号：");
                for(Map.Entry<Integer, String> pair : result.entrySet()){
                    contentText += String.format("%n　第%d艦隊→%s", pair.getKey(), pair.getValue());
                }
            }
            final String expeditionId = characterRecognition.getSelectedExpeditionId(image);
            contentText += String.format("%n遠征ID：%s", expeditionId);
        }
        utility.showDialog(contentText, "画像認識結果");
    }
}
