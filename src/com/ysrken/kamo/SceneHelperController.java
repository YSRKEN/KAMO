package com.ysrken.kamo;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.awt.image.BufferedImage;

public class SceneHelperController {
    /**
     * 各コントロール・コンテナ
     */
    @FXML private ImageView SceneImageView;
    @FXML private BorderPane SceneBP;
    @FXML private Label DifferenceHashLabel;
    @FXML private Label AverageColorLabel;

    /**
     * Model
     */
    private SceneHelperModel model;

    /**
     * 初期化
     */
    public void initialize(){
        model = new SceneHelperModel();
        // Data Binding
        SceneImageView.imageProperty().bind(model.ViewImage);
        DifferenceHashLabel.textProperty().bind(model.DifferenceHash);
        AverageColorLabel.textProperty().bind(model.AverageColor);
        // ImageViewのサイズを自動調整する
        // 参考→https://qiita.com/opengl-8080/items/29c3ef163f41ee172173
        SceneImageView.fitWidthProperty().bind(SceneBP.widthProperty());
        SceneImageView.fitHeightProperty().bind(SceneBP.heightProperty());
    }
    /**
     * 画像をセット
     * @param image 画像データ
     */
    public void setImage(BufferedImage image){
        model.setImage(image);
    }
}
