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

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

@ComponentScan
public class MainApp extends Application {

	@Autowired
	private LoggerService loggerService;

    private static ConfigurableApplicationContext context;
    
    /**
     * main�֐�
     * @param args �R�}���h���C������
     * @throws Exception ���s����O
     */
    public static void main(String[] args) throws Exception {
    	context = new AnnotationConfigApplicationContext(MainApp.class);
        launch(args);
    }

    /**
     * JavaFX�̋N������
     * @param stage Stage���
     * @throws Exception ���s����O
     */
    public void start(Stage stage) throws Exception {

    	//loggerService.info("Starting Hello JavaFX and Maven demonstration application");

        String fxmlFile = "/fxml/hello.fxml";
        //loggerService.debug("Loading FXML for main view from: {}", fxmlFile);
        FXMLLoader loader = new FXMLLoader();
        loader.setControllerFactory(context::getBean);
        Parent rootNode = (Parent) loader.load(getClass().getResourceAsStream(fxmlFile));

        //loggerService.debug("Showing JFX scene");
        Scene scene = new Scene(rootNode, 400, 200);
        scene.getStylesheets().add("/styles/styles.css");

        stage.setTitle("Hello JavaFX and Maven");
        stage.setScene(scene);
        stage.show();
    }
    
    /**
     * JavaFX�̏I������
     * @throws Exception ���s����O
     */
    @Override
    public void stop() throws Exception {
        context.close();
    }
}
