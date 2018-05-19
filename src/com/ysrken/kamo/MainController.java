package com.ysrken.kamo;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;

import java.io.IOException;

public class MainController {
    // 各コントロール
    @FXML private MenuItem ExitMenu;
    @FXML private MenuItem GetPositionMenu;
    @FXML private MenuItem SaveScreenshotMenu;
    @FXML private MenuItem OpenPicFolderMenu;
    @FXML private MenuItem CheckVersionMenu;
    @FXML private MenuItem AboutMenu;
    @FXML private Button GetPositionButton;
    @FXML private Button SaveScreenshotButton;
    @FXML private TextArea MessageLogTextArea;

    /**
     * Dependency InjectionさせるModel
     */
    private MainModel model;
    /**
     * ログテキスト
     */
    private StringProperty logText = new SimpleStringProperty("");

    /**
     * ログにテキストを追加する
     * @param text
     */
    private void addLogText(String text){
        logText.set(String.format(
                "%s%s %s%n",
                logText.get(),
                Utility.getDateStringShort(),
                text
        ));
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
        OpenPicFolderMenu.setOnAction(e -> model.openPicFolderCommand());
        AboutMenu.setOnAction(e -> model.aboutCommand());
        GetPositionButton.setOnAction(e -> model.getPositionCommand());
        SaveScreenshotButton.setOnAction(e -> model.saveScreenshotCommand());
        // プロパティをData Bindingする
        SaveScreenshotMenu.disableProperty().bind(model.DisableSaveScreenshotFlg);
        SaveScreenshotButton.disableProperty().bind(model.DisableSaveScreenshotFlg);
        MessageLogTextArea.textProperty().bind(this.logText);
        // 使えない設定をdisableする
        OpenPicFolderMenu.setDisable(!Utility.isWindows());
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
