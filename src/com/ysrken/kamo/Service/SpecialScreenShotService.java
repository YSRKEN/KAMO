package com.ysrken.kamo.Service;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.GDI32Util;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.W32APIOptions;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;

/** WinAPIを叩くための準備*/
interface User32Ex extends W32APIOptions {
    User32Ex instance = (User32Ex) Native.loadLibrary("user32", User32Ex.class, DEFAULT_OPTIONS);
    WinDef.HWND WindowFromPoint(long point);
    WinDef.HDC GetWindowDC(WinDef.HWND hWnd);
    WinDef.HWND ChildWindowFromPoint(WinDef.HWND hWnd, long point);
}

public class SpecialScreenShotService {
    /** 対象のウィンドウハンドル */
    private static WinDef.HWND windowHandle = null;
    /** 取得したい矩形 */
    private static Rectangle rect;
    /** 取得したい矩形より1ピクセル大きい範囲 */
    private static Rectangle rectForCheck;
    /** 特殊な定数 */
    private static int CAPTUREBLT = 0x40000000;
    private static final DirectColorModel SCREENSHOT_COLOR_MODEL = new DirectColorModel(24, 0x00FF0000, 0xFF00, 0xFF);
    private static final int[] SCREENSHOT_BAND_MASKS = {
            SCREENSHOT_COLOR_MODEL.getRedMask(),
            SCREENSHOT_COLOR_MODEL.getGreenMask(),
            SCREENSHOT_COLOR_MODEL.getBlueMask()
    };

    /** 初期化コード(成功したらtrue) */
    public static boolean initialize(Rectangle rect, Rectangle rectForCheck, int frameColor){
        windowHandle = null;
        try{
            // WinAPIを叩き、対象ゲーム画面のウィンドウハンドルを取得する
            final var px = rect.x + rect.width / 2;
            final var py = rect.y + rect.height / 2;
            final long pp = ((long)py << 32) | (long)px;
            var hwnd = User32Ex.instance.WindowFromPoint(pp);
            var windowRect = new WinDef.RECT();
            User32.INSTANCE.GetWindowRect(hwnd, windowRect);
            // ウィンドウハンドルが正常に取得されない際は再帰的に判断する
            while(true){
                // 子ウィンドウのハンドルを調べる
                final var hwnd2 = User32Ex.instance.ChildWindowFromPoint(hwnd, pp);
                if(hwnd2 == null || hwnd.equals(hwnd2))
                    break;
                // 対象ハンドルのウィンドウサイズを調べる
                final var windowRect2 = new WinDef.RECT();
                // サイズが小さすぎる場合はループを抜け、そうでない場合はウィンドウ情報を更新する
                User32.INSTANCE.GetWindowRect(hwnd, windowRect2);
                if(windowRect2.right - windowRect2.left < rect.width
                || windowRect2.bottom - windowRect2.top < rect.height){
                    break;
                }else{
                    hwnd = hwnd2;
                    windowRect = windowRect2;
                }
            }
            // とりあえず撮影してみて、真っ黒じゃないかによって取得可能かを判定する
            final var image = GDI32Util.getScreenshot(hwnd)
                    .getSubimage(rect.x - windowRect.left, rect.y - windowRect.top, rect.width, rect.height);
            if(image.getRGB(0, 0) != Color.black.getRGB()) {
                windowHandle = hwnd;
                SpecialScreenShotService.rect = new Rectangle(
                        rect.x - windowRect.left, rect.y - windowRect.top,
                        rect.width, rect.height);
                SpecialScreenShotService.rectForCheck = new Rectangle(
                        rect.x - windowRect.left - 1, rect.y - windowRect.top - 1,
                        rect.width + 2, rect.height + 2);
                return true;
            }else{
                return false;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
    /** 特殊なスクリーンショットを撮影できるか？ */
    public static boolean canSpecialScreenShot(){
        return (windowHandle != null);
    }
    /** 特殊なスクリーンショットを撮影する */
    public static BufferedImage getScreenshot(){
        return GDI32Util.getScreenshot(windowHandle).getSubimage(rect.x, rect.y, rect.width, rect.height);
    }
    /** 特殊なスクリーンショットを撮影する(チェック用) */
    public static BufferedImage getScreenshotForCheck(){
        return GDI32Util.getScreenshot(windowHandle).getSubimage(rectForCheck.x, rectForCheck.y, rectForCheck.width, rectForCheck.height);
    }
}
