package com.ysrken.kamo.model;

import com.ysrken.kamo.service.PictureProcessingService;
import com.ysrken.kamo.service.UtilityService;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SceneTabModel {
    /** 表示画像 */
    public final ObjectProperty<Image> Image = new SimpleObjectProperty<>(null);
    public final StringProperty LabelText = new SimpleStringProperty("");

    /**
     * 最後に選択したフォルダパスを保持
     */
    private File lastSelectFolder = null;

    /**
     * 各種サービス
     */
    @Autowired
    private UtilityService utility;
    @Autowired
    private PictureProcessingService pictureProcessing;

    /**
     * 画像を保存するコマンド
     */
    public void saveScene(){
        // 画像がそもそも存在しているか？
        final Image image = Image.get();
        if(image == null){
            return;
        }
        // ファイルを選択
        final FileChooser fc = new FileChooser();
        fc.setTitle("ファイルを保存");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("ALL", "*.*")
        );
        fc.setInitialFileName(LabelText.get() + ".png");
        if(lastSelectFolder != null) {
            fc.setInitialDirectory(lastSelectFolder);
        }
        final File file = fc.showSaveDialog(null);
        if(file == null) {
            return;
        }
        lastSelectFolder = file.getParentFile();
        // 保存用のデータを保存
        try{
            final BufferedImage processedImage = pictureProcessing.getProcessedImage(SwingFXUtils.fromFXImage(image, null));
            ImageIO.write(processedImage, "png", file);
        } catch (IOException e) {
            utility.showDialog("画像を保存できませんでした。", "IOエラー", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * 画像をタブにセットする
     * @param image 画像
     */
    public void setImage(BufferedImage image){
        Platform.runLater(() -> {
            Image.set(SwingFXUtils.toFXImage(image, null));
            LabelText.set(utility.getDateStringLong());
        });
    }
}
