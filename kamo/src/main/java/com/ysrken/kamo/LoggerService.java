package com.ysrken.kamo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * ���M���O������Bean
 * @author ysrke
 */
@Component
public class LoggerService {
	private final Logger log = LoggerFactory.getLogger(MainApp.class);
	
	/**
	 * �f�o�b�O���b�Z�[�W���c��
	 * @param message
	 */
	public void debug(String message) {
		this.log.debug(message);
	}
	
	/**
	 * �C���t�H���[�V�������b�Z�[�W���c��
	 * @param message
	 */
	public void info(String message) {
		this.log.info(message);
	}

	/**
	 * �f�o�b�O���b�Z�[�W���c��
	 * @param arg0
	 * @param arg1
	 */
	public void debug(String arg0, String arg1) {
		this.log.debug(arg0, arg1);
	}
}
