package com.custom;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.ptr.IntByReference;
import java.lang.instrument.Instrumentation;

public class WindowHiderAgent {

    // Константы WinAPI
    private static final int WDA_EXCLUDEFROMCAPTURE = 0x00000011;
    private static final int GWL_EXSTYLE = -20;
    private static final long WS_EX_APPWINDOW = 0x00040000L;
    private static final long WS_EX_TOOLWINDOW = 0x00000080L;
    private static final int GW_OWNER = 4;
    private static final int SW_HIDE = 0;
    private static final int SW_SHOW = 5;

    public interface User32 extends StdCallLibrary {
        User32 INSTANCE = Native.load("user32", User32.class);

        interface WNDENUMPROC extends StdCallCallback {
            boolean callback(Pointer hWnd, Pointer arg);
        }

        boolean EnumWindows(WNDENUMPROC lpEnumFunc, Pointer arg);
        int GetWindowThreadProcessId(Pointer hWnd, IntByReference lpdwProcessId);
        Pointer GetWindow(Pointer hWnd, int uCmd);
        boolean IsWindowVisible(Pointer hWnd);
        int GetClassNameA(Pointer hWnd, byte[] lpClassName, int nMaxCount);
        boolean GetWindowDisplayAffinity(Pointer hWnd, IntByReference pdwAffinity);
        boolean SetWindowDisplayAffinity(Pointer hWnd, int dwAffinity);
        long GetWindowLongPtrW(Pointer hWnd, int nIndex);
        long SetWindowLongPtrW(Pointer hWnd, int nIndex, long dwNewLong);
        boolean ShowWindow(Pointer hWnd, int nCmdShow);
    }

    public interface Kernel32 extends StdCallLibrary {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);
        int GetCurrentProcessId();
    }

    // Точка входа Java Agent (вызывается до запуска Minecraft)
    public static void premain(String agentArgs, Instrumentation inst) {
        Thread watcher = new Thread(WindowHiderAgent::watchWindow, "WindowHiderThread");
        watcher.setDaemon(true);
        watcher.start();
    }

    private static void watchWindow() {
        int currentPid = Kernel32.INSTANCE.GetCurrentProcessId();

        while (true) {
            try {
                Pointer[] foundHwnd = new Pointer[1];

                User32.INSTANCE.EnumWindows((hWnd, arg) -> {
                    IntByReference pidRef = new IntByReference();
                    User32.INSTANCE.GetWindowThreadProcessId(hWnd, pidRef);

                    if (pidRef.getValue() == currentPid
                            && User32.INSTANCE.GetWindow(hWnd, GW_OWNER) == null
                            && User32.INSTANCE.IsWindowVisible(hWnd)) {

                        byte[] buffer = new byte[64];
                        int len = User32.INSTANCE.GetClassNameA(hWnd, buffer, buffer.length);
                        String className = new String(buffer, 0, len);

                        if ("GLFW30".equals(className) || "LWJGL".equals(className)) {
                            foundHwnd[0] = hWnd;
                            return false; // Останавливаем перебор
                        }
                    }
                    return true;
                }, null);

                if (foundHwnd[0] != null) {
                    applyFlags(foundHwnd[0]);
                }

                Thread.sleep(1000);
            } catch (Throwable ignored) {
                // Игнорируем временные ошибки при пересоздании окна
            }
        }
    }

    private static void applyFlags(Pointer hWnd) {
        // 1. Устанавливаем флаг 0x11 (защита от захвата экрана)
        IntByReference affinity = new IntByReference();
        if (User32.INSTANCE.GetWindowDisplayAffinity(hWnd, affinity)
                && affinity.getValue() != WDA_EXCLUDEFROMCAPTURE) {
            User32.INSTANCE.SetWindowDisplayAffinity(hWnd, WDA_EXCLUDEFROMCAPTURE);
        }

        // 2. Убираем окно с панели задач и из Alt+Tab
        long exStyle = User32.INSTANCE.GetWindowLongPtrW(hWnd, GWL_EXSTYLE);
        boolean isAppWindow = (exStyle & WS_EX_APPWINDOW) != 0;
        boolean isToolWindow = (exStyle & WS_EX_TOOLWINDOW) != 0;

        if (isAppWindow || !isToolWindow) {
            long newStyle = (exStyle & ~WS_EX_APPWINDOW) | WS_EX_TOOLWINDOW;
            User32.INSTANCE.ShowWindow(hWnd, SW_HIDE);
            User32.INSTANCE.SetWindowLongPtrW(hWnd, GWL_EXSTYLE, newStyle);
            User32.INSTANCE.ShowWindow(hWnd, SW_SHOW);
        }
    }
}
