package com.ysrken.kamo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloController
{
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private Label messageLabel;

    private static final Logger log = LoggerFactory.getLogger(HelloController.class);
    private TestService testService = new TestService();
    
    public void sayHello() {

        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();

        messageLabel.setText(testService.joinName(firstName, lastName, log));
    }

}
