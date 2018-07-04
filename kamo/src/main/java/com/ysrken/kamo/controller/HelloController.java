package com.ysrken.kamo.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ysrken.kamo.service.ExtraStageFactory;
import com.ysrken.kamo.service.ExtraStage;
import com.ysrken.kamo.service.TestService;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Helloコントローラー
 * @author ysrken
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
    
    @Autowired
    private ExtraStageFactory factory;
    
    /**
     * ボタン操作1
     */
    public void sayHello() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        messageLabel.setText(testService.joinName(firstName, lastName));
    }
    
    /**
     * ボタン操作2
     * @throws IOException FXMLファイルを読み込めない際に発生
     */
    public void addWindow() throws IOException {
		ExtraStage slaveStage = factory.create("/fxml/hello.fxml");
    	slaveStage.setTitle("Slave Window");
    	slaveStage.setWidth(400);
    	slaveStage.setHeight(300);
        slaveStage.show();
    }
}
