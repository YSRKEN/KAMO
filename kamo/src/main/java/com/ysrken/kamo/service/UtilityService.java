package com.ysrken.kamo.service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

import org.springframework.stereotype.Component;

@Component
public class UtilityService {
	/**
	 * Windows OS上で動作しているかを判定
	 */
    public boolean isWindows(){
        final String osName = System.getProperty("os.name").toLowerCase();
        return (osName.startsWith("windows"));
    }
    
    /**
     * 現在の日時を「03:04:05」形式で取得
     * @return 現在の日時文字列
     */
    private final DateTimeFormatter dtfShort = DateTimeFormatter.ofPattern("HH:mm:ss");
    public String getDateStringShort(){
        return LocalDateTime.now().format(dtfShort);
    }
}
