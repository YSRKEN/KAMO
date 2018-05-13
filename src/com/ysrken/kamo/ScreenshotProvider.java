package com.ysrken.kamo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ScreenshotProvider {
    /**
     * 撮影用のRobot
     */
    private static Robot robot = null;
    /**
     * 撮影範囲
     */
    private static Rectangle rect = null;

    /**
     * 初期化
     */
    public static void initialize(){}
    public static void test() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-SSS");
            // グラフィックデバイスの情報を取得する
            GraphicsDevice[] all_gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
            for (GraphicsDevice gd : all_gd) {
                // 各グラフィックデバイスにおけるグラフィックス特性を取得する
                GraphicsConfiguration[] all_gc = gd.getConfigurations();
                // 各グラフィックス特性に従い、その座標を取得してスクショを撮る
                Robot robot = new Robot(gd);
                for (GraphicsConfiguration gc : all_gc) {
                    BufferedImage imageData = robot.createScreenCapture(gc.getBounds());
                    String fileName = sdf.format(Calendar.getInstance().getTime()) + ".png";
                    ImageIO.write(imageData, "png", new File(fileName));
                }
            }
        } catch (AWTException | IOException e) {
            e.printStackTrace();
        }
    }
}
