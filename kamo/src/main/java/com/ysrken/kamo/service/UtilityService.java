package com.ysrken.kamo.service;

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
}
