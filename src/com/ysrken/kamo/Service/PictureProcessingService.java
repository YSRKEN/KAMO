package com.ysrken.kamo.Service;

import com.ysrken.kamo.BitmapImage;

import java.awt.image.BufferedImage;

import static java.awt.Image.SCALE_SMOOTH;

public class PictureProcessingService {
    /**
     * 画像データの一部分を、一部分を選択した部分を拡大することによって覆い隠す
     * wPerが負数の場合、左から右ではなく右から左に上書きする
     * @param image 画像データ
     * @param xPer 選択左上座標の割合(X軸)
     * @param yPer 選択左上座標の割合(Y軸)
     * @param wPer 選択して拡大する横幅の割合
     * @param hPer 選択して拡大する部分の縦幅の割合
     * @return 隠した後の画像データ
     */
    private static BufferedImage blindImageByAreaStretch(BufferedImage image, double xPer, double yPer, double wPer, double hPer){
        final var reverseFlg = (wPer < 0.0);
        // 元画像から領域を切り取ってリサイズした画像を用意する
        final var rectX = (int)Math.round(Math.abs(xPer) * image.getWidth() / 100);
        final var rectY = (int)Math.round(Math.abs(yPer) * image.getHeight() / 100);
        final var rectW = (int)Math.round(Math.abs(wPer) * image.getWidth() / 100);
        final var rectH = (int)Math.round(Math.abs(hPer) * image.getHeight() / 100);
        final var blind = BitmapImage.of(image).crop(rectX, rectY, 1, rectH).resize(rectW, rectH);
        // 画像を貼り付ける
        if(reverseFlg) {
            return BitmapImage.of(image).paste(blind, rectX - rectW, rectY).getImage();
        }else{
            return BitmapImage.of(image).paste(blind, rectX, rectY).getImage();
        }
    }
    /**
     * 画像データから名前部分を隠した画像を返す
     * @param image 画像データ
     * @return 名前部分が隠れた画像データ
     */
    private static BufferedImage blindUserName(BufferedImage image){
        // シーン判定を行う
        final var scene = SceneRecognitionService.judgeScene(image);
        final var isNearlyHomeFlg = SceneRecognitionService.isNearlyHomeScene(image);
        // シーン認識結果から、画像のどの部分を覆うべきかを判定し、隠蔽操作を行う
        if(isNearlyHomeFlg){
            image = blindImageByAreaStretch(image, 277.0 / 8, 4.0 / 4.8, -165.0 / 8, 20.0 / 4.8);
        }
        switch(scene){
            case "戦闘結果":
                return blindImageByAreaStretch(image, 93.0 / 8, 81.0 / 4.8, 170.0 / 8, 24.0 / 4.8);
            case "MVP":
                return blindImageByAreaStretch(image, 58.0 / 8, 81.0 / 4.8, 170.0 / 8, 24.0 / 4.8);
            case "艦隊司令部情報":
                return blindImageByAreaStretch(image, 201.0 / 8, 123.0 / 4.8, 295.0 / 8, 26.0 / 4.8);
            case "ランキング":
                return blindImageByAreaStretch(image, 228.0 / 8, 157.0 / 4.8, 146.0 / 8, 290.0 / 4.8);
            case "演習一覧":
                image = blindImageByAreaStretch(image, 336.0 / 8, 178.0 / 4.8, 165.0 / 8, 15.0 / 4.8);
                image = blindImageByAreaStretch(image, 336.0 / 8, 233.0 / 4.8, 165.0 / 8, 15.0 / 4.8);
                image = blindImageByAreaStretch(image, 336.0 / 8, 288.0 / 4.8, 165.0 / 8, 15.0 / 4.8);
                image = blindImageByAreaStretch(image, 336.0 / 8, 343.0 / 4.8, 165.0 / 8, 15.0 / 4.8);
                return  blindImageByAreaStretch(image, 336.0 / 8, 398.0 / 4.8, 165.0 / 8, 15.0 / 4.8);
            case "演習個別":
                return  blindImageByAreaStretch(image, 129.0 / 8, 85.0 / 4.8, 285.0 / 8, 27.0 / 4.8);
            case "遠征結果":
                return  blindImageByAreaStretch(image, 62.0 / 8, 79.0 / 4.8, 172.0 / 8, 19.0 / 4.8);
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
