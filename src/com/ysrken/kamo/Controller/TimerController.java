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
    private TimerModel timerModel = new TimerModel();

    /** 初期化 */
    public void initialize(){
        final var tabList = TimerTabs.getTabs();
        // 遠征タブを追加する
        {
            final var expTimerTab = new Tab();
            expTimerTab.setText("遠征");
            final var grid = new GridPane();
            grid.setGridLinesVisible(true);
            int row = 0;
            for(var stringproperty : timerModel.ExpTimerString){
                final var label = new Label();
                label.setFont(new Font("", 16));
                label.textProperty().bind(stringproperty);
                grid.addRow(row);
                grid.add(label, 0, row);
                ++row;
            }
            tabList.add(expTimerTab);
        }
    }
    /** 時刻をセットする */
    public void setExpTimer(Date date, int index){
        timerModel.setExpTimer(date, index);
    }
}
