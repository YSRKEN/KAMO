package com.ysrken.kamo.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ysrken.kamo.MainApp;
import com.ysrken.kamo.service.ExtraStage;
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
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class HelloController
{
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private Label messageLabel;

    @Autowired
    private TestService testService;
    
    public void sayHello() {

        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();

        messageLabel.setText(testService.joinName(firstName, lastName));
    }
    
    public void addWindow() throws IOException {
    	ExtraStage slaveStage = new ExtraStage(new Stage(), "/fxml/hello.fxml") {{
        	setTitle("Slave Window");
        	setWidth(400);
        	setHeight(300);
        }};
        slaveStage.show();
    }
}
