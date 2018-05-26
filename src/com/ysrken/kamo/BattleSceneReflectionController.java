package com.ysrken.kamo;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BattleSceneReflectionController {
    // RI KI WA ZA
    @FXML private BorderPane SceneLabel1BP;
    @FXML private BorderPane SceneLabel2BP;
    @FXML private ImageView SceneImageView1;
    @FXML private ImageView SceneImageView2;
    @FXML private ImageView SceneImageView3;
    @FXML private ImageView SceneImageView4;
    @FXML private ImageView SceneImageView5;
    @FXML private Label SceneLabel1;
    @FXML private Label SceneLabel2;
    @FXML private Label SceneLabel3;
    @FXML private Label SceneLabel4;
    @FXML private Label SceneLabel5;

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
        // ImageViewのサイズを自動調整する。
        // 参考→https://qiita.com/opengl-8080/items/29c3ef163f41ee172173
        SceneImageView1.fitWidthProperty().bind(SceneLabel1BP.widthProperty());
        SceneImageView1.fitHeightProperty().bind(SceneLabel1BP.heightProperty());
        SceneImageView2.fitWidthProperty().bind(SceneLabel2BP.widthProperty());
        SceneImageView2.fitHeightProperty().bind(SceneLabel2BP.heightProperty());
        // マッピング
        sceneMapping.put("昼戦後",   new TabContents(SceneImageView1, SceneLabel1));
        sceneMapping.put("夜戦後",   new TabContents(SceneImageView2, SceneLabel2));
        sceneMapping.put("戦闘結果", new TabContents(SceneImageView3, SceneLabel3));
        sceneMapping.put("MVP",     new TabContents(SceneImageView4, SceneLabel4));
        sceneMapping.put("マップ",   new TabContents(SceneImageView5, SceneLabel5));
    }
    /**
     * 画像をタブにセット
     * @param key タブ名
     * @param image 画像
     */
    public void setImage(String key, BufferedImage image){
        sceneMapping.get(key).setImage(image);
    }
    /**
     * テキストをタブにセット
     * @param key タブ名
     * @param text テキスト
     */
    public void setText(String key, String text){
        sceneMapping.get(key).setText(text);
    }
    public Set<String> getBattleSceneSet(){
        return sceneMapping.keySet();
    }
}
