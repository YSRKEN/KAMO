package com.ysrken.kamo;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;

import com.ysrken.kamo.controller.HelloController;
import com.ysrken.kamo.controller.MainController;
import com.ysrken.kamo.model.MainModel;
import com.ysrken.kamo.service.LoggerService;
import com.ysrken.kamo.service.SettingService;
import com.ysrken.kamo.service.TestService;
import com.ysrken.kamo.service.UtilityService;
import com.ysrken.kamo.stage.ExtraStageFactory;
import com.ysrken.kamo.stage.ExtraStageImpl;

import javafx.stage.Stage;

@Configuration
public class AppConfig {
	public MainController getMainController() {
		return new MainController();
	}

	public HelloController getHelloController() {
		return new HelloController();
	}

	public MainModel getMainModel() {
		return new MainModel();
	}

	public LoggerService getLoggerService() {
		return new LoggerService();
	}

	public SettingService getSettingService() {
		return new SettingService();
	}

	public UtilityService getUtilityService() {
		return new UtilityService();
	}
	
	public TestService getTestService() {
		return new TestService();
	}

	public ExtraStageFactory getExtraStageFactory() {
		return new ExtraStageFactory();
	}

	public ExtraStageImpl getExtraStageImpl(Stage stage, String fxmlPath, String keyWord) throws IOException {
		return new ExtraStageImpl(stage, fxmlPath, keyWord);
	}
}
