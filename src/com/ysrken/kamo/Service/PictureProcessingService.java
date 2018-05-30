package com.ysrken.kamo.Service;

import java.awt.image.BufferedImage;

import static java.awt.Image.SCALE_SMOOTH;

public class PictureProcessingService {
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
     * 画像データの一部分を、一部分を選択した部分を拡大することによって覆い隠す
     * @param image 画像データ
     * @param xPer 選択左上座標の割合(X軸)
     * @param yPer 選択左上座標の割合(Y軸)
     * @param wPer 選択して拡大する横幅の割合
     * @param hPer 選択して拡大する部分の縦幅の割合
     * @return 隠した後の画像データ
     */
    private static BufferedImage blindImageByAreaStretch(BufferedImage image, double xPer, double yPer, double wPer, double hPer){
        // 画像の選択範囲(％)を選択範囲(ピクセル)に変換
        final var rectX = (int)perToPixel(xPer, image.getWidth());
        final var rectY = (int)perToPixel(yPer, image.getHeight());
        final var rectW = (int)perToPixel(wPer, image.getWidth());
        final var rectH = (int)perToPixel(hPer, image.getHeight());
        // 元画像から領域を選択してリサイズした画像を用意
        final var tempImage = image
                .getSubimage(rectX, rectY, 1, rectH)
                .getScaledInstance(rectW, rectH, SCALE_SMOOTH);
        // リサイズした画像を別のBufferedImageに書き出し
        final var canvas = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        final var g = canvas.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.drawImage(tempImage, rectX, rectY, null);
        g.dispose();
        return canvas;
    }
    /**
     * 画像データから名前部分を隠した画像を返す
     * @param image 画像データ
     * @return 名前部分が隠れた画像データ
     */
    private static BufferedImage blindUserName(BufferedImage image){
        // シーン判定を行う
        final var scene = SceneRecognitionService.judgeScene(image);
        // シーン認識結果から、画像のどの部分を覆うべきかを判定し、隠蔽操作を行う
        switch(scene){
            case "戦闘結果":
                return blindImageByAreaStretch(image, 93.0 / 8, 81.0 / 4.8, 170.0 / 8, 24.0 / 4.8);
            case "MVP":
                return blindImageByAreaStretch(image, 58.0 / 8, 81.0 / 4.8, 170.0 / 8, 24.0 / 4.8);
            default:
                return image;
        }
    }

    /**
     * 画像処理後のイメージを取得する
     * @param image 処理前
     * @return 処理後
     */
    public static BufferedImage getProcessedImage(BufferedImage image){
        if(SettingsStore.BlindNameTextFlg.get()){
            return blindUserName(image);
        }
        return image;
    }
}
