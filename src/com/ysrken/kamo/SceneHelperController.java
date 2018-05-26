package com.ysrken.kamo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

public class SceneHelperController {
    @FXML private ImageView SceneImageView;
    @FXML private BorderPane SceneBP;
    @FXML private Label DifferenceHashLabel;
    @FXML private Label AverageColorLabel;

    public void initialize(){
        // ImageViewのサイズを自動調整する。
        // 参考→https://qiita.com/opengl-8080/items/29c3ef163f41ee172173
        SceneImageView.fitWidthProperty().bind(SceneBP.widthProperty());
        SceneImageView.fitHeightProperty().bind(SceneBP.heightProperty());

        DifferenceHashLabel.setText("テスト1");
        AverageColorLabel.setText("テスト2");
    }
}
