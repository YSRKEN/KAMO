package com.ysrken.kamo.Service;

import com.ysrken.kamo.JsonData;
import com.ysrken.kamo.Utility;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.control.Alert;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Map;
import java.util.stream.Collectors;

public class SettingsStore {
    public static BooleanProperty OpenBattleSceneReflectionFlg = new SimpleBooleanProperty(false);
    public static BooleanProperty OpenTimerFlg = new SimpleBooleanProperty(false);
    public static BooleanProperty OpenSceneHelperFlg = new SimpleBooleanProperty(false);
    public static BooleanProperty AutoGetPositionFlg = new SimpleBooleanProperty(false);
    public static BooleanProperty BlindNameTextFlg = new SimpleBooleanProperty(true);
    public static BooleanProperty SpecialGetPosFlg = new SimpleBooleanProperty(false);
    public static BooleanProperty SaveWindowPositionFlg = new SimpleBooleanProperty(false);
    //
    public static DoubleProperty MainViewX = new SimpleDoubleProperty(Double.MAX_VALUE);
    public static DoubleProperty MainViewY = new SimpleDoubleProperty(Double.MAX_VALUE);
    public static DoubleProperty MainViewW = new SimpleDoubleProperty(Double.MAX_VALUE);
    public static DoubleProperty MainViewH = new SimpleDoubleProperty(Double.MAX_VALUE);

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
                if(jsonData.hasKey("OpenBattleSceneReflectionFlg"))
                    OpenBattleSceneReflectionFlg.set(jsonData.getBoolean("OpenBattleSceneReflectionFlg"));
                if(jsonData.hasKey("OpenTimerFlg"))
                    OpenTimerFlg.set(jsonData.getBoolean("OpenTimerFlg"));
                if(jsonData.hasKey("OpenSceneHelperFlg"))
                    OpenSceneHelperFlg.set(jsonData.getBoolean("OpenSceneHelperFlg"));
                if(jsonData.hasKey("AutoGetPositionFlg"))
                    AutoGetPositionFlg.set(jsonData.getBoolean("AutoGetPositionFlg"));
                if(jsonData.hasKey("BlindNameTextFlg"))
                    BlindNameTextFlg.set(jsonData.getBoolean("BlindNameTextFlg"));
                if(jsonData.hasKey("SpecialGetPosFlg"))
                    SpecialGetPosFlg.set(jsonData.getBoolean("SpecialGetPosFlg"));
                if(jsonData.hasKey("SaveWindowPositionFlg"))
                    SaveWindowPositionFlg.set(jsonData.getBoolean("SaveWindowPositionFlg"));
                if(jsonData.hasKey("MainViewX"))
                    MainViewX.set(jsonData.getDouble("MainViewX"));
                if(jsonData.hasKey("MainViewY"))
                    MainViewY.set(jsonData.getDouble("MainViewY"));
                if(jsonData.hasKey("MainViewW"))
                    MainViewW.set(jsonData.getDouble("MainViewW"));
                if(jsonData.hasKey("MainViewH"))
                    MainViewH.set(jsonData.getDouble("MainViewH"));
            } catch (IOException | ScriptException e) {
                e.printStackTrace();
                Utility.showDialog(String.format("設定ファイルを開けませんでした。%nデフォルト設定で起動します。"), "IOエラー", Alert.AlertType.ERROR);
            }
        }
        // 特殊処理(座標記憶を設定してない際に詰まないようにする)
        if(!SaveWindowPositionFlg.get()){
            OpenBattleSceneReflectionFlg.set(false);
            OpenTimerFlg.set(false);
            OpenSceneHelperFlg.set(false);
        }
    }
    /** 設定をJSONに保存 */
    private static void saveSettings(){
        try(final var fos = new FileOutputStream("settings.json");
            final var osw = new OutputStreamWriter(fos, Charset.forName("UTF-8"));
            final var bw = new BufferedWriter(osw)){
            final var jsonData = JsonData.of();
            // 各設定項目を書き込み
            jsonData.setBoolean("OpenBattleSceneReflectionFlg", OpenBattleSceneReflectionFlg.get());
            jsonData.setBoolean("OpenTimerFlg", OpenTimerFlg.get());
            jsonData.setBoolean("OpenSceneHelperFlg", OpenSceneHelperFlg.get());
            jsonData.setBoolean("AutoGetPositionFlg", AutoGetPositionFlg.get());
            jsonData.setBoolean("BlindNameTextFlg", BlindNameTextFlg.get());
            jsonData.setBoolean("SpecialGetPosFlg", SpecialGetPosFlg.get());
            jsonData.setBoolean("SaveWindowPositionFlg", SaveWindowPositionFlg.get());
            jsonData.setDouble("MainViewX", MainViewX.get());
            jsonData.setDouble("MainViewY", MainViewY.get());
            jsonData.setDouble("MainViewW", MainViewW.get());
            jsonData.setDouble("MainViewH", MainViewH.get());
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
        OpenBattleSceneReflectionFlg.addListener((s, o, n) -> saveSettings());
        OpenTimerFlg.addListener((s, o, n) -> saveSettings());
        OpenSceneHelperFlg.addListener((s, o, n) -> saveSettings());
        AutoGetPositionFlg.addListener((s, o, n) -> saveSettings());
        BlindNameTextFlg.addListener((s, o, n) -> saveSettings());
        SpecialGetPosFlg.addListener((s, o, n) -> saveSettings());
        SaveWindowPositionFlg.addListener((s, o, n) -> saveSettings());
        MainViewX.addListener((s, o, n) -> saveSettings());
        MainViewY.addListener((s, o, n) -> saveSettings());
        MainViewW.addListener((s, o, n) -> saveSettings());
        MainViewH.addListener((s, o, n) -> saveSettings());
    }
}
