package com.ysrken.kamo.Service;

import java.awt.image.BufferedImage;
import java.time.Duration;

public class CharacterRecognitionService {
    /** 画像から遠征残り時間を取り出す*/
    public static Duration getExpeditionRemainingTime(BufferedImage image){

        return Duration.parse("PT3H4M5S");
    }
}
