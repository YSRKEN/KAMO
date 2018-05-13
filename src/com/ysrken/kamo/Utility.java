package com.ysrken.kamo;

import javafx.scene.control.Alert;

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
    public static void ShowDialog(String contentText, String headerText){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.setTitle(SOFTWARE_NAME);
        alert.show();
    }
}
