package com.ysrken.kamo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ysrken.kamo.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class SettingService {
	private Map<String, Object> setting = new HashMap<>();

	@Autowired
	private LoggerService logger;
	@Autowired
	private UtilityService utility;
	
	/**
	 * 最終保存日時
	 */
    private Date lastSaveDate = new Date();

    /**
     * 保存するべきか？
     */
    private boolean saveFlg = false;
	
    /**
     * コンストラクタ
     */
	public SettingService() {
		System.out.println("DEBUG MainApp - SettingService#SettingService");
		// 設定を読み込み
		loadSetting();
		
		// 自動セーブ設定
        final Timer saveTimer = new Timer();
        saveTimer.schedule(new SaveTask(() -> saveSetting()), 0, Constant.SAVE_SETTING_INTERVAL);
	}
	
	/**
	 * 設定を編集する
	 */
	public void setSetting(String key, Object value) {
		// 書き込む
		setting.put(key, value);
		
		// セーブフラグを立てておく
		saveFlg = true;
	}
	
	/**
	 * 設定を読み込む
	 */
	@SuppressWarnings("unchecked")
	public <T> T getSetting(String key) {
		if(setting.containsKey(key)) {
			// 既にその設定項目が存在する部分は、その値を読み込む
			return (T)setting.get(key);
		}else {
			// その設定項目が存在しない部分は、デフォルト値を読み込む
			return getDefaultSetting(key);
		}
	}
	
	/**
	 * デフォルト設定を返す
	 */
	@SuppressWarnings("unchecked")
	public <T> T getDefaultSetting(String key) {
		switch(key) {
		
		// 各画面の座標・大きさ
		case "MainWindow":
			return (T)(new ArrayList<Double>(Arrays.asList(Double.NaN, Double.NaN, 400.0, 250.0)));
		case "BattleSceneReflectionWindow":
			return (T)(new ArrayList<Double>(Arrays.asList(Double.NaN, Double.NaN, 400.0, 300.0)));
		case "TimerWindow":
			return (T)(new ArrayList<Double>(Arrays.asList(Double.NaN, Double.NaN, 400.0, 300.0)));
		case "SceneHelperWindow":
			return (T)(new ArrayList<Double>(Arrays.asList(Double.NaN, Double.NaN, 400.0, 300.0)));
		// 遠征
		case "ExpTimer0":
			return (T)(new ArrayList<String>(Arrays.asList(utility.DateToISO8601(new Date()), "？")));
		case "ExpTimer1":
			return (T)(new ArrayList<String>(Arrays.asList(utility.DateToISO8601(new Date()), "？")));
		case "ExpTimer2":
			return (T)(new ArrayList<String>(Arrays.asList(utility.DateToISO8601(new Date()), "？")));
		case "ExpTimer3":
			return (T)(new ArrayList<String>(Arrays.asList(utility.DateToISO8601(new Date()), "？")));
		// その他設定項目
		case "AutoGetPositionFlg":
			return (T)(Boolean.FALSE);
		case "BlindNameTextFlg":
			return (T)(Boolean.FALSE);
		case "SpecialGetPosFlg":
			return (T)(Boolean.FALSE);
		case "SaveWindowPositionFlg":
			return (T)(Boolean.FALSE);
		default:
			throw new IllegalArgumentException();
		}
	}
	
	/**
	 * 設定を読み込む
	 */
	public void loadSetting() {
		System.out.println("DEBUG MainApp - SettingService#loadSetting");
		try {
			// ファイルを読み込む
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(new File("setting.json"));
			
			// 各項目を読み込み、Mapに登録する
			Iterator<String> keys = root.fieldNames();
			while (keys.hasNext()) {
	            String key = keys.next();
	            JsonNode value = root.get(key);
	            setting.put(key, mapper.readValue(value.toString(), Object.class));
	        }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 設定を保存する
	 */
	public void saveSetting() {
		try (FileOutputStream fos = new FileOutputStream("setting.json");
				OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
				BufferedWriter writer = new BufferedWriter(osw)) {

			// ファイルに書き込む
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			mapper.writeValue(writer, setting);
			logger.debug("Save Settings");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    /**
     * 自動保存用のタスク
     */
    private class SaveTask extends TimerTask {
    	private Runnable saveSettings;
    	
    	/**
    	 * コンストラクタ
    	 * @param saveSettings 保存コマンド
    	 */
    	public SaveTask(Runnable saveSettings) {
    		this.saveSettings = saveSettings;
    	}
    	
    	/**
    	 * 保存操作
    	 */
        @Override
        public void run() {
        	// 現在時刻を取得する
        	Date date = new Date();
        	
        	// セーブフラグが立っており、最後に保存した時刻からSAVE_SETTING_INTERVALミリ秒以上経過していれば保存する
            if(saveFlg && (date.getTime() - lastSaveDate.getTime() >= Constant.SAVE_SETTING_INTERVAL)){
                saveSettings.run();
                saveFlg = false;
                lastSaveDate = date;
            }
        }
    }
}
