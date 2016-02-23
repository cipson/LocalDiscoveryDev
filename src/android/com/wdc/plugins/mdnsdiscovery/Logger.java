package com.wdc.plugins.mdnsdiscovery;
import android.util.Log;

/**
 * Copyright 2015 Western Digital Corporation. All rights reserved.
 */
public class Logger {
    private static boolean debugEnabled = true;

    public static boolean isDebugEnabled() {
        return debugEnabled;
    }

    public static void setDebugEnabled(boolean debugEnabled) {
        Logger.debugEnabled = debugEnabled;
    }

    public static void e(String tag, String message){
        if (debugEnabled) { Log.e(tag, message); }
    }

    public static void d(String tag, String message){
        if (debugEnabled) { Log.d(tag, message); }
    }
    public static void w(String tag, String message){
        if (debugEnabled) { Log.w(tag, message); }
    }
}
