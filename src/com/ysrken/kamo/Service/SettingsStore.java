package com.ysrken.kamo.Service;

import com.ysrken.kamo.JsonData;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Map;
import java.util.stream.Collectors;

public class SettingsStore {
    public static BooleanProperty AutoGetPositionFlg = new SimpleBooleanProperty(false);
    public static BooleanProperty BlindNameTextFlg = new SimpleBooleanProperty(true);
    public static BooleanProperty SpecialGetPosFlg = new SimpleBooleanProperty(false);
    public static BooleanProperty SaveWindowPositionFlg = new SimpleBooleanProperty(false);

    /** 設定をJSONから読み込み */
    private static void loadSettings(){
        if(Files.exists(new File("settings.json").toPath())){
            try(final var fis = new FileInputStream("settings.json");
                final var isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
                final var br = new BufferedReader(isr)) {
                // テキストデータを用意してパース
                final var jsonString = br.lines().collect(Collectors.joining());
                final var jsonData = JsonData.of(jsonString);
                // 各設定項目を読み取る
                Platform.runLater(() -> AutoGetPositionFlg.set(jsonData.getBoolean("AutoGetPositionFlg")));
                Platform.runLater(() -> BlindNameTextFlg.set(jsonData.getBoolean("BlindNameTextFlg")));
                Platform.runLater(() -> SpecialGetPosFlg.set(jsonData.getBoolean("SpecialGetPosFlg")));
                Platform.runLater(() -> SaveWindowPositionFlg.set(jsonData.getBoolean("SaveWindowPositionFlg")));
            } catch (IOException | ScriptException e) {
                e.printStackTrace();
            }
        }
    }
    /** 設定をJSONに保存 */
    private static void saveSettings(){
        try(final var fos = new FileOutputStream("settings.json");
            final var osw = new OutputStreamWriter(fos, Charset.forName("UTF-8"));
            final var bw = new BufferedWriter(osw)){
            final var jsonData = JsonData.of();
            // 各設定項目を書き込み
            jsonData.setBoolean("AutoGetPositionFlg", AutoGetPositionFlg.get());
            jsonData.setBoolean("BlindNameTextFlg", BlindNameTextFlg.get());
            jsonData.setBoolean("SpecialGetPosFlg", SpecialGetPosFlg.get());
            jsonData.setBoolean("SaveWindowPositionFlg", SaveWindowPositionFlg.get());
            // JSON文字列に変換
            final var jsonString = jsonData.toString();
            bw.write(jsonString);
            System.out.println("Save Settings.");
        } catch (ScriptException | IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 初期化
     */
    public static void initialize(){
        // 最初の読み込み
        loadSettings();
        // 変更時のセーブ設定
        AutoGetPositionFlg.addListener((s, o, n) -> saveSettings());
        BlindNameTextFlg.addListener((s, o, n) -> saveSettings());
        SpecialGetPosFlg.addListener((s, o, n) -> saveSettings());
        SaveWindowPositionFlg.addListener((s, o, n) -> saveSettings());
    }
}
