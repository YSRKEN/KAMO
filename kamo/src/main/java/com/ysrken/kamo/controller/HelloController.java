package com.ysrken.kamo.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ysrken.kamo.service.TestService;
import com.ysrken.kamo.stage.ExtraStage;
import com.ysrken.kamo.stage.ExtraStageFactory;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Helloコントローラー
 * @author ysrken
 */
@Component
@Scope(org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE)
public class HelloController
{
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private Label messageLabel;
    @FXML private Button addWindowButton;

    @Autowired
    private TestService testService;
    
    @Autowired
    private ExtraStageFactory factory;
        
    // 開かれる新規ウィンドウ
    private ObjectProperty<ExtraStage> slaveStage = new SimpleObjectProperty<>(null);
    
    /**
     * コンストラクタ
     */
    public HelloController() {
    	slaveStage.addListener((ob, o, n) -> {
    		addWindowButton.disableProperty().set(n != null);
    	});
    }
    
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
    	if(slaveStage.get() == null) {
    		slaveStage.set(factory.create("/fxml/hello.fxml", "SlaveWindow"));
    		slaveStage.get().setTitle("Slave Window");
    		addWindowButton.disableProperty().set(true);
    		slaveStage.get().setOnCloseRequest(() -> {
    			slaveStage.set(null);
    		});
    		slaveStage.get().show();
    	}
    }
}
