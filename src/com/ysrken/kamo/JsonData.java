package com.ysrken.kamo;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** JSONデータに効率よくアタッチするためのラッパークラス
 * 参考→https://symfoware.blog.fc2.com/blog-entry-2094.html
 */
public class JsonData {
    /** 実体 */
    private ScriptObjectMirror scriptObject = null;
    /** JSONを解釈するためのスクリプトエンジン */
    final private static ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("javascript");

    /** 空のデータを作成 */
    public static JsonData of() throws ScriptException {
        final var jsonData = new JsonData();
        jsonData.scriptObject = (ScriptObjectMirror)scriptEngine.eval("new Object()");
        return jsonData;
    }

    /** JSON文字列から作成 */
    public static JsonData of(String jsonString) throws ScriptException {
        final var jsonData = new JsonData();
        jsonData.scriptObject = (ScriptObjectMirror)((ScriptObjectMirror) scriptEngine.eval("JSON")).callMember("parse", jsonString);
        return jsonData;
    }

    /** ScriptObjectMirrorから作成 */
    public static JsonData of(ScriptObjectMirror scriptObject)  {
        final var jsonData = new JsonData();
        jsonData.scriptObject = scriptObject;
        return jsonData;
    }

    /** JsonDataのListに変換 */
    public List<JsonData> toJsonDataArray(){
        return Arrays.stream(scriptObject.to(ScriptObjectMirror[].class)).map(s -> JsonData.of(s)).collect(Collectors.toList());
    }

    /** 文字列をキーに文字列を取得 */
    public String getString(String key){
        return (String)scriptObject.get(key);
    }

    /** 文字列をキーにJsonDataを取得 */
    public JsonData getJsonData(String key){
        return JsonData.of((ScriptObjectMirror)scriptObject.get(key));
    }

    /** 文字列をキーにdoubleを取得(数式も評価する) */
    public double getDoubleEval(String key) throws ScriptException {
        return (double)JsonData.scriptEngine.eval(this.getString(key));
    }

    /** 文字列をキーにintを取得 */
    public int getInt(String key) {
        return (int)scriptObject.get(key);
    }

    /** 文字列をキーにbooleanを取得 */
    public boolean getBoolean(String key){
        return (boolean)scriptObject.get(key);
    }

    /** 文字列をキーにdoubleを取得 */
    public double getDouble(String key){
        // 座標が整数値だった際の対策
        final var val = scriptObject.get(key);
        if(val instanceof Integer){
            return 1.0 * ((Integer)val);
        }else {
            return (Double)val;
        }
    }

    /** 文字列をキーにbooleanを書き込む */
    public void setBoolean(String key, boolean bool){
        scriptObject.put(key , bool);
    }

    /** 文字列をキーにdoubleを書き込む */
    public void setDouble(String key, double dbl){
        scriptObject.put(key , dbl);
    }


    /** 文字列化 */
    @Override
    public String toString(){
        try {
            return (String)((ScriptObjectMirror) scriptEngine.eval("JSON")).callMember("stringify", scriptObject);
        } catch (ScriptException e) {
            e.printStackTrace();
            return "";
        }
    }
}
