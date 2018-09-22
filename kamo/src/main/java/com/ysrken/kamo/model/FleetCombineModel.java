package com.ysrken.kamo.model;

import com.ysrken.kamo.BitmapImage;
import com.ysrken.kamo.service.PictureProcessingService;
import com.ysrken.kamo.service.ScreenshotService;
import com.ysrken.kamo.service.UtilityService;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class FleetCombineModel {

    /**
     * 表示画像の基準となる画像
     */
    private final List<BufferedImage> baseImageList = new ArrayList<>();

    /**
     * ダミー画像の大きさ
     */
    private static final int DUMMY_SIZE = 1;

    /**
     * ダミー画像の色
     */
    private static final int DUMMY_COLOR = 0xFFFFFF;

    /**
     * 保存時に基準となる画面横サイズ
     */
    private static final int DEFAULT_SIZE_X = 1200;

    /**
     * 保存時に基準となる画面縦サイズ
     */
    private static final int DEFAULT_SIZE_Y = 720;

    /**
     * 編成画面(大～小)におけるRECT
     */
    private static final List<Rectangle> VIEW_TYPE_RECT = new ArrayList<Rectangle>(Arrays.asList(
            new Rectangle(468, 141, 732, 565),
            new Rectangle(468, 141, 359, 565),
            new Rectangle(468, 141, 359, 348)
    ));

    /**
     * 編成画面(大～小)におけるRECT(割合)
     */
    private static List<double[]> VIEW_TYPE_RECT_PER = new ArrayList<>();

    /**
     * 最後に選択したフォルダパスを保持
     */
    private File lastSelectFolder = new File(String.format("%s\\pic", System.getProperty("user.dir")));

    /**
     * 表示画像
     */
    public final List<ImageView> ImageViewList = new ArrayList<>();

    /**
     * 表示形式
     */
    public final IntegerProperty ViewType = new SimpleIntegerProperty(0);

    /**
     * まとめ形式
     */
    public final IntegerProperty CombineType = new SimpleIntegerProperty(0);

    public final BooleanProperty ClearCheckFlg = new SimpleBooleanProperty(false);

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
    @Autowired
    UtilityService utility;

    /**
     * X列Y行目の画像に適切な艦番テキストを付与して返す
     * @param image 画像
     * @param x X-1
     * @param y Y-1
     * @return 追加後画像
     */
    private BufferedImage setTextByCombineType(BufferedImage image, double zoomPer, int x, int y){
        if (image.getWidth() <= DUMMY_SIZE) {
            return image;
        }
        if (CombineType.get() != 0){
            String text = "";
            switch(CombineType.get()){
                case 1:
                    if (x < 2 && y < 3){
                        text = String.format("%d番艦",y * 2 + x + 1);
                    }
                    break;
                case 2:
                    if(y < 3){
                        if (x < 2){
                            text = String.format("1-%d",y * 2 + x + 1);
                        }else{
                            text = String.format("2-%d",y * 2 + (x - 2) + 1);
                        }
                    }
                    break;
                case 3:
                    if (x < 2){
                        text = String.format("%d番艦",y * 2 + x + 1);
                    }
                    break;
            }
            if(ViewType.get() == 0){
                return BitmapImage.of(image).addText(text, (int)(zoomPer * 60), (int)(zoomPer * 550), (int)(zoomPer * 0)).getImage();
            }else{
                return BitmapImage.of(image).addText(text, (int)(zoomPer * 48), (int)(zoomPer * 209), (int)(zoomPer * 50)).getImage();
            }
        }else{
            return image;
        }
    }

    /**
     * X列Y行目の画像要素を書き換える
     * @param x X-1
     * @param y Y-1
     */
    private void updateImageView(int x, int y){
        Platform.runLater(() -> {
            // クロップする範囲を決定する
            double[] cropPer = VIEW_TYPE_RECT_PER.get(ViewType.get());

            // クロップする(ダミーデータは避ける)
            BufferedImage tempBi = baseImageList.get(y * X_COUNT + x), tempBi2;
            if (tempBi.getWidth() > DUMMY_SIZE) {
                tempBi2 = BitmapImage.of(tempBi).crop(cropPer[0], cropPer[1], cropPer[2], cropPer[3]).getImage();
            }else{
                tempBi2 = tempBi;
            }

            // CombineTypeを選択している場合、文字を付与する
            double zoomPer = 1.0 * tempBi.getWidth() / DEFAULT_SIZE_X;
            ImageViewList.get(y * X_COUNT + x).setImage(SwingFXUtils.toFXImage(setTextByCombineType(tempBi2, zoomPer, x, y), null));
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
     * ダミー画像を返す
     * @return ダミー画像
     */
    private BufferedImage getDummyImage(){
        BufferedImage image = new BufferedImage(DUMMY_SIZE, DUMMY_SIZE, BufferedImage.TYPE_3BYTE_BGR);
        image.setRGB(0, 0, DUMMY_COLOR);
        return image;
    }

    /**
     * コンストラクタ
     */
    public FleetCombineModel(){
        // VIEW_TYPE_RECT_PERを初期化
        for(Rectangle rect : VIEW_TYPE_RECT){
            VIEW_TYPE_RECT_PER.add(new double[]{1.0 * rect.x / 12, 1.0 * rect.y / 7.2, 1.0 * rect.width / 12, 1.0 * rect.height / 7.2});
        }

        // baseImageListを初期化
        for(int y = 0; y < Y_COUNT; ++y) {
            for (int x = 0; x < X_COUNT; ++x) {
                baseImageList.add(getDummyImage());
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

                /**
                 * クリックされた際、現在の画像を取り込むようにする
                 * 右クリックされた際、対象の画像を消去する
                 */
                final int x_ = x, y_ = y;
                imageView.setOnMouseClicked((e) -> {
                    MouseButton temp = e.getButton();
                    if (e.getButton() == MouseButton.PRIMARY) {
                        if (screenshot != null && screenshot.canGetScreenshot()) {
                            BufferedImage image = screenshot.getScreenshot();
                            baseImageList.set(y_ * X_COUNT + x_, image);
                            updateImageView(x_, y_);
                        }
                    }else if(e.getButton() == MouseButton.SECONDARY){
                        baseImageList.set(y_ * X_COUNT + x_, getDummyImage());
                        updateImageView(x_, y_);
                    }
                });

                ImageViewList.add(imageView);
            }
        }

        // ViewTypeにイベント設定を行う
        ViewType.addListener((ob, o, n) -> updateImageViewAll());
        CombineType.addListener((ob, o, n) -> updateImageViewAll());
    }

    /**
     * 画像を全て消去する
     */
    public void clearAll(){
        for(int y = 0; y < Y_COUNT; ++y) {
            for (int x = 0; x < X_COUNT; ++x) {
                baseImageList.set(y * X_COUNT + x, getDummyImage());
                updateImageView(x, y);
            }
        }
    }

    /**
     * 画像の保存処理
     */
    public void saveCombinePicture(){
        // 左上座標と右下座標を取得
        int[] rect1 = {X_COUNT, Y_COUNT}, rect2 = {-1, -1};
        for(int y = 0; y < Y_COUNT; ++y) {
            for (int x = 0; x < X_COUNT; ++x) {
                BufferedImage tempBi = baseImageList.get(y * X_COUNT + x);
                if (tempBi.getWidth() > DUMMY_SIZE) {
                    rect1[0] = Math.min(rect1[0], x);
                    rect1[1] = Math.min(rect1[1], y);
                    rect2[0] = Math.max(rect2[0], x);
                    rect2[1] = Math.max(rect2[1], y);
                }
            }
        }
        if (rect2[0] < 0){
            return;
        }

        // 範囲を別のイメージに転記
        int rectW = rect2[0] - rect1[0] + 1, rectH = rect2[1] - rect1[1] + 1;
        Rectangle tempRect = VIEW_TYPE_RECT.get(ViewType.get());
        BitmapImage resultImage = BitmapImage.of(new BufferedImage(
                tempRect.width * rectW, tempRect.height * rectH, BufferedImage.TYPE_3BYTE_BGR
        ));
        for(int y = rect1[1]; y <= rect2[1]; ++y) {
            for (int x = rect1[0]; x <= rect2[0]; ++x) {
                BufferedImage tempBi = baseImageList.get(y * X_COUNT + x);
                BufferedImage tempBi2;
                if (tempBi.getWidth() > DUMMY_SIZE) {
                    tempBi2 = BitmapImage.of(tempBi).resize(DEFAULT_SIZE_X, DEFAULT_SIZE_Y).crop(tempRect).getImage();
                }else{
                    tempBi2 = BitmapImage.of(getDummyImage()).resize(DEFAULT_SIZE_X, DEFAULT_SIZE_Y).crop(tempRect).getImage();
                }
                BufferedImage tempBi3 = setTextByCombineType(tempBi2, 1.0, x, y);
                resultImage = resultImage.paste(BitmapImage.of(tempBi3), (x - rect1[0]) * tempRect.width, (y - rect1[1]) * tempRect.height);
            }
        }

        // 画像を保存
        // ファイルを選択
        final FileChooser fc = new FileChooser();
        fc.setTitle("ファイルを保存");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("ALL", "*.*")
        );
        fc.setInitialFileName("編成まとめ画像.png");
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
            ImageIO.write(resultImage.getImage(), "png", file);
            if(ClearCheckFlg.get()){
                clearAll();
            }
        } catch (IOException e) {
            utility.showDialog("画像を保存できませんでした。", "IOエラー", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
}
