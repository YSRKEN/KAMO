package com.ysrken.kamo.service;

import org.springframework.stereotype.Component;

import com.ysrken.kamo.MainApp;

import javafx.stage.Stage;

@Component
public class ExtraStageFactory {
	
	public ExtraStage create(Stage stage, String fxmlPath) {
		return MainApp.getApplicationContext().getBean(ExtraStage.class, stage, fxmlPath);
	}
}
