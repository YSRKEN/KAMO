package com.ysrken.kamo;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;

public class MainModel {
    public BooleanProperty DisableSaveScreenshotFlg = new SimpleBooleanProperty(true);

    /**
     * MainViewのログ表示部分にログを追加するメソッド
     */
    private Consumer<String> addLogText;

    /**
     * コンストラクタ
     * @param addLogText MainViewのログ表示部分にログを追加するメソッド
     */
    public MainModel(Consumer<String> addLogText){
        this.addLogText = addLogText;
    }
    /**
     * 終了コマンド
     */
    public void exitCommand(){
        System.exit(0);
    }
    /**
     * ゲーム座標を取得する
     */
    public void getPositionCommand(){
        addLogText.accept("【座標取得】");
        final var getPositionFlg = ScreenshotProvider.trySearchGamePosition();
        if(getPositionFlg){
            final var rect = ScreenshotProvider.getPosition();
            addLogText.accept(String.format("取得位置：(%d,%d)-%dx%d", rect.x, rect.y, rect.width, rect.height));
            DisableSaveScreenshotFlg.set(false);
        }else{
            addLogText.accept("座標取得：NG");
            DisableSaveScreenshotFlg.set(true);
        }
    }
    /**
     * スクリーンショットを取得・保存する
     */
    public void saveScreenshotCommand(){
        addLogText.accept("【スクリーンショット】");
        if(ScreenshotProvider.canGetScreenshot()){
            final var screenShot = ScreenshotProvider.getScreenshot();
            final var fileName = String.format("%s.png", Utility.getDateStringLong());
            try {
                ImageIO.write(screenShot, "png", new File(String.format("pic\\%s", fileName)));
                addLogText.accept(String.format("ファイル名：%s", fileName));
            } catch (IOException e) {
                e.printStackTrace();
                addLogText.accept("エラー：スクリーンショットの保存に失敗しました。");
            }
        }else{
            addLogText.accept("エラー：スクリーンショットを取得できません。");
        }
    }
    /**
     * ソフトウェアの更新が来ているかをチェックする
     */
    public void checkVersionCommand(){
        try {
            addLogText.accept("【更新チェック】");
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
            addLogText.accept(String.format("現在のバージョン：%s, リビジョン：%d", Utility.getSoftwareVersion(), Utility.getSoftwareRevision()));
            addLogText.accept(String.format("最新のバージョン：%s, リビジョン：%d", temp[1], revision));
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
                addLogText.accept("このソフトウェアは最新です。");
            }
        }catch(NumberFormatException | IOException e){
            e.printStackTrace();
            addLogText.accept("エラー：更新データを確認できませんでした。");
        }
    }
    /**
     * バージョン情報を表示する
     */
    public void aboutCommand(){
        String contentText = String.format("ソフト名：%s%nバージョン：%s%n作者：%s",
                Utility.getSoftwareName(),
                Utility.getSoftwareVersion(),
                Utility.getSoftwareAuthor());
        Utility.showDialog(contentText, "バージョン情報");
    }
}
