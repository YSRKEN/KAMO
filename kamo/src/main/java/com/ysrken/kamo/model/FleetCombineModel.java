package com.ysrken.kamo.model;

import com.ysrken.kamo.service.ScreenshotService;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.scene.input.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@Component
public class FleetCombineModel {

    /** 表示画像 */
    public final List<ImageView> ImageViewList = new ArrayList<>();

    /** セルの横個数 */
    public static final int X_COUNT = 4;

    /** セルの縦個数 */
    public static final int Y_COUNT = 4;

    /**
     * Model
     */
    @Autowired
    ScreenshotService screenshot;

    /**
     * コンストラクタ
     */
    public FleetCombineModel(){
        for(int y = 0; y < Y_COUNT; ++y){
            for(int x = 0; x < X_COUNT; ++x){
                // ImageViewのインスタンスを作成
                ImageView imageView = new ImageView();
                BufferedImage dummy = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
                dummy.setRGB(0, 0, 0xFFFFFF);
                imageView.setImage(SwingFXUtils.toFXImage(dummy, null));

                // 表示位置を指定
                GridPane.setConstraints(imageView, x, y);

                // クリックされた際、現在の画像を取り込むようにする
                imageView.setOnMouseClicked((e) -> {
                    System.out.println("Click");
                    if(screenshot != null && screenshot.canGetScreenshot()){
                        BufferedImage image = screenshot.getScreenshot();
                        imageView.setImage(SwingFXUtils.toFXImage(image, null));
                    }
                });

                ImageViewList.add(imageView);
            }
        }
    }
}
