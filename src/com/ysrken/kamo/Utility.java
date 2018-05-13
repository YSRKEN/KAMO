package com.ysrken.kamo;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.stream.Collectors;

public class Utility {
    /**
     * ソフトウェアの名称
     */
    private static String SOFTWARE_NAME = "艦これモニタリングツール「KAMO」";
    /**
     * ソフトウェアのバージョン番号
     */
    private static String SOFTWARE_VER = "1.0.0";
    /**
     * ソフトウェアのリビジョン番号
     */
    private static int SOFTWARE_REVISION = 1;
    /**
     * ソフトウェアの作者名
     */
    private static String SOFTWARE_AUTHOR = "YSRKEN";
    /**
     * ソフトウェアのURL
     */
    private static String SOFTWARE_URL = "YSRKEN";

    /**
     * ソフトウェアの名称を取得
     * @return ソフトウェアの名称
     */
    public static String getSoftwareName(){
        return SOFTWARE_NAME;
    }
    /**
     * ソフトウェアのバージョン番号を取得
     * @return ソフトウェアのバージョン番号
     */
    public static String getSoftwareVersion(){
        return SOFTWARE_VER;
    }
    /**
     * ソフトウェアのリビジョン番号を取得
     * @return ソフトウェアのリビジョン番号
     */
    public static int getSoftwareRevision(){
        return SOFTWARE_REVISION;
    }
    /**
     * ソフトウェアの作者名を取得
     * @return ソフトウェアの作者名
     */
    public static String getSoftwareAuthor(){
        return SOFTWARE_AUTHOR;
    }
    /**
     * ダイアログを表示
     * @param contentText 本文
     * @param headerText タイトル文
     */
    public static void showDialog(String contentText, String headerText){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.setTitle(SOFTWARE_NAME);
        alert.show();
    }
    public static boolean showChoiceDialog(String contentText, String headerText){
        Alert alert = new Alert(Alert.AlertType.INFORMATION, contentText, ButtonType.CANCEL, ButtonType.APPLY);
        alert.setHeaderText(headerText);
        alert.setTitle(SOFTWARE_NAME);
        Optional<ButtonType> result = alert.showAndWait();
        return (result.isPresent() && result.get() == ButtonType.APPLY);
    }
    /**
     * 指定したURL上のファイルをUTF-8形式の文字列としてダウンロードする。
     * ダウンロードできなかった際は空文字列を返す
     * @param url URL
     * @return UTF-8形式のテキストデータ
     */
    public static String downloadTextData(String url){
        try(InputStream is = new URL(url).openStream();
            InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr)){
            return br.lines().collect(Collectors.joining(String.format("%n")));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}