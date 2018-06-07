package com.ysrken.kamo.Model;

import com.ysrken.kamo.Utility;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimerModel {
    /** 遠征の残時間表示 */
    public final List<StringProperty> ExpTimerString = new ArrayList<>();

    /** 最大の遠征数 */
    private int expCount = 3;
    private final List<Date> ExpTimer = new ArrayList<>();

    /** コンストラクタ */
    public TimerModel(){
        // 遠征周りの初期化
        for(int i = 0; i < expCount; ++i){
            ExpTimerString.add(new SimpleStringProperty("00:00:00"));
            ExpTimer.add(new Date());
        }
    }
    /** 時刻をセットする */
    public void setExpTimer(Date date, int index){
        ExpTimer.set(index, date);
    }
    /** 遠征の残時間表示を更新する */
    public void refreshExpTimerString(){
        for(int i = 0; i < expCount; ++i){
            final var period = ExpTimer.get(i).getTime() - new Date().getTime();
            final var ii = i;
            Platform.runLater(() ->
                    ExpTimerString.get(ii).set(Utility.LongToDateStringShort(period / 1000)));
        }
    }
}
