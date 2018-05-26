package com.ysrken.kamo.Controller;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * タブによるスクショ表示を表現するカスタムコントロール
 * 参考→
 * 「FXMLを利用する（２）リソースバンドル／カスタムコンポーネント」
 * (URL略)
 * 「JavaFX8で簡単なアプリケーションを簡単に作る方法」
 * (http://seraphy.hatenablog.com/entries/2015/04/11)
 */
public class SceneTab extends Tab {
    @FXML private BorderPane SceneBP;
    @FXML private Label SceneLabel;
    @FXML private ImageView SceneImageView;
    /**
     * コンストラクタ
     */
    public SceneTab(String scene){
        super();
        this.setText(scene);
        final var fxmlLoader  = new FXMLLoader(getClass().getResource("../View/SceneTab.fxml"));
        // FXMLのルートタグとして、自らを登録
        fxmlLoader.setRoot( this );
        fxmlLoader.setController( this );
        // FXMLの読込
        try{
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        // ImageViewのサイズを自動調整する
        // 参考→https://qiita.com/opengl-8080/items/29c3ef163f41ee172173
        SceneImageView.fitWidthProperty().bind(SceneBP.widthProperty());
        SceneImageView.fitHeightProperty().bind(SceneBP.heightProperty());
    }
    public void setImage(BufferedImage image){
        Platform.runLater(() -> SceneImageView.setImage(SwingFXUtils.toFXImage(image, null)));
    }
    public void setLabelText(String text){
        Platform.runLater(() -> SceneLabel.setText(text));
    }
}
