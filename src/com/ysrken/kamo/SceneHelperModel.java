package com.ysrken.kamo;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;

import static java.awt.Image.SCALE_SMOOTH;

public class SceneHelperModel {
    /**
     * 表示用画像データ
     */
    public final ObjectProperty<Image> ViewImage = new SimpleObjectProperty<>(null);
    /**
     * 表示用テキストデータ
     */
    public final StringProperty RectXPer = new SimpleStringProperty("0.0");
    public final StringProperty RectYPer = new SimpleStringProperty("0.0");
    public final StringProperty RectWPer = new SimpleStringProperty("20.0");
    public final StringProperty RectHPer = new SimpleStringProperty("20.0");
    public final StringProperty DifferenceHash = new SimpleStringProperty("");
    public final StringProperty AverageColor = new SimpleStringProperty("");

    //特殊な係数
    private final double divx = 800.0 / 100.0;
    private final double divy = 480.0 / 100.0;
    // 内部用座標データ
    public final DoubleProperty rectXPer = new SimpleDoubleProperty(0.0);
    public final DoubleProperty rectYPer = new SimpleDoubleProperty(0.0);
    public final DoubleProperty rectWPer = new SimpleDoubleProperty(5.0 / divx);
    public final DoubleProperty rectHPer = new SimpleDoubleProperty(5.0 / divy);
    /**
     * 内部用画像データ
     */
    private final ObjectProperty<BufferedImage> viewImageB = new SimpleObjectProperty<>(null);

    /**
     * ％表記の割合(A)と100％時のピクセル値(B)から、ピクセルを出力する
     * ただし出力値は、[0, B - 1]にクロップされる
     * @param per 割合
     * @param pixel ピクセル
     * @return 割合値のピクセル
     */
    private static long perToPixel(double per, int pixel){
        final var rawPixel = per * pixel / 100;
        final var roundPixel = Math.round(rawPixel);
        return Math.min(Math.max(roundPixel, 0), pixel - 1);
    }
    /**
     * DifferenceHashを計算し、ラベルに反映する
     */
    private void calcDifferenceHash(){
        // 画像の選択範囲(％)を選択範囲(ピクセル)に変換
        final var rectX = (int)perToPixel(rectXPer.get(), viewImageB.get().getWidth());
        final var rectY = (int)perToPixel(rectYPer.get(), viewImageB.get().getHeight());
        final var rectW = (int)perToPixel(rectWPer.get(), viewImageB.get().getWidth());
        final var rectH = (int)perToPixel(rectHPer.get(), viewImageB.get().getHeight());
        // 元画像を切り抜き、9x8ピクセルにリサイズ
        final var tempImage = viewImageB.get()
                .getSubimage(rectX, rectY, rectW, rectH)
                .getScaledInstance(9, 8, SCALE_SMOOTH);
        // リサイズした画像を、グレースケール化しつつBufferedImageに書き出し
        final var canvas = new BufferedImage(9, 8, BufferedImage.TYPE_BYTE_GRAY);
        final var g = canvas.getGraphics();
        g.drawImage(tempImage, 0, 0, null);
        g.dispose();
        // 隣接ピクセルとの比較結果を符号化する
        long hash = 0;
        for (var y = 0; y < 8; ++y){
            for (var x = 0; x < 8; ++x){
                hash <<= 1;
                final var b1 = canvas.getRGB(x, y) & 0xFF;
                final var b2 = canvas.getRGB(x + 1, y) & 0xFF;
                if (b1 > b2)
                    hash |= 1;
            }
        }
        // 表示する
        final long hash2 = hash;
        Platform.runLater(() -> DifferenceHash.set(String.format("0x%sL", Long.toHexString(hash2))));
    }
    /**
     * AverageColorを計算し、ラベルに反映する
     */
    private void calcAverageColor(){
        // 画像の選択範囲(％)を選択範囲(ピクセル)に変換
        final var rectX = (int)perToPixel(rectXPer.get(), viewImageB.get().getWidth());
        final var rectY = (int)perToPixel(rectYPer.get(), viewImageB.get().getHeight());
        final var rectW = (int)perToPixel(rectWPer.get(), viewImageB.get().getWidth());
        final var rectH = (int)perToPixel(rectHPer.get(), viewImageB.get().getHeight());
        // 画素値の平均色を計算する
        long rSum = 0, gSum = 0, bSum = 0;
        for(int y = rectY; y < rectY + rectH; ++y) {
            for (int x = rectX; x < rectX + rectW; ++x) {
                final var color = viewImageB.get().getRGB(x, y);
                rSum += (color >>> 16) & 0xFF;
                gSum += (color >>> 8) & 0xFF;
                bSum += color & 0xFF;
            }
        }
        int rAve = (int)Math.round(1.0 * rSum / rectW / rectH);
        int gAve = (int)Math.round(1.0 * gSum / rectW / rectH);
        int bAve = (int)Math.round(1.0 * bSum / rectW / rectH);
        // クロップ後に出力
        rAve = (rAve < 0 ? 0 : rAve > 255 ? 255 : rAve);
        gAve = (gAve < 0 ? 0 : gAve > 255 ? 255 : gAve);
        bAve = (bAve < 0 ? 0 : bAve > 255 ? 255 : bAve);
        final var rAve2 = rAve;
        final var gAve2 = gAve;
        final var bAve2 = bAve;
        Platform.runLater(() -> AverageColor.set(String.format("(%d,%d,%d)", rAve2, gAve2, bAve2)));
    }
    /**
     * ラベルに表示する値の計算を行う
     */
    private void calcDHandAC(){
        calcDifferenceHash();
        calcAverageColor();
    }

    /**
     * コンストラクタ
     */
    public SceneHelperModel(){
        // ChangeListenerを設定
        viewImageB.addListener((observable, oldValue, newValue) -> {
            ViewImage.set(SwingFXUtils.toFXImage(newValue, null));
            calcDHandAC();
        });
        RectXPer.addListener((observable, oldValue, newValue) -> {
            try{
                final var rectX = Double.parseDouble(newValue) / divx;
                rectXPer.set(rectX < 0.0 ? 0.0 : rectX > 100.0 ? 100.0 : rectX);
                calcDHandAC();
            }catch(Exception e){}
        });
        RectYPer.addListener((observable, oldValue, newValue) -> {
            try{
                final var rectY = Double.parseDouble(newValue) / divy;
                rectYPer.set(rectY < 0.0 ? 0.0 : rectY > 100.0 ? 100.0 : rectY);
                calcDHandAC();
            }catch(Exception e){}
        });
        RectWPer.addListener((observable, oldValue, newValue) -> {
            try{
                final var rectW = Double.parseDouble(newValue) / divx;
                rectWPer.set(rectW < 0.0 ? 0.0 : rectW > 100.0 ? 100.0 : rectW);
                calcDHandAC();
            }catch(Exception e){}
        });
        RectHPer.addListener((observable, oldValue, newValue) -> {
            try{
                final var rectH = Double.parseDouble(newValue) / divy;
                rectHPer.set(rectH < 0.0 ? 0.0 : rectH > 100.0 ? 100.0 : rectH);
                calcDHandAC();
            }catch(Exception e){}
        });
    }
    /**
     * 画像データをセット
     * @param image 画像データ
     */
    public void setImage(BufferedImage image){
        if(image != null)
            viewImageB.set(image);
    }
}
