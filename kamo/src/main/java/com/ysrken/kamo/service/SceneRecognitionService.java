package com.ysrken.kamo.service;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ysrken.kamo.BitmapImage;

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
    private Map<String, SceneEvidence[]> sceneList = new HashMap<String, SceneEvidence[]>();
    
    /**
     * 母港にいるととみなせるシーン一覧
     */
    private SceneEvidence[] nearlyHomeScene = new SceneEvidence[]{};

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
		JsonNode root = mapper.readTree(new File("D:\\自作ソフトウェア\\艦これモニタリングツール「KAMO」\\repository\\kamo\\src\\main\\resources\\scene_parameter.json"));
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
			
            // 特殊処理
            if(sceneName.equals("ほぼ母港")){
                nearlyHomeScene = evidenceList.toArray(new SceneEvidence[0]);
            }else {
                sceneList.put(sceneName, evidenceList.toArray(new SceneEvidence[0]));
            }
		}
    }

    /**
     * シーン判定を行う
     * @param frame スクショ
     * @return シーンを表す文字列
     */
    public String judgeScene(BufferedImage frame){
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
    public boolean isNearlyHomeScene(BufferedImage frame){
        return Arrays.stream(nearlyHomeScene).allMatch(se -> se.isMatchImage(frame));
    }
    
    /**
     * 画像の情報を算出して返す
     */
    public void testSceneRecognition(BufferedImage image){
        final String scene = judgeScene(image);
        final boolean isNearlyHomeFlg = isNearlyHomeScene(image);
        String contentText = String.format("シーン判定：%s%nほぼ母港か？：%s", scene.isEmpty() ? "不明" : scene, isNearlyHomeFlg ? "Yes" : "No");
        if(scene.equals("遠征一覧") || scene.equals("遠征中止")){
            /*final var duration = CharacterRecognitionService.getExpeditionRemainingTime(image);
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
            contentText += String.format("%n遠征ID：%s", expeditionId);*/
        }
        utility.showDialog(contentText, "画像認識結果");
    }
}
