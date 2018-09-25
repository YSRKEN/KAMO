package com.ysrken.kamo.controller;

import com.ysrken.kamo.BitmapImage;
import com.ysrken.kamo.model.SceneHelperModel;
import com.ysrken.kamo.service.ScreenshotService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;

/**
 * 画像認識支援画面のController
 * @author ysrken
 */
@Component
public class SceneHelperController {
    /**
     * 各コントロール
     */
    @FXML private TextField RectXPerTextField;
    @FXML private TextField RectYPerTextField;
    @FXML private TextField RectWPerTextField;
    @FXML private TextField RectHPerTextField;
    @FXML private TextField DifferenceHashTextField;
    @FXML private TextField AverageColorTextField;
    @FXML private ImageView SceneImageView;
    @FXML private BorderPane SceneBP;
    @FXML private Button GetScreenShotImageButton;

    /**
     * Model
     */
    @Autowired
    SceneHelperModel model;
    @Autowired
    private ScreenshotService screenshot;

    /**
     * 初期化
     */
    public void initialize() {
        System.out.println("DEBUG MainApp - SceneHelperController#initialize");

        // メソッドをコントロールに割り当てる
        GetScreenShotImageButton.setOnAction(e -> {
            if(screenshot.canGetScreenshot()) {
                final BufferedImage image = screenshot.getScreenshot();
                setImage(BitmapImage.of(image).clone().getImage());
            }
        });

        // プロパティをData Bindingさせる
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
