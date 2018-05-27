package com.ysrken.kamo;

import com.ysrken.kamo.Controller.BattleSceneReflectionController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.stream.Collectors;

public class Utility {
    /**
     * ソフトウェアの名称
     */
    public static final String SOFTWARE_NAME = "艦これモニタリングツール「KAMO」";
    /**
     * ソフトウェアのバージョン番号
     */
    public static final String SOFTWARE_VER = "1.0.0";
    /**
     * ソフトウェアのリビジョン番号
     */
    public static final int SOFTWARE_REVISION = 1;
    /**
     * ソフトウェアの作者名
     */
    public static final String SOFTWARE_AUTHOR = "YSRKEN";
    /**
     * ソフトウェアのURL
     */
    public static final String SOFTWARE_URL = "https://github.com/YSRKEN/KAMO/releases";

    /**
     * ダイアログを表示
     * @param contentText 本文
     * @param headerText タイトル文
     */
    public static void showDialog(String contentText, String headerText){
        showDialog(contentText, headerText, Alert.AlertType.INFORMATION);
    }
    /**
     * ダイアログを表示
     * @param contentText 本文
     * @param headerText タイトル文
     * @param type ダイアログの種類
     */
    public static void showDialog(String contentText, String headerText, Alert.AlertType type){
        // ダイアログの設定を行う
        final var alert = new Alert(type);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        // 適当なStageを作成し、ダイアログを「載せる」
        final var dialogPane = alert.getDialogPane();
        dialogPane.getScene().setRoot(new Group());
        final var stage = new Stage();
        for (ButtonType buttonType : dialogPane.getButtonTypes()) {
            final var button = (ButtonBase) dialogPane.lookupButton(buttonType);
            button.setOnAction(e -> {
                dialogPane.setUserData(buttonType);
                stage.close();
            });
        }
        stage.setScene(new Scene(dialogPane));
        stage.setTitle(SOFTWARE_NAME);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        // Stageを「表示」
        stage.show();
    }
    /**
     * YES/NOで選ぶ選択ダイアログを表示
     * @param contentText 本文
     * @param headerText タイトル文
     * @return 選択結果。YESならtrue、それ以外はfalse
     */
    public static boolean showChoiceDialog(String contentText, String headerText){
        final var alert = new Alert(Alert.AlertType.INFORMATION, contentText, ButtonType.CANCEL, ButtonType.APPLY);
        alert.setHeaderText(headerText);
        alert.setTitle(SOFTWARE_NAME);
        final var result = alert.showAndWait();
        return (result.isPresent() && result.get() == ButtonType.APPLY);
    }
    /**
     * 指定したURL上のファイルをUTF-8形式の文字列としてダウンロードする。
     * ダウンロードできなかった際は空文字列を返す
     * @param url URL
     * @return UTF-8形式のテキストデータ
     */
    public static String downloadTextData(String url){
        try(final var is = new URL(url).openStream();
            final var isr = new InputStreamReader(is, Charset.forName("UTF-8"));
            final var br = new BufferedReader(isr)){
            return br.lines().collect(Collectors.joining(String.format("%n")));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
    /**
     * 現在の日時を「2006-01-02 03-04-05-890」形式で取得
     * @return 現在の日時文字列
     */
    private static SimpleDateFormat sdfLong = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-SSS");
    public static String getDateStringLong(){
        return Utility.sdfLong.format(Calendar.getInstance().getTime());
    }
    /**
     * 現在の日時を「03:04:05」形式で取得
     * @return 現在の日時文字列
     */
    private static SimpleDateFormat sdfShort = new SimpleDateFormat("HH:mm:ss");
    public static String getDateStringShort(){
        return Utility.sdfShort.format(Calendar.getInstance().getTime());
    }
    /**
     * Windows OS上で動作しているかを判定
     * @return
     */
    public static boolean isWindows(){
        final var osName = System.getProperty("os.name").toLowerCase();
        return (osName.startsWith("windows"));
    }
}