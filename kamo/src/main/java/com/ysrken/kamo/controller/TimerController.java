package com.ysrken.kamo.controller;

import com.ysrken.kamo.model.BattleSceneReflectionModel;
import com.ysrken.kamo.model.TimerModel;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 各種タイマー画面のController
 * @author ysrken
 */
@Component
public class TimerController {
    /** タブを登録するベース */
    @FXML
    private TabPane TimerTabs;

    /**
     * Model
     */
    @Autowired
    TimerModel model;

    /**
     * 初期化
     */
    public void initialize() {
        System.out.println("DEBUG MainApp - TimerController#initialize");

        // 遠征タブを追加する
        final ObservableList<Tab> tabList = TimerTabs.getTabs();
        tabList.addAll(model.getTabList());
    }

    /**
     * 時刻をセットする
     */
    public void setExpTimer(Date date, int index){
        model.setExpTimer(date, index);
    }

    /**
     * 遠征情報をセットする
     */
    public void setExpInfo(String info, int index){
        model.setExpInfo(info, index);
    }

    /**
     * 遠征情報を取得する
     */
    public String getExpInfo(int index){
        return model.getExpInfo(index);
    }

    /**
     * 表示時間を更新する
     */
    public void refreshExpTimerString(){
        model.refreshExpTimerString();
    }
}
