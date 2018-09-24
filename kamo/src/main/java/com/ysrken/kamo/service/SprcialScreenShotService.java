package com.ysrken.kamo.service;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;
import com.sun.jna.win32.W32APIOptions;
import com.ysrken.kamo.BitmapImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;

/** WinAPIを叩くための準備*/
interface User32Ex extends W32APIOptions {
    User32Ex instance = (User32Ex) Native.loadLibrary("user32", User32Ex.class, DEFAULT_OPTIONS);
    WinDef.HWND WindowFromPoint(long point);
    WinDef.HWND ChildWindowFromPoint(WinDef.HWND hWnd, long point);
    WinDef.HWND GetDesktopWindow();
    int GetSystemMetrics(int nIndex);
}

interface Gdi32Ex extends W32APIOptions {
    Gdi32Ex instance = (Gdi32Ex) Native.loadLibrary("gdi32", Gdi32Ex.class, DEFAULT_OPTIONS);
    WinDef.HDC CreateDCW(String lpszDriver, long lpszDevice, long lpszOutput, long lpInitData);
    WinDef.BOOL DeleteDC(WinDef.HDC hdc);
    WinDef.BOOL BitBlt(WinDef.HDC hdc, int nXDest, int nYDest, int nWidth, int nHeight, WinDef.HDC hdcSrc, int nXSrc, int nYSrc, int dwRop);
}

@Component
public class SprcialScreenShotService {
    /** 対象のウィンドウハンドル */
    private WinDef.HWND windowHandle = null;

    /** 取得したい矩形 */
    private Rectangle rect = null;

    /** 取得したい矩形より1ピクセル大きい範囲 */
    private Rectangle rectForCheck = null;

    /** 特殊な定数 */
    private final int CAPTUREBLT = 0x40000000;
    private final int SM_XVIRTUALSCREEN = 76;
    private final int SM_YVIRTUALSCREEN = 77;
    private final int SM_CXVIRTUALSCREEN = 78;
    private final int SM_CYVIRTUALSCREEN = 79;
    private final int SRCCOPY = 0x00CC0020;
    private final DirectColorModel SCREENSHOT_COLOR_MODEL = new DirectColorModel(24, 0x00FF0000, 0xFF00, 0xFF);
    private final int[] SCREENSHOT_BAND_MASKS = {
            SCREENSHOT_COLOR_MODEL.getRedMask(),
            SCREENSHOT_COLOR_MODEL.getGreenMask(),
            SCREENSHOT_COLOR_MODEL.getBlueMask()
    };

    /**
     * 各種サービス
     */
    @Autowired
    private LoggerService logger;

    /**
     * コンストラクタ
     */
    public SprcialScreenShotService() {
        System.out.println("DEBUG MainApp - SprcialScreenShotService#SprcialScreenShotService");
    }

    /**
     * 自動座標取得を試みる(成功したらtrue)
     */
    public boolean trySearchGamePosition(Rectangle rect, Rectangle rectForCheck, int frameColor) {
        windowHandle = null;
        try {
            // WinAPIを叩き、対象ゲーム画面のウィンドウハンドルを取得する
            final int px = rect.x + rect.width / 2;
            final int py = rect.y + rect.height / 2;
            final long pp = ((long) py << 32) | (long) px;
            WinDef.HWND hwnd = User32Ex.instance.WindowFromPoint(pp);
            WinDef.RECT windowRect = new WinDef.RECT();
            User32.INSTANCE.GetWindowRect(hwnd, windowRect);
            // ウィンドウハンドルが正常に取得されない際は再帰的に判断する
            while (true) {
                // 子ウィンドウのハンドルを調べる
                final WinDef.HWND hwnd2 = User32Ex.instance.ChildWindowFromPoint(hwnd, pp);
                if (hwnd2 == null || hwnd.equals(hwnd2))
                    break;
                // 対象ハンドルのウィンドウサイズを調べる
                final WinDef.RECT windowRect2 = new WinDef.RECT();
                // サイズが小さすぎる場合はループを抜け、そうでない場合はウィンドウ情報を更新する
                User32.INSTANCE.GetWindowRect(hwnd, windowRect2);
                if (windowRect2.right - windowRect2.left < rect.width
                        || windowRect2.bottom - windowRect2.top < rect.height) {
                    break;
                } else {
                    hwnd = hwnd2;
                    windowRect = windowRect2;
                }
            }
            // とりあえず撮影してみて、真っ黒じゃないかによって取得可能かを判定する
            final BufferedImage image = GDI32Util.getScreenshot(hwnd)
                    .getSubimage(rect.x - windowRect.left, rect.y - windowRect.top, rect.width, rect.height);
            if (image.getRGB(0, 0) != Color.black.getRGB()) {
                windowHandle = hwnd;
                this.rect = new Rectangle(
                        rect.x - windowRect.left, rect.y - windowRect.top,
                        rect.width, rect.height);
                this.rectForCheck = new Rectangle(
                        rect.x - windowRect.left - 1, rect.y - windowRect.top - 1,
                        rect.width + 2, rect.height + 2);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * スクリーンショットを取得できる状態ならtrue
     */
    public boolean canGetScreenshot(){
        return (windowHandle != null);
    }

    /**
     * スクリーンショットを取得する
     */
    public BufferedImage getScreenshot(){
        return GDI32Util.getScreenshot(windowHandle).getSubimage(rect.x, rect.y, rect.width, rect.height);
    }

    /**
     * スクリーンショットを取得する(デスクトップ全体のイメージからクロップするタイプ)
     * 参考：https://stackoverflow.com/questions/4433994/java-window-image
     * @param rect 取得範囲(スクリーン座標)
     * @return 切り取ったイメージ
     */
    public BufferedImage getBasicScreenshot(Rectangle rect){
        WinDef.HDC hdcWindow = Gdi32Ex.instance.CreateDCW("DISPLAY", 0, 0, 0);
        WinDef.HDC hdcMemDC = GDI32.INSTANCE.CreateCompatibleDC(hdcWindow);

        WinDef.HBITMAP hBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(hdcWindow, rect.width, rect.height);

        WinNT.HANDLE hOld = GDI32.INSTANCE.SelectObject(hdcMemDC, hBitmap);
        Gdi32Ex.instance.BitBlt(hdcMemDC, 0, 0, rect.width, rect.height, hdcWindow, rect.x, rect.y, SRCCOPY);

        GDI32.INSTANCE.SelectObject(hdcMemDC, hOld);
        GDI32.INSTANCE.DeleteDC(hdcMemDC);

        WinGDI.BITMAPINFO bmi = new WinGDI.BITMAPINFO();
        bmi.bmiHeader.biWidth = rect.width;
        bmi.bmiHeader.biHeight = -rect.height;
        bmi.bmiHeader.biPlanes = 1;
        bmi.bmiHeader.biBitCount = 32;
        bmi.bmiHeader.biCompression = WinGDI.BI_RGB;

        Memory buffer = new Memory(rect.width * rect.height * 4);
        GDI32.INSTANCE.GetDIBits(hdcWindow, hBitmap, 0, rect.height, buffer, bmi, WinGDI.DIB_RGB_COLORS);

        BufferedImage image = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, rect.width, rect.height, buffer.getIntArray(0, rect.width * rect.height), 0, rect.width);

        GDI32.INSTANCE.DeleteObject(hBitmap);
        Gdi32Ex.instance.DeleteDC(hdcWindow);

        return image;
    }

    /**
     * スクリーンショットを取得する(チェック用)
     */
    public BufferedImage getScreenshotForCheck() {
        return GDI32Util.getScreenshot(windowHandle).getSubimage(rectForCheck.x, rectForCheck.y, rectForCheck.width, rectForCheck.height);
    }
}
