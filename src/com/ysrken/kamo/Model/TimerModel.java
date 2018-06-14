package com.ysrken.kamo.Model;

import com.ysrken.kamo.Service.SettingsStore;
import com.ysrken.kamo.Utility;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimerModel {
    /** 遠征の残時間表示 */
    public final List<StringProperty> ExpTimerString = new ArrayList<>();
    public final List<StringProperty> ExpInfoString = SettingsStore.ExpInfoString;

    /** 最大の遠征数 */
    public static int EXPEDITION_COUNT = 3;
    private final List<ObjectProperty<Date>> ExpTimer = SettingsStore.ExpTimer;

    /** コンストラクタ */
    public TimerModel(){
        // 遠征周りの初期化
        for(int i = 0; i < EXPEDITION_COUNT; ++i){
            ExpTimerString.add(new SimpleStringProperty("00:00:00"));
            refreshExpTimerString();
        }
    }
    /** 時刻をセットする */
    public void setExpTimer(Date date, int index){
        ExpTimer.get(index).set(date);
    }
    /** 遠征情報をセットする */
    public void setExpInfo(String info, int index){
        Platform.runLater(() ->
            ExpInfoString.get(index).set(info));
    }
    /** 遠征の残時間表示を更新する */
    public void refreshExpTimerString(){
        for(int i = 0; i < EXPEDITION_COUNT; ++i){
            final var period = ExpTimer.get(i).get().getTime() - new Date().getTime();
            final var ii = i;
            Platform.runLater(() ->
                    ExpTimerString.get(ii).set(Utility.LongToDateStringShort(period / 1000)));
        }
    }
}
