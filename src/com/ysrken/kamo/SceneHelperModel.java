package com.ysrken.kamo;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;

public class SceneHelperModel {
    /**
     * 表示用画像データ
     */
    public final ObjectProperty<Image> ViewImage = new SimpleObjectProperty<>(null);
    public final StringProperty DifferenceHash = new SimpleStringProperty("テスト1");
    public final StringProperty AverageColor = new SimpleStringProperty("テスト2");

    /**
     * 内部用画像データ
     */
    private final ObjectProperty<BufferedImage> viewImageB = new SimpleObjectProperty<>(null);

    /**
     * コンストラクタ
     */
    public SceneHelperModel(){
        // ChangeListenerを設定
        viewImageB.addListener((observable, oldValue, newValue) -> {
            ViewImage.set(SwingFXUtils.toFXImage(newValue, null));
        });
    }
    /**
     * 画像データをセット
     * @param image 画像データ
     */
    public void setImage(BufferedImage image){
        viewImageB.set(image);
    }
}
