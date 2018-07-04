package com.ysrken.kamo.service;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ysrken.kamo.model.Setting;

import lombok.Getter;

@Component
public class SettingService {
	@Getter
	private Setting setting = new Setting();

	@Autowired
	private LoggerService logger;
	
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
}
