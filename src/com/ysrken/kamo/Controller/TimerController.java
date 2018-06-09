package com.ysrken.kamo.Controller;

import com.ysrken.kamo.Model.TimerModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;

import java.util.Date;

public class TimerController {
    /** タブを登録するベース */
    @FXML private TabPane TimerTabs;

    /** Model */
    private TimerModel timerModel;

    /** 初期化 */
    public void initialize(){
        final var tabList = TimerTabs.getTabs();
        // 遠征タブを追加する
        {
            final var expTimerTab = new Tab();
            expTimerTab.setText("遠征");
            final var grid = new GridPane();
            grid.setGridLinesVisible(true);
            grid.addColumn(0);
            grid.addColumn(1);
            int row = 0;
            timerModel = new TimerModel();
            for(int i = 0; i < timerModel.ExpTimerString.size(); ++i){
                grid.addRow(row);
                final var label = new Label();
                label.setFont(new Font("", 16));
                label.setStyle("-fx-font-size: 20;");
                label.textProperty().bind(timerModel.ExpTimerString.get(i));
                grid.add(label, 0, row);
                final var label2 = new Label();
                label2.setFont(new Font("", 16));
                label2.setStyle("-fx-font-size: 20;");
                label2.textProperty().bind(timerModel.ExpInfoString.get(i));
                grid.add(label2, 1, row);
                ++row;
            }
            expTimerTab.setContent(grid);
            tabList.add(expTimerTab);
        }
    }
    /** 時刻をセットする */
    public void setExpTimer(Date date, int index){
        timerModel.setExpTimer(date, index);
    }
    /** 遠征情報をセットする */
    public void setExpInfo(String info, int index){
        timerModel.setExpInfo(info, index);
    }
    /** 表示時間を更新する */
    public void refreshExpTimerString(){ timerModel.refreshExpTimerString(); }
}
