package com.ysrken.kamo.Model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.List;

public class TimerModel {
    /** 遠征の残時間表示 */
    public List<StringProperty> ExpTimerString = new ArrayList<>();

    /** 最大の遠征数 */
    private int expCount = 3;

    /** コンストラクタ */
    public TimerModel(){
        // 遠征周りの初期化
        for(int i = 0; i < expCount; ++i){
            ExpTimerString.add(new SimpleStringProperty("00:00:00"));
        }
    }
}
