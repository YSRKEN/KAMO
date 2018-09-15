package com.ysrken.kamo.controller;

import com.ysrken.kamo.model.BattleSceneReflectionModel;
import com.ysrken.kamo.model.SceneTabModel;
import com.ysrken.kamo.service.PictureProcessingService;
import com.ysrken.kamo.service.UtilityService;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * タブによるスクショ表示を表現するカスタムコントロール
 * 参考→
 * 「FXMLを利用する（２）リソースバンドル／カスタムコンポーネント」
 * (URL略)
 * 「JavaFX8で簡単なアプリケーションを簡単に作る方法」
 * (http://seraphy.hatenablog.com/entries/2015/04/11)
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SceneTab extends Tab {
    /**
     * 各種  コントロール
     */
    @FXML private BorderPane SceneBP;
    @FXML private Label SceneLabel;
    @FXML private ImageView SceneImageView;
    @FXML private Button SaveSceneButton;

    /**
     * Model
     */
    @Autowired
    SceneTabModel model;

    /**
     * 初期化
     */
    public void initialize(String scene) throws IOException {
        this.setText(scene);

        // メソッドをコントロールに割り当てる
        SaveSceneButton.setOnAction(e -> model.saveScene());

        // プロパティをData Bindingさせる
        SceneImageView.imageProperty().bindBidirectional(model.Image);
        SceneLabel.textProperty().bindBidirectional(model.LabelText);

        // ImageViewのサイズを自動調整する
        // 参考→https://qiita.com/opengl-8080/items/29c3ef163f41ee172173
        SceneImageView.fitWidthProperty().bind(SceneBP.widthProperty());
        SceneImageView.fitHeightProperty().bind(SceneBP.heightProperty());
    }

    /**
     * 画像をタブにセットする
     * @param image 画像
     */
    public void setImage(BufferedImage image){
        model.setImage(image);
    }
}