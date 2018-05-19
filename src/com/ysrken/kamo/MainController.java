package com.ysrken.kamo;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class MainController {
    @FXML private MenuItem ExitMenu;
    @FXML private MenuItem GetPositionMenu;
    @FXML private MenuItem CheckVersionMenu;
    @FXML private MenuItem AboutMenu;
    @FXML private Button GetPositionButton;
    @FXML private TextArea MessageLogTextArea;

    public void initialize(){
        // スクショ用のクラスを初期化する
        ScreenshotProvider.initialize();
        // 起動時にバージョンチェックする
        checkVersionCommand();
    }
    // ソフトウェアを終了する
    @FXML private void exitCommand(){
        System.exit(0);
    }
    // ゲーム座標を取得する
    @FXML private void getPositionCommand(){
        addLogText("【座標取得】");
        final var getPositionFlg = ScreenshotProvider.trySearchGamePosition();
        if(getPositionFlg){
            addLogText("座標取得：OK");
            final var rect = ScreenshotProvider.getPosition();
            addLogText(String.format("取得位置：(%d,%d)-%dx%d", rect.x, rect.y, rect.width, rect.height));
        }else{
            addLogText("座標取得：NG");
        }
    }
    // ソフトウェアの更新が来ているかをチェックする
    @FXML private void checkVersionCommand(){
        try {
            addLogText("【更新チェック】");
            // 更新情報を表すテキストファイルをダウンロードする
            String checkText = Utility.downloadTextData("https://raw.githubusercontent.com/YSRKEN/KAMO/master/version.txt");
            if(checkText == "")
                throw new IOException();
            // 更新文字列は「1,1.0.0」のような書式になっているはずなので確認する
            String[] temp = checkText.split(",");
            if(temp.length < 2){
                throw new NumberFormatException();
            }
            // 情報を読み取っていく
            int revision = Integer.parseInt(temp[0]);
            addLogText(String.format("現在のバージョン：%s, リビジョン：%d", Utility.getSoftwareVersion(), Utility.getSoftwareRevision()));
            addLogText(String.format("最新のバージョン：%s, リビジョン：%d", temp[1], revision));
            if(Utility.getSoftwareRevision() < revision){
                String message = String.format("より新しいバージョンが見つかりました。%n現在のバージョン：%s%n最新のバージョン：%s%nダウンロードサイトを開きますか？", Utility.getSoftwareVersion(), temp[1]);
                boolean openUrlFlg = Utility.showChoiceDialog(message, "更新チェック");
                if(openUrlFlg){
                    Desktop desktop = Desktop.getDesktop();
                    try{
                        desktop.browse(new URI("https://github.com/YSRKEN/KAMO/releases"));
                    }catch( Exception e ){
                        e.printStackTrace();
                    }
                }
            }else{
                addLogText("このソフトウェアは最新です。");
            }
        }catch(NumberFormatException | IOException e){
            e.printStackTrace();
            addLogText("エラー：更新データを確認できませんでした。");
            return;
        }
    }
    // バージョン情報を表示する
    @FXML private void aboutCommand(){
        String contentText = String.format("ソフト名：%s%nバージョン：%s%n作者：%s",
                Utility.getSoftwareName(),
                Utility.getSoftwareVersion(),
                Utility.getSoftwareAuthor());
        Utility.showDialog(contentText, "バージョン情報");
    }
    // ログにテキストを追加する
    public void addLogText(String text){
        String allText = MessageLogTextArea.getText();
        allText += Utility.getDateStringShort() + " " + text + String.format("%n");
        MessageLogTextArea.setText(allText);
        MessageLogTextArea.setScrollTop(MessageLogTextArea.getScrollTop() + 40);
    }
}
