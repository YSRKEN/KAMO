package com.ysrken.kamo.service;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ysrken.kamo.model.Setting;

import lombok.Getter;
import lombok.Setter;

@Component
public class SettingService {
	@Getter
	private Setting setting = new Setting();

	@Autowired
	private LoggerService logger;
	
	/**
	 * 最終保存日時
	 */
    private Date lastSaveDate = new Date();
    /**
     * 保存するべきか？
     */
    @Setter
    private boolean saveFlg = false;
	
    /**
     * コンストラクタ
     */
	public SettingService() {
		// 自動セーブ設定
        final Timer saveTimer = new Timer();
        saveTimer.schedule(new SaveTask(() -> saveSetting()), 0, 1000);
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
