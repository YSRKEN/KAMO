package com.ysrken.kamo.controller;

import com.ysrken.kamo.model.BattleSceneReflectionModel;
import com.ysrken.kamo.model.FleetCombineModel;
import com.ysrken.kamo.service.ScreenshotService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;

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
    @Autowired
    ScreenshotService screenshot;

    /**
     * 初期化
     */
    public void initialize() {
        System.out.println("DEBUG MainApp - FleetCombineController#initialize");
        FleetTabs.setGridLinesVisible(true);

        // FleetTabsとModelを設定する
        for(int y = 0; y < Y_COUNT; ++y){
            for(int x = 0; x < X_COUNT; ++x){
                // ImageViewのインスタンスを作成
                ImageView imageView = new ImageView();

                // ImgeViewにModelのImageを関連付ける
                imageView.imageProperty().bind(model.ImageList.get(y * X_COUNT + x));

                // クリックされた際、現在の画像を取り込むようにする
                imageView.setOnMouseClicked((e) -> {
                    System.out.println("Click");
                    if(screenshot != null && screenshot.canGetScreenshot()){
                        BufferedImage image = screenshot.getScreenshot();
                        imageView.setImage(SwingFXUtils.toFXImage(image, null));
                    }
                });

                // ImageViewのグリッド上の位置を指定して貼り付け
                FleetTabs.add(imageView, y, x);
            }
        }

        // グリッドの比率について設定する
        for(int x = 0; x < X_COUNT; ++x){
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / X_COUNT);
            FleetTabs.getColumnConstraints().add(column);
        }
        for(int y = 0; y < Y_COUNT; ++y){
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(100.0 / Y_COUNT);
            FleetTabs.getRowConstraints().add(row);
        }
    }
}
