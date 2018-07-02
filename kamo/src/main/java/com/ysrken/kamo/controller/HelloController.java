package com.ysrken.kamo.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import com.ysrken.kamo.MainApp;
import com.ysrken.kamo.service.TestService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Helloコントローラー
 */
@Component
public class HelloController
{
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private Label messageLabel;

    private static final Logger log = LoggerFactory.getLogger(HelloController.class);
    
    @Autowired
    private TestService testService;
    
    public void sayHello() {

        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();

        messageLabel.setText(testService.joinName(firstName, lastName));
    }
    
    public void addWindow() {
    	ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(MainApp.class);
        Stage stage = new Stage();
        String fxmlFile = "/fxml/hello.fxml";
        FXMLLoader loader = new FXMLLoader();
        loader.setControllerFactory(context::getBean);
        Parent rootNode;
		try {
			rootNode = (Parent) loader.load(getClass().getResourceAsStream(fxmlFile));
	        Scene scene = new Scene(rootNode, 400, 200);
	        scene.getStylesheets().add("/styles/styles.css");
	        stage.setTitle("Slave Window");
	        stage.setScene(scene);
	        stage.show();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
    }
}
