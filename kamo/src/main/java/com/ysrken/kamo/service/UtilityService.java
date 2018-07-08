package com.ysrken.kamo.service;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class UtilityService {
	/**
	 * Windows OS上で動作しているかを判定
	 */
    public boolean isWindows(){
        final String osName = System.getProperty("os.name").toLowerCase();
        return (osName.startsWith("windows"));
    }
    
    /**
     * 現在の日時を「2006-01-02 03-04-05-890」形式で取得
     * @return 現在の日時文字列
     */
    private static DateTimeFormatter dtfLong = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss-SSS");
    public static String getDateStringLong(){
        return LocalDateTime.now().format(dtfLong);
    }
    
    /**
     * 現在の日時を「03:04:05」形式で取得
     * @return 現在の日時文字列
     */
    private final DateTimeFormatter dtfShort = DateTimeFormatter.ofPattern("HH:mm:ss");
    public String getDateStringShort(){
        return LocalDateTime.now().format(dtfShort);
    }
    
    /**
     * 色間のRGB色空間における距離を計算する
     * @param a 色1
     * @param b 色2
     * @return 距離
     */
    public int calcColorDistance(Color a, Color b){
        final int rDiff = a.getRed() - b.getRed();
        final int gDiff = a.getGreen() - b.getGreen();
        final int bDiff = a.getBlue() - b.getBlue();
        return rDiff * rDiff + gDiff * gDiff + bDiff * bDiff;
    }
    
    /**
     * ビットカウント
     * 参考→http://developer.cybozu.co.jp/takesako/2006/11/binary_hacks.html
     * @param x long型(64bit)の値
     * @return ビットカウント後の数
     */
    private long popcnt(long x) {
        x = ((x & 0xaaaaaaaaaaaaaaaaL) >> 1) + (x & 0x5555555555555555L);
        x = ((x & 0xccccccccccccccccL) >> 2) + (x & 0x3333333333333333L);
        x = ((x & 0xf0f0f0f0f0f0f0f0L) >> 4) + (x & 0x0f0f0f0f0f0f0f0fL);
        x = ((x & 0xff00ff00ff00ff00L) >> 8) + (x & 0x00ff00ff00ff00ffL);
        x = ((x & 0xffff0000ffff0000L) >> 16) + (x & 0x0000ffff0000ffffL);
        x = ((x & 0xffffffff00000000L) >> 32) + (x & 0x00000000ffffffffL);
        return x;
    }
    
    /**
     * ハミング距離を計算する
     * @param a 値1
     * @param b 値2
     * @return ハミング距離
     */
    public long calcHummingDistance(long a, long b) {
        return popcnt(a ^ b);
    }
}
