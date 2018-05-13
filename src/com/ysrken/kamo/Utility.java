package com.ysrken.kamo;

import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        ChoiceDialog<Boolean> choice = new ChoiceDialog();
        choice.setHeaderText(headerText);
        choice.setContentText(contentText);
        choice.setTitle(SOFTWARE_NAME);
        return choice.showAndWait().orElse(false);
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
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
    public static String downloadTextData2(String url){
        try (Stream<String> a = Files.lines(Paths.get(new URI(url)), Charset.forName("UTF-8"))) {
            return a.collect(Collectors.joining());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return "";
        }
    }
    public static String downloadTextData3(String url){
        try {
            return new String(Files.readAllBytes(Paths.get(new URI(url))), Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return "";
        }
    }
}