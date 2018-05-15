package com.ysrken.kamo;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class MainController {
    @FXML private MenuItem ExitMenu;
    @FXML private MenuItem GetPositionMenu;
    @FXML private MenuItem CheckVersionMenu;
    @FXML private MenuItem AboutMenu;
    @FXML private Button GetPositionButton;

    public void initialize(){
        // スクショ用のクラスを初期化する
        ScreenshotProvider.initialize();
        // 起動時にバージョンチェックする
        checkVersionCommand(false);
    }
    // ソフトウェアを終了する
    @FXML private void exitCommand(){
        System.exit(0);
    }
    // ゲーム座標を取得する
    @FXML private void getPositionCommand(){
        boolean getPositionFlg = ScreenshotProvider.getPosition();
        if(getPositionFlg){
            System.out.println("OK");
        }else{
            System.out.println("NG");
        }
    }
    // ソフトウェアの更新が来ているかをチェックする
    @FXML private void checkVersionCommand(){
        checkVersionCommand(true);
    }
    @FXML private void checkVersionCommand(boolean successDialogFlg){
        try {
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
            if(Utility.getSoftwareRevision() < revision){
                String message = String.format("より新しいバージョンが見つかりました。%n現行バージョン：%s%n最新バージョン：%s%nダウンロードサイトを開きますか？", Utility.getSoftwareVersion(), temp[1]);
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
                if(successDialogFlg){
                    Utility.showDialog("このソフトウェアは最新です。", "更新チェック");
                }
            }
        }catch(NumberFormatException | IOException e){
            e.printStackTrace();
            Utility.showDialog("更新データを確認できませんでした。", "更新チェック");
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
}
