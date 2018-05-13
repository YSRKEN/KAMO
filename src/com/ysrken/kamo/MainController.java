package com.ysrken.kamo;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;

public class MainController {
    @FXML private MenuItem ExitCommand;

    public void initialize(){
        // 終了コマンド
        ExitCommand.setOnAction(e -> System.exit(0));

    }

}
