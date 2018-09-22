package com.ysrken.kamo.model;

import com.ysrken.kamo.BitmapImage;
import com.ysrken.kamo.service.ScreenshotService;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
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

    /**
     * 表示画像の基準となる画像
     */
    private final List<BufferedImage> baseImageList = new ArrayList<>();

    /**
     * 表示画像
     */
    public final List<ImageView> ImageViewList = new ArrayList<>();

    /**
     * 表示形式
     */
    public final IntegerProperty ViewType = new SimpleIntegerProperty(0);

    /**
     * セルの横個数
     */
    public static final int X_COUNT = 4;

    /**
     * セルの縦個数
     */
    public static final int Y_COUNT = 4;

    /**
     * Model
     */
    @Autowired
    ScreenshotService screenshot;

    /**
     * X列Y行目の画像要素を書き換える
     * @param x X-1
     * @param y Y-1
     */
    private void updateImageView(int x, int y){
        Platform.runLater(() -> {
            // クロップする範囲を決定する
            double[] cropPer = {0.0, 0.0, 100.0, 100.0};

            switch (ViewType.get()){
                case 0:
                    // 編成画面(大)
                    cropPer = new double[]{468.0 / 12, 141.0 / 7.2, 732.0 / 12, 565.0 / 7.2};
                    break;
                case 1:
                    // 編成画面(中)
                    cropPer = new double[]{468.0 / 12, 141.0 / 7.2, 359.0 / 12, 565.0 / 7.2};
                    break;
                case 2:
                    // 編成画面(小)
                    cropPer = new double[]{468.0 / 12, 141.0 / 7.2, 359.0 / 12, 348.0 / 7.2};
                    break;
            }

            // クロップする(ダミーデータは避ける)
            BufferedImage tempBi = baseImageList.get(y * X_COUNT + x);
            if (tempBi.getWidth() > 1) {
                BufferedImage tempBi2 = BitmapImage.of(tempBi).crop(cropPer[0], cropPer[1], cropPer[2], cropPer[3]).getImage();
                ImageViewList.get(y * X_COUNT + x).setImage(SwingFXUtils.toFXImage(tempBi2, null));
            }
        });
    }

    /**
     * 全要素を置き換える
     */
    private void updateImageViewAll(){
        // baseImageListを初期化
        for(int y = 0; y < Y_COUNT; ++y) {
            for (int x = 0; x < X_COUNT; ++x) {
                updateImageView(x, y);
            }
        }
    }

    /**
     * コンストラクタ
     */
    public FleetCombineModel(){
        // baseImageListを初期化
        for(int y = 0; y < Y_COUNT; ++y) {
            for (int x = 0; x < X_COUNT; ++x) {
                BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
                image.setRGB(0, 0, 0xFFFFFF);
                baseImageList.add(image);
            }
        }

        // ImageViewListを初期化
        for(int y = 0; y < Y_COUNT; ++y){
            for(int x = 0; x < X_COUNT; ++x){
                // ImageViewのインスタンスを作成
                ImageView imageView = new ImageView();
                imageView.setImage(SwingFXUtils.toFXImage(baseImageList.get(y * X_COUNT + x), null));

                // 表示位置を指定
                GridPane.setConstraints(imageView, x, y);

                // クリックされた際、現在の画像を取り込むようにする
                final int x_ = x, y_ = y;
                imageView.setOnMouseClicked((e) -> {
                    if(screenshot != null && screenshot.canGetScreenshot()){
                        BufferedImage image = screenshot.getScreenshot();
                        baseImageList.set(y_ * X_COUNT + x_, image);
                        updateImageView(x_, y_);
                    }
                });

                ImageViewList.add(imageView);
            }
        }

        // ViewTypeにイベント設定を行う
        ViewType.addListener((ob, o, n) -> updateImageViewAll());
    }
}
