package com.ysrken.kamo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.ysrken.kamo.control.ExtraStage;
import com.ysrken.kamo.service.LoggerService;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * 起動用クラス
 */
@ComponentScan
public class MainApp extends Application {

	private LoggerService loggerService = new LoggerService();

    private static ConfigurableApplicationContext context;
    
    /**
     * main関数
     * @param args コマンドライン引数
     * @throws Exception 実行時例外
     */
    public static void main(String[] args) throws Exception {
    	context = new AnnotationConfigApplicationContext(MainApp.class);
        launch(args);
    }

    /**
     * JavaFXの起動処理
     * @param stage Stage情報
     * @throws Exception 実行時例外
     */
    public void start(Stage stage) throws Exception {

    	loggerService.info("Starting Hello JavaFX and Maven demonstration application");
        String fxmlFile = "/fxml/hello.fxml";
        loggerService.debug("Loading FXML for main view from: {}", fxmlFile);
        ExtraStage mainStage = new ExtraStage(stage, fxmlFile) {{
        	setTitle("Hello JavaFX and Maven");
        	setWidth(400);
        	setHeight(300);
        }};
        mainStage.show();
    }
    
    /**
     * JavaFXの終了処理
     * @throws Exception 実行時例外
     */
    @Override
    public void stop() throws Exception {
        context.close();
    }
}
