package com.ysrken.kamo.model;

import com.ysrken.kamo.service.SettingService;
import com.ysrken.kamo.service.UtilityService;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class TimerModel {
    /**
     * 最大の遠征数
     */
    private static int EXPEDITION_COUNT = 3;

    /**
     * 遠征の残時間表示
     */
    private final List<StringProperty> expTimerString = new ArrayList<>();

    /**
     * 遠征の名称表示
     */
    private final List<StringProperty> expInfoString = new ArrayList<>();

    /**
     * 遠征タイマーの終了時刻
     */
    private final List<ObjectProperty<Date>> expTimer = new ArrayList<>();

    /**
     * 各種サービス
     */
    @Autowired
    private SettingService setting;
    @Autowired
    private UtilityService utility;

    /** コンストラクタ */
    public TimerModel(){
        // 遠征周りの初期化
        for(int i = 0; i < EXPEDITION_COUNT; ++i){
            expTimerString.add(new SimpleStringProperty("00:00:00"));
            expInfoString.add(new SimpleStringProperty("？"));
            expTimer.add(new SimpleObjectProperty<>(new Date()));
        }
        refreshExpTimerString();
    }

    /**
     * タイマー表示用のタブを追加
     * @return
     */
    public List<Tab> getTabList(){
        final List<Tab> tabList = new ArrayList<>();
        // 遠征タブを追加する
        {
            // メイン表示部分を準備
            final GridPane grid = new GridPane(){{
                setGridLinesVisible(true);
                addColumn(0);
                addColumn(1);
                getColumnConstraints().addAll(new ColumnConstraints(), new ColumnConstraints(210));
            }};
            for(int i = 0; i < expTimerString.size(); ++i){
                grid.addRow(i);
                int row = i;

                // タイマー表示用ラベル
                final Label label = new Label(){{
                    setStyle("-fx-font-size: 20;-fx-padding: 5;");
                    textProperty().bind(expTimerString.get(row));
                }};
                grid.add(label, 0, row);

                // 遠征名表示用ラベル
                final Label label2 = new Label(){{
                    setStyle("-fx-font-size: 20;-fx-padding: 5;");
                    textProperty().bind(expInfoString.get(row));
                }};
                grid.add(label2, 1, row);
            }

            // タブ部分を準備
            final Tab expTimerTab = new Tab(){{
                setText("遠征");
                setContent(grid);
                getContent().setStyle("-fx-padding: 10;");
            }};

            //　リストに追加
            tabList.add(expTimerTab);
        }
        return tabList;
    }

    /**
     * 時刻をセットする
     */
    public void setExpTimer(Date date, int index){
        expTimer.get(index).set(date);
    }

    /**
     * 遠征情報をセットする
     */
    public void setExpInfo(String info, int index){
        Platform.runLater(() -> expInfoString.get(index).set(info));
    }

    /**
     * 表示時間を更新する
     */
    public void refreshExpTimerString(){
        for(int i = 0; i < EXPEDITION_COUNT; ++i){
            final long period = expTimer.get(i).get().getTime() - new Date().getTime();
            final int ii = i;
            Platform.runLater(() ->
                    expTimerString.get(ii).set(utility.getDateStringShortFromLong(period / 1000)));
        }
    }
}
