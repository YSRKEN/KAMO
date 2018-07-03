package com.ysrken.kamo.controller;

import java.io.IOException;

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
    	ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(MainApp.class);
		ExtraStage slaveStage = context.getBean(ExtraStage.class, new Stage(), "/fxml/hello.fxml");
    	slaveStage.setTitle("Slave Window");
    	slaveStage.setWidth(400);
    	slaveStage.setHeight(300);
        slaveStage.show();
    }
}
