package com.ysrken.kamo;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class BattleSceneReflectionController {
    // RI KI WA ZA
    public ImageView SceneImageView1;
    public ImageView SceneImageView2;
    public ImageView SceneImageView3;
    public ImageView SceneImageView4;
    public ImageView SceneImageView5;
    public Label SceneLabel1;
    public Label SceneLabel2;
    public Label SceneLabel3;
    public Label SceneLabel4;
    public Label SceneLabel5;

    /**
     * シーンごとのマッピング
     */
    private final Map<String, TabContents> sceneMapping = new HashMap<>();

    /**
     * タブに表示する情報
     */
    private class TabContents{
        private ObjectProperty<Image> tabImage = new SimpleObjectProperty<>(null);
        private StringProperty tabLabel = new SimpleStringProperty("");
        private ImageView imageView;
        private Label label;
        /**
         * コンストラクタ
         * @param imageView 取得画像を表示するImageView
         * @param label 取得時刻を表示するLabel
         */
        public TabContents(ImageView imageView, Label label){
            this.imageView = imageView;
            this.label = label;
            this.imageView.imageProperty().bind(tabImage);
            this.label.textProperty().bind(tabLabel);
        }

        /**
         * 画像をセットする
         * @param image BufferedImage形式の画像
         */
        public void setImage(BufferedImage image){
            tabImage.set(SwingFXUtils.toFXImage(image, null));
        }
        /**
         * 文字列をセットする
         * @param text String型の文字列
         */
        public void setText(String text){
            tabLabel.set(text);
        }
    }

    /**
     * 初期化
     */
    public void initialize(){
        sceneMapping.put("昼戦後",   new TabContents(SceneImageView1, SceneLabel1));
        sceneMapping.put("夜戦後",   new TabContents(SceneImageView2, SceneLabel2));
        sceneMapping.put("戦闘結果", new TabContents(SceneImageView3, SceneLabel3));
        sceneMapping.put("MVP",     new TabContents(SceneImageView4, SceneLabel4));
        sceneMapping.put("マップ",   new TabContents(SceneImageView5, SceneLabel5));
        // 表示テスト
        var image = new BufferedImage(300, 200, TYPE_INT_ARGB);
        final var graphics = image.getGraphics();
        graphics.setColor(Color.BLUE);
        graphics.fillRect(0, 0, 300, 200);
        sceneMapping.get("昼戦後").setImage(image);
        sceneMapping.get("昼戦後").setText("2006/01/02 03:04:05:890");
    }
}
