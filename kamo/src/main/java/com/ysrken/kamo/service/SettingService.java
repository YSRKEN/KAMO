package com.ysrken.kamo.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.Getter;
import lombok.Setter;

@Component
public class SettingService {
	private Map<String, Object> setting = new HashMap<>();

	@Autowired
	private LoggerService logger;
	
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
		// 設定を読み込み
		loadSetting();
		// 自動セーブ設定
        final Timer saveTimer = new Timer();
        saveTimer.schedule(new SaveTask(() -> saveSetting()), 0, 1000);
	}
	
	/**
	 * 設定を編集する
	 */
	public void setSetting(String key, Object value) {
		setting.put(key, value);
		saveFlg = true;
	}
	
	/**
	 * 設定を読み込む
	 */
	@SuppressWarnings("unchecked")
	public <T> T getSetting(String key) {
		if(setting.containsKey(key)) {
			return (T)setting.get(key);
		}else {
			//　デフォルト値を返す
			switch(key) {
			
			// 各画面の座標・大きさ
			case "MainWindow":
				return (T)(new double[] {0.0, 0.0, 0.0, 0.0});
			case "BattleSceneReflectionWindow":
				return (T)(new double[] {0.0, 0.0, 0.0, 0.0});
			case "TimerWindow":
				return (T)(new double[] {0.0, 0.0, 0.0, 0.0});
			case "SceneHelperWindow":
				return (T)(new double[] {0.0, 0.0, 0.0, 0.0});

			//　各画面の表示の有無
			case "OpenBattleSceneReflectionFlg":
				return (T)(Boolean.FALSE);
			case "OpenTimerFlg":
				return (T)(Boolean.FALSE);
			case "OpenSceneHelperFlg":
				return (T)(Boolean.FALSE);
				
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
	}
	
	/**
	 * 設定を読み込む
	 */
	public void loadSetting() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(new File("sample_setting.json"));
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
		try (FileOutputStream fos = new FileOutputStream("sample_setting.json");
				OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
				BufferedWriter writer = new BufferedWriter(osw)) {

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
    	 * @param saveTask 保存コマンド
    	 */
    	public SaveTask(Runnable saveSettings) {
    		this.saveSettings = saveSettings;
    	}
    	
    	/**
    	 * 保存操作
    	 */
        @Override
        public void run() {
        	Date date = new Date();
            if(saveFlg && (date.getTime() - lastSaveDate.getTime() >= 1000)){
                saveSettings.run();
                saveFlg = false;
                lastSaveDate = date;
            }
        }
    }
}
