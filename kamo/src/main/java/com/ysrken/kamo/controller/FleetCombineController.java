package com.ysrken.kamo.controller;

import com.ysrken.kamo.model.FleetCombineModel;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.ComboBox;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.ysrken.kamo.model.FleetCombineModel.X_COUNT;
import static com.ysrken.kamo.model.FleetCombineModel.Y_COUNT;

/**
 * 編成まとめ画面のController
 * @author ysrken
 */
@Component
public class FleetCombineController {
    /**
     * タブ一覧(コントロール)
     */
    @FXML
    private GridPane FleetTabs;

    /**
     * 表示タイプ設定(コントロール)
     */
    @FXML
    private ComboBox FleetTypeComboBox;

    /**
     * Model
     */
    @Autowired
    FleetCombineModel model;

    /**
     * 初期化
     */
    public void initialize() {
        System.out.println("DEBUG MainApp - FleetCombineController#initialize");
        FleetTabs.setGridLinesVisible(true);

        // グリッドの比率について設定する
        for(int x = 0; x < X_COUNT; ++x){
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / X_COUNT);
            column.setHgrow(Priority.ALWAYS);
            column.setHalignment(HPos.CENTER);
            column.setFillWidth(true);
            FleetTabs.getColumnConstraints().add(column);
        }
        for(int y = 0; y < Y_COUNT; ++y){
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(100.0 / Y_COUNT);
            row.setVgrow(Priority.ALWAYS);
            row.setValignment(VPos.CENTER);
            row.setFillHeight(true);
            FleetTabs.getRowConstraints().add(row);
        }

        // FleetTabsとModelを設定する
        final List<ImageView> imageViewLIst = model.ImageViewList;
        for(int y = 0; y < Y_COUNT; ++y){
            for(int x = 0; x < X_COUNT; ++x){
                ImageView imageView = imageViewLIst.get(y * X_COUNT + x);
                FleetTabs.widthProperty().addListener((ob, o, n) ->{
                    imageView.setFitWidth(n.doubleValue() / X_COUNT - 2);
                });
                FleetTabs.heightProperty().addListener((ob, o, n) ->{
                    imageView.setFitHeight(n.doubleValue() / Y_COUNT - 2);
                });
                FleetTabs.add(imageView, x, y);
            }
        }

        // その他の設定
        FleetTypeComboBox.getSelectionModel().select(model.ViewType.get());
        model.ViewType.bind(FleetTypeComboBox.getSelectionModel().selectedIndexProperty());
    }
}
