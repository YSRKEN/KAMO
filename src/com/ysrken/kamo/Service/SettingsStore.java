package com.ysrken.kamo.Service;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class SettingsStore {
    public static BooleanProperty AutoGetPositionFlg = new SimpleBooleanProperty(false);

    /**
     * 初期化
     */
    public static void initialize(){
        // 変更時のセーブ設定
        AutoGetPositionFlg.addListener((s, o, n) -> saveSettings());
    }
    /**
     * 設定をJSONに保存
     */
    private static void saveSettings(){
        System.out.println("Save Settings.");
    }
}
