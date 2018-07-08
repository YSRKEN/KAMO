package com.ysrken.kamo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ysrken.kamo.MainApp;

/**
 * ロギング処理のBean
 * @author ysrke
 */
@Component
public class LoggerService {
	private final Logger log = LoggerFactory.getLogger(MainApp.class);
	
	public LoggerService() {
		System.out.println("DEBUG MainApp - LoggerService#LoggerService");
	}
	
	/**
	 * デバッグメッセージを残す
	 * @param message
	 */
	public void debug(String message) {
		this.log.debug(message);
	}
	
	/**
	 * インフォメーションメッセージを残す
	 * @param message
	 */
	public void info(String message) {
		this.log.info(message);
	}

	/**
	 * デバッグメッセージを残す
	 * @param arg0
	 * @param arg1
	 */
	public void debug(String arg0, String arg1) {
		this.log.debug(arg0, arg1);
	}
}
