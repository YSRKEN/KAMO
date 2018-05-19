package com.ysrken.kamo;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;

public class MainController {
    @FXML private MenuItem ExitMenu;
    @FXML private MenuItem GetPositionMenu;
    @FXML private MenuItem SaveScreenshotMenu;
    @FXML private MenuItem CheckVersionMenu;
    @FXML private MenuItem AboutMenu;
    @FXML private Button GetPositionButton;
    @FXML private Button SaveScreenshotButton;
    @FXML private TextArea MessageLogTextArea;

    private MainModel model;

    /**
     * ログにテキストを追加する
     * @param text
     */
    private void addLogText(String text){
        String allText = MessageLogTextArea.getText();
        allText += Utility.getDateStringShort() + " " + text + String.format("%n");
        MessageLogTextArea.setText(allText);
        MessageLogTextArea.setScrollTop(Double.POSITIVE_INFINITY);
    }
    /**
     * 初期化
     */
    public void initialize(){
        model = new MainModel(this::addLogText);
        // メソッドをコントロールに割り当てる
        ExitMenu.setOnAction(e -> model.exitCommand());
        CheckVersionMenu.setOnAction(e -> model.checkVersionCommand());
        GetPositionMenu.setOnAction(e -> model.getPositionCommand());
        SaveScreenshotMenu.setOnAction(e -> model.saveScreenshotCommand());
        AboutMenu.setOnAction(e -> model.aboutCommand());
        GetPositionButton.setOnAction(e -> model.getPositionCommand());
        SaveScreenshotButton.setOnAction(e -> model.saveScreenshotCommand());
        // プロパティをData Bindingする
        SaveScreenshotMenu.disableProperty().bind(model.DisableSaveScreenshotFlg);
        SaveScreenshotButton.disableProperty().bind(model.DisableSaveScreenshotFlg);
        // スクショ用のクラスを初期化する
        try {
            ScreenshotProvider.initialize();
        } catch (IOException e) {
            e.printStackTrace();
            Utility.showDialog("picフォルダを作成できませんでした。%nソフトウェアを終了します", "致命的なエラー", Alert.AlertType.ERROR);
        }
        // 起動時にバージョンチェックする
        model.checkVersionCommand();
    }
}
