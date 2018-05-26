package com.ysrken.kamo;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.awt.image.BufferedImage;

public class SceneHelperController {
    /**
     * 各コントロール・コンテナ
     */
    @FXML private TextField RectXPerTextField;
    @FXML private TextField RectYPerTextField;
    @FXML private TextField RectWPerTextField;
    @FXML private TextField RectHPerTextField;
    @FXML private TextField DifferenceHashTextField;
    @FXML private TextField AverageColorTextField;
    @FXML private ImageView SceneImageView;
    @FXML private BorderPane SceneBP;

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
        RectXPerTextField.textProperty().bindBidirectional(model.RectXPer);
        RectYPerTextField.textProperty().bindBidirectional(model.RectYPer);
        RectWPerTextField.textProperty().bindBidirectional(model.RectWPer);
        RectHPerTextField.textProperty().bindBidirectional(model.RectHPer);
        DifferenceHashTextField.textProperty().bind(model.DifferenceHash);
        AverageColorTextField.textProperty().bind(model.AverageColor);
        SceneImageView.imageProperty().bind(model.ViewImage);
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
