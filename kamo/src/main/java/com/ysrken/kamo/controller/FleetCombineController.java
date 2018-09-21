package com.ysrken.kamo.controller;

import com.ysrken.kamo.model.BattleSceneReflectionModel;
import com.ysrken.kamo.model.FleetCombineModel;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
     * Model
     */
    @Autowired
    FleetCombineModel model;

    /**
     * 初期化
     */
    public void initialize() {
        System.out.println("DEBUG MainApp - FleetCombineController#initialize");

        // FleetTabsとModelを設定する
        for(int y = 0; y < Y_COUNT; ++y){
            for(int x = 0; x < X_COUNT; ++x){
                ImageView imageView = new ImageView();
                imageView.imageProperty().bind(model.ImageList.get(y * X_COUNT + x));
                FleetTabs.add(imageView, y, x);
            }
        }
    }
}
