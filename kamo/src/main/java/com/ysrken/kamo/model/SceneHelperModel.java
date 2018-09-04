package com.ysrken.kamo.model;

import com.ysrken.kamo.BitmapImage;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 画像認識支援画面のModel
 * @author ysrken
 */
@Component
public class SceneHelperModel {
    /** 表示用画像データ */
    public final ObjectProperty<Image> ViewImage = new SimpleObjectProperty<>(null);
    /** 表示用テキストデータ */
    public final StringProperty RectXPer = new SimpleStringProperty("0.0");
    public final StringProperty RectYPer = new SimpleStringProperty("0.0");
    public final StringProperty RectWPer = new SimpleStringProperty("20.0");
    public final StringProperty RectHPer = new SimpleStringProperty("20.0");
    public final StringProperty DifferenceHash = new SimpleStringProperty("");
    public final StringProperty AverageColor = new SimpleStringProperty("");

    /** 特殊な係数 */
    private final double divx = 1200.0 / 100.0;
    private final double divy = 720.0 / 100.0;
    /** 内部用座標データ */
    public final DoubleProperty rectXPer = new SimpleDoubleProperty(0.0);
    public final DoubleProperty rectYPer = new SimpleDoubleProperty(0.0);
    public final DoubleProperty rectWPer = new SimpleDoubleProperty(5.0 / divx);
    public final DoubleProperty rectHPer = new SimpleDoubleProperty(5.0 / divy);
    /** 内部用画像データ */
    private final ObjectProperty<BufferedImage> viewImageB = new SimpleObjectProperty<>(null);

    /** DifferenceHashを計算し、ラベルに反映する */
    private void calcDifferenceHash(){
        final long hash = BitmapImage.of(viewImageB.get()).calcDifferenceHash(rectXPer.get(), rectYPer.get(), rectWPer.get(), rectHPer.get());
        final String setText = String.format("0x%sL", Long.toHexString(hash));
        Platform.runLater(() -> DifferenceHash.set(setText));
    }
    /** AverageColorを計算し、ラベルに反映する */
    private void calcAverageColor(){
        final Color color = BitmapImage.of(viewImageB.get()).calcAverageColor(rectXPer.get(), rectYPer.get(), rectWPer.get(), rectHPer.get());
        final String setText = String.format("(%d,%d,%d)", color.getRed(), color.getGreen(), color.getBlue());
        Platform.runLater(() -> AverageColor.set(setText));
    }
    /** ラベルに表示する値の計算を行う */
    private void calcDHandAC(){
        calcDifferenceHash();
        calcAverageColor();
    }

    /** コンストラクタ */
    public SceneHelperModel(){
        System.out.println("DEBUG MainApp - SceneHelperModel#SceneHelperModel");

        // ChangeListenerを設定
        viewImageB.addListener((observable, oldValue, newValue) -> {
            ViewImage.set(SwingFXUtils.toFXImage(newValue, null));
            calcDHandAC();
        });
        RectXPer.addListener((observable, oldValue, newValue) -> {
            try{
                final double rectX = Double.parseDouble(newValue) / divx;
                rectXPer.set(rectX < 0.0 ? 0.0 : rectX > 100.0 ? 100.0 : rectX);
                calcDHandAC();
            }catch(Exception e){}
        });
        RectYPer.addListener((observable, oldValue, newValue) -> {
            try{
                final double rectY = Double.parseDouble(newValue) / divy;
                rectYPer.set(rectY < 0.0 ? 0.0 : rectY > 100.0 ? 100.0 : rectY);
                calcDHandAC();
            }catch(Exception e){}
        });
        RectWPer.addListener((observable, oldValue, newValue) -> {
            try{
                final double rectW = Double.parseDouble(newValue) / divx;
                rectWPer.set(rectW < 0.0 ? 0.0 : rectW > 100.0 ? 100.0 : rectW);
                calcDHandAC();
            }catch(Exception e){}
        });
        RectHPer.addListener((observable, oldValue, newValue) -> {
            try{
                final double rectH = Double.parseDouble(newValue) / divy;
                rectHPer.set(rectH < 0.0 ? 0.0 : rectH > 100.0 ? 100.0 : rectH);
                calcDHandAC();
            }catch(Exception e){}
        });
    }

    /** 画像データをセット */
    public void setImage(BufferedImage image){
        if(image != null)
            viewImageB.set(image);
    }
}
