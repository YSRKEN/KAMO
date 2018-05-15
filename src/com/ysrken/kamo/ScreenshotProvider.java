package com.ysrken.kamo;

import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;
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

    /**
     * 自動座標取得を試みる(成功したらtrue)
     */
    public static boolean getPosition(){
        try {
            // グラフィックデバイスの情報を取得する
            GraphicsDevice[] all_gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
            for (GraphicsDevice gd : all_gd) {
                // 各グラフィックデバイスにおけるグラフィックス特性を取得する
                GraphicsConfiguration[] all_gc = gd.getConfigurations();
                // 各グラフィックス特性に従い、その座標を取得してスクショを撮る
                Robot robot = new Robot(gd);
                for (GraphicsConfiguration gc : all_gc) {
                    BufferedImage imageData = robot.createScreenCapture(gc.getBounds());
                    // 撮ったスクショに対して、ゲーム画面を検索する
                    List<Rectangle> rectList = searchGamePosition(imageData);
                    // 検索にヒットした場合、その座標を取得して結果を返す
                    if(rectList.size() > 0){
                        ScreenshotProvider.robot = robot;
                        ScreenshotProvider.rect = rectList.get(0);
                        return true;
                    }
                }
            }
            return false;
        } catch (AWTException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 画像データから、ゲーム画面の候補を抽出する
     * @param image 画像データ
     * @return ゲーム画面の候補の一覧
     */
    private static List<Rectangle> searchGamePosition(BufferedImage image){
        List<Rectangle> rectList = new ArrayList<>();
        return rectList;
    }
}
