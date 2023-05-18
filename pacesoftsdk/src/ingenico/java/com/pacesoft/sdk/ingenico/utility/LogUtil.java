package com.pacesoft.sdk.ingenico.utility;

import android.util.Log;

import com.pacesoft.sdk.ingenico.IngenicoDeviceConfig;


public class LogUtil {

    public static void d(String message) {
        Log.d(IngenicoDeviceConfig.INSTANCE.getTAG(), message);
    }

    public static void e(String message) {
        Log.e(IngenicoDeviceConfig.INSTANCE.getTAG(), message);
    }
}
