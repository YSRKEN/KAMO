package com.ysrken.kamo.Service;

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

    /** 設定をJSONから読み込み
     * 参考→https://symfoware.blog.fc2.com/blog-entry-2094.html
     */
    private static void loadSettings(){
        Platform.runLater(() -> {
            if(Files.exists(new File("settings.json").toPath())){
                final var manager = new ScriptEngineManager();
                final var engine = manager.getEngineByName("javascript");
                try(final var fis = new FileInputStream("settings.json");
                    final var isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
                    final var br = new BufferedReader(isr)) {
                    final var json = (ScriptObjectMirror) engine.eval("JSON");
                    final var result = json.callMember("parse", br.lines().collect(Collectors.joining()));
                    final var m = (Map<?, ?>)result;

                    AutoGetPositionFlg.set(Boolean.class.cast(m.get("AutoGetPositionFlg")));
                    BlindNameTextFlg.set(Boolean.class.cast(m.get("BlindNameTextFlg")));
                    SpecialGetPosFlg.set(Boolean.class.cast(m.get("SpecialGetPosFlg")));

                } catch (IOException | ScriptException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    /** 設定をJSONに保存 */
    private static void saveSettings(){
        final var manager = new ScriptEngineManager();
        final var engine = manager.getEngineByName("javascript");
        try(final var fos = new FileOutputStream("settings.json");
            final var osw = new OutputStreamWriter(fos, Charset.forName("UTF-8"));
            final var bw = new BufferedWriter(osw)){
            final var m = (ScriptObjectMirror)engine.eval("new Object()");

            m.put("AutoGetPositionFlg", AutoGetPositionFlg.get());
            m.put("BlindNameTextFlg", BlindNameTextFlg.get());
            m.put("SpecialGetPosFlg", SpecialGetPosFlg.get());

            final var json = (ScriptObjectMirror) engine.eval("JSON");
            final var result = (String)json.callMember("stringify", m);
            bw.write(result);
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
    }
}
