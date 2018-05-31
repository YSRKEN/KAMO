package com.ysrken.kamo.Controller;

import com.ysrken.kamo.Service.PictureProcessingService;
import com.ysrken.kamo.Utility;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;

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
    @FXML private Button SaveSceneButton;

    private File lastSelectFolder = null;

    /**
     * 画像を保存するコマンド
     */
    private void saveScene(){
        // 画像がそもそも存在しているか？
        final var image = SceneImageView.getImage();
        if(image == null){
            return;
        }
        // ファイルを選択
        final var fc = new FileChooser();
        fc.setTitle("ファイルを保存");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("ALL", "*.*")
        );
        fc.setInitialFileName(SceneLabel.getText() + ".png");
        if(lastSelectFolder != null) {
            fc.setInitialDirectory(lastSelectFolder);
        }
        final var file = fc.showSaveDialog(null);
        if(file == null) {
            return;
        }
        lastSelectFolder = file.getParentFile();
        // 保存用のデータを保存
        try{
            final var processedImage = PictureProcessingService.getProcessedImage(SwingFXUtils.fromFXImage(image, null));
            ImageIO.write(processedImage, "png", file);
        } catch (IOException e) {
            Utility.showDialog("画像を保存できませんでした。", "IOエラー", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * コンストラクタ
     */
    public SceneTab(String scene){
        super();
        this.setText(scene);
        final var fxmlLoader  = new FXMLLoader(
                ClassLoader.getSystemResource("com/ysrken/kamo/View/SceneTab.fxml"));
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
        // Data Binding
        SaveSceneButton.setOnAction(e -> saveScene());
    }
    /**
     * 画像をタブにセットする
     * @param image 画像
     */
    public void setImage(BufferedImage image){
        Platform.runLater(() -> SceneImageView.setImage(SwingFXUtils.toFXImage(image, null)));
    }
    /**
     * テキストをタブにセットする
     * @param text
     */
    public void setLabelText(String text){
        Platform.runLater(() -> SceneLabel.setText(text));
    }
}
