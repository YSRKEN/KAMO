package com.ysrken.kamo.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ysrken.kamo.service.SettingService;
import com.ysrken.kamo.service.TestService;
import com.ysrken.kamo.stage.ExtraStage;
import com.ysrken.kamo.stage.ExtraStageFactory;

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
        
    // 開かれる新規ウィンドウ
    private ExtraStage slaveStage = null;
    
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
    	if(slaveStage == null) {
    		slaveStage = factory.create("/fxml/hello.fxml");
    		slaveStage.setTitle("Slave Window");
    		slaveStage.setWidth(400);
    		slaveStage.setHeight(300);
    		slaveStage.show();
    	}
    }
}
