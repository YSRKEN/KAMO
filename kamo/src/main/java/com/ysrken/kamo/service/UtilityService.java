package com.ysrken.kamo.service;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UtilityService {
	public UtilityService() {
		System.out.println("DEBUG MainApp - UtilityService#UtilityService");
	}
	
	/**
	 * Windows OS上で動作しているかを判定
	 */
    public boolean isWindows(){
        final String osName = System.getProperty("os.name").toLowerCase();
        return (osName.startsWith("windows"));
    }
    
    /**
     * 現在の日時を「2006-01-02 03-04-05-890」形式で取得
     * @return 現在の日時文字列
     */
    private final DateTimeFormatter dtfLong = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss-SSS");
    public String getDateStringLong(){
        return LocalDateTime.now().format(dtfLong);
    }
    
    /**
     * 現在の日時を「03:04:05」形式で取得
     * @return 現在の日時文字列
     */
    private final DateTimeFormatter dtfShort = DateTimeFormatter.ofPattern("HH:mm:ss");
    public String getDateStringShort(){
        return LocalDateTime.now().format(dtfShort);
    }

    /** 秒数を時間の文字列に変換する */
    public static String getDateStringShortFromLong(long x) {
        if(x <= 0){
            return "00:00:00";
        }
        long second = x % 60; x -= second; x /= 60;
        long minute = x % 60; x -= minute; x /= 60;
        long hour = x;
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    /**
     * 色間のRGB色空間における距離を計算する
     * @param a 色1
     * @param b 色2
     * @return 距離
     */
    public int calcColorDistance(Color a, Color b){
        final int rDiff = a.getRed() - b.getRed();
        final int gDiff = a.getGreen() - b.getGreen();
        final int bDiff = a.getBlue() - b.getBlue();
        return rDiff * rDiff + gDiff * gDiff + bDiff * bDiff;
    }
    
    /**
     * ビットカウント
     * 参考→http://developer.cybozu.co.jp/takesako/2006/11/binary_hacks.html
     * @param x long型(64bit)の値
     * @return ビットカウント後の数
     */
    private long popcnt(long x) {
        x = ((x & 0xaaaaaaaaaaaaaaaaL) >> 1) + (x & 0x5555555555555555L);
        x = ((x & 0xccccccccccccccccL) >> 2) + (x & 0x3333333333333333L);
        x = ((x & 0xf0f0f0f0f0f0f0f0L) >> 4) + (x & 0x0f0f0f0f0f0f0f0fL);
        x = ((x & 0xff00ff00ff00ff00L) >> 8) + (x & 0x00ff00ff00ff00ffL);
        x = ((x & 0xffff0000ffff0000L) >> 16) + (x & 0x0000ffff0000ffffL);
        x = ((x & 0xffffffff00000000L) >> 32) + (x & 0x00000000ffffffffL);
        return x;
    }
    
    /**
     * ハミング距離を計算する
     * @param a 値1
     * @param b 値2
     * @return ハミング距離
     */
    public long calcHummingDistance(long a, long b) {
        return popcnt(a ^ b);
    }
    
    /**
     * 「A/B」といった簡単な数式をパースして結果を返す
     * @param str 数式
     * @return 結果
     */
    public double parseFormula(String str) {
    	if(str.contains("/")) {
    		int pos = str.indexOf("/");
    		double x = Double.parseDouble(str.substring(0, pos));
    		double y = Double.parseDouble(str.substring(pos + 1, str.length()));
    		return x / y;
    	}else {
    		return Double.parseDouble(str);
    	}
    }
    
    /**
     * 常に最前面表示になるAlertダイアログ
     * 参考→http://totomo.net/11317-javafxalert.htm
     */
    private class AlwaysOnTopAlert extends Stage{
        /**
         * AlertをStage上に載せる
         * @param alert Alert
         */
        private void setAlertOnStage(Alert alert){
            final DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getScene().setRoot(new Group());
            dialogPane.setPadding(new Insets(0,0,1,0)); //黒魔術こわい
            for (ButtonType buttonType : dialogPane.getButtonTypes()) {
                final ButtonBase button = (ButtonBase) dialogPane.lookupButton(buttonType);
                button.setOnAction(e -> {
                    dialogPane.setUserData(buttonType);
                    this.close();
                });
            }
            this.setScene(new Scene(dialogPane));
            this.setTitle(com.ysrken.kamo.Constant.SOFTWARE_NAME);
            this.initModality(Modality.APPLICATION_MODAL);
            this.setAlwaysOnTop(true);
            this.setResizable(false);
        }

        /**
         * 通常のダイアログを表示する
         * @param contentText 内容
         * @param headerText タイトル
         * @param type 種類
         */
        public void showDialog(String contentText, String headerText, Alert.AlertType type){
            // ダイアログの設定を行う
            final Alert alert = new Alert(type);
            alert.setHeaderText(headerText);
            alert.setContentText(contentText);
            // Stageにダイアログを「載せる」
            setAlertOnStage(alert);
            // Stageを「表示」
            this.show();
        }
        
        /**
         * 選択ダイアログを表示する
         * @param contentText 内容
         * @param headerText タイトル
         * @return 選択結果が「OK」の時のみtrue
         */
        public boolean showChoiceDialog(String contentText, String headerText){
            // ダイアログの設定を行う
            final Alert alert = new Alert(Alert.AlertType.INFORMATION, contentText, ButtonType.CANCEL, ButtonType.APPLY);
            alert.setHeaderText(headerText);
            alert.setContentText(contentText);
            
            // Stageにダイアログを「載せる」
            setAlertOnStage(alert);
            
            // Stageを「表示」
            this.showAndWait();
            final Optional<ButtonType> buttonType = Optional.ofNullable((ButtonType) alert.getDialogPane().getUserData());
            return (buttonType.isPresent() && buttonType.get() == ButtonType.APPLY);
        }
    }

    /**
     * ダイアログを表示
     * @param contentText 本文
     * @param headerText タイトル文
     */
    public void showDialog(String contentText, String headerText){
        showDialog(contentText, headerText, Alert.AlertType.INFORMATION);
    }
    
    /**
     * ダイアログを表示
     * @param contentText 本文
     * @param headerText タイトル文
     * @param type ダイアログの種類
     */
    public void showDialog(String contentText, String headerText, Alert.AlertType type){
        final AlwaysOnTopAlert alert = new AlwaysOnTopAlert();
        alert.showDialog(contentText, headerText, type);
    }
    
    /**
     * YES/NOで選ぶ選択ダイアログを表示
     * @param contentText 本文
     * @param headerText タイトル文
     * @return 選択結果。YESならtrue、それ以外はfalse
     */
    public boolean showChoiceDialog(String contentText, String headerText){
        final AlwaysOnTopAlert alert = new AlwaysOnTopAlert();
        return alert.showChoiceDialog(contentText, headerText);
    }
    
    /**
     * 指定したURL上のファイルをUTF-8形式の文字列としてダウンロードする。
     * ダウンロードできなかった際は空文字列を返す
     * @param url URL
     * @return UTF-8形式のテキストデータ
     */
    public String downloadTextData(String url){
        try(final InputStream is = new URL(url).openStream();
            final InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
            final BufferedReader br = new BufferedReader(isr)){
            return br.lines().collect(Collectors.joining(String.format("%n")));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
