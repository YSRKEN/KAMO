package com.ysrken.kamo;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

    /** 指定したキーが有るかを判定する */
    public boolean hasKey(String key){
        return scriptObject.containsKey(key);
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

    /** 文字列をキーにgetRectangleを取得 */
    public Rectangle getRectangle(String key){
        final var temp = (ScriptObjectMirror)scriptObject.get(key);
        Integer[] list = temp.to(Integer[].class);
        return new Rectangle(list[0], list[1], list[2], list[3]);
    }

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-SSS");
    /** 文字列をキーに時刻の配列を取得 */
    public List<Date> getDateArray(String key) throws ParseException {
        final var temp = (ScriptObjectMirror)scriptObject.get(key);
        String[] list = temp.to(String[].class);
        final var dateList = new ArrayList<Date>();
        for(var dateString : list){
            dateList.add(sdf.parse(dateString));
        }
        return dateList;
    }
    /** 文字列をキーに文字列の配列を取得 */
    public List<String> getStringArray(String key)  {
        final var temp = (ScriptObjectMirror)scriptObject.get(key);
        String[] list = temp.to(String[].class);
        return Arrays.stream(list).collect(Collectors.toList());
    }

    /** 文字列をキーにbooleanを書き込む */
    public void setBoolean(String key, boolean bool){
        scriptObject.put(key , bool);
    }

    /** 文字列をキーにdoubleを書き込む */
    public void setDouble(String key, double dbl){
        scriptObject.put(key , dbl);
    }

    /** 文字列をキーにRectangleを書き込む */
    public void setRectangle(String key, Rectangle rect) throws ScriptException {
        ScriptObjectMirror list = (ScriptObjectMirror)scriptEngine.eval("new Array()");
        list.callMember("push", rect.x);
        list.callMember("push", rect.y);
        list.callMember("push", rect.width);
        list.callMember("push", rect.height);
        scriptObject.put(key, list);
    }

    /** 文字列をキーに時刻の配列を書き込む */
    public void setDateArray(String key, List<Date> dateList) throws ScriptException {
        ScriptObjectMirror list = (ScriptObjectMirror)scriptEngine.eval("new Array()");
        for(var date : dateList){
            list.callMember("push", sdf.format(date));
        }
        scriptObject.put(key, list);
    }

    /** 文字列をキーに文字列の配列を書き込む */
    public void setStringArray(String key, List<String> stringList) throws ScriptException {
        ScriptObjectMirror list = (ScriptObjectMirror)scriptEngine.eval("new Array()");
        for(var str : stringList){
            list.callMember("push", str);
        }
        scriptObject.put(key, list);
    }

    /** 文字列化 */
    @Override
    public String toString(){
        try {
            return (String)((ScriptObjectMirror) scriptEngine.eval("JSON")).callMember("stringify", scriptObject, null, 2);
        } catch (ScriptException e) {
            e.printStackTrace();
            return "";
        }
    }
}
