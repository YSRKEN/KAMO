package com.ysrken.kamo.Controller;

import com.ysrken.kamo.Model.MainModel;
import com.ysrken.kamo.Service.SceneRecognitionService;
import com.ysrken.kamo.Service.ScreenshotService;
import com.ysrken.kamo.Service.SettingsStore;
import com.ysrken.kamo.Utility;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;

public class MainController {
    // 各コントロール
    @FXML private MenuItem ExitMenu;
    @FXML private MenuItem GetPositionMenu;
    @FXML private MenuItem SaveScreenshotMenu;
    @FXML private MenuItem OpenPicFolderMenu;
    @FXML private MenuItem OpenBattleSceneReflectionMenu;
    @FXML private MenuItem OpenSceneHelperMenu;
    @FXML private CheckMenuItem AutoGetPositionMenu;
    @FXML private MenuItem CheckVersionMenu;
    @FXML private MenuItem AboutMenu;
    @FXML private Button GetPositionButton;
    @FXML private Button SaveScreenshotButton;
    @FXML private Label NowSceneTextLabel;
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
        Platform.runLater(() -> {
            logText.set(String.format(
                    "%s%s %s%n",
                    logText.get(),
                    Utility.getDateStringShort(),
                    text
            ));
            MessageLogTextArea.setScrollTop(Double.POSITIVE_INFINITY);
        });
    }
    /**
     * 初期化
     */
    public void initialize(){
        // 設定用のクラスを初期化する
        SettingsStore.initialize();
        //
        model = new MainModel(this::addLogText);
        // メソッドをコントロールに割り当てる
        ExitMenu.setOnAction(e -> model.exitCommand());
        CheckVersionMenu.setOnAction(e -> model.checkVersionCommand());
        GetPositionMenu.setOnAction(e -> model.getPositionCommand());
        SaveScreenshotMenu.setOnAction(e -> model.saveScreenshotCommand());
        OpenPicFolderMenu.setOnAction(e -> model.openPicFolderCommand());
        OpenBattleSceneReflectionMenu.setOnAction(e -> model.openBattleSceneReflectionCommand());
        OpenSceneHelperMenu.setOnAction(e -> model.openSceneHelperCommand());
        AboutMenu.setOnAction(e -> model.aboutCommand());
        GetPositionButton.setOnAction(e -> model.getPositionCommand());
        SaveScreenshotButton.setOnAction(e -> model.saveScreenshotCommand());
        // プロパティをData Bindingする
        SaveScreenshotMenu.disableProperty().bind(model.DisableSaveScreenshotFlg);
        OpenBattleSceneReflectionMenu.disableProperty().bind(model.OpenBattleSceneReflectionFlg);
        OpenSceneHelperMenu.disableProperty().bind(model.OpenSceneHelperFlg);
        AutoGetPositionMenu.selectedProperty().bindBidirectional(model.AutoGetPositionFlg);
        SaveScreenshotButton.disableProperty().bind(model.DisableSaveScreenshotFlg);
        NowSceneTextLabel.textProperty().bind(model.NowSceneText);
        MessageLogTextArea.textProperty().bind(this.logText);
        // 使えない設定をdisableする
        OpenPicFolderMenu.setDisable(!Utility.isWindows());
        // スクショ用のクラスを初期化する
        try {
            ScreenshotService.initialize();
        } catch (IOException e) {
            e.printStackTrace();
            Utility.showDialog("picフォルダを作成できませんでした。%nソフトウェアを終了します", "致命的なエラー", Alert.AlertType.ERROR);
        }
        // 画像認識用のクラスを初期化する
        SceneRecognitionService.initialize();
        // 起動時にバージョンチェックする
        model.checkVersionCommand();
    }
}
