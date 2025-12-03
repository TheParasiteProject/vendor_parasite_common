/*
 * Copyright (C) 2024 TheParasiteProject
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.util.custom;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @hide
 */
class CommonPropsUtils {

    private static final String TAG = CommonPropsUtils.class.getSimpleName();
    private static final boolean DEBUG = false;

    private static final Map<String, Field> fieldCache = new HashMap<>();
    private static Boolean isLargeScreen = null;

    protected static String[] getStringArrayResSafely(int resId) {
        try {
            return Resources.getSystem().getStringArray(resId);
        } catch (Resources.NotFoundException e) {
            return new String[0];
        }
    }

    protected static String getBuildID(String fingerprint) {
        Pattern pattern = Pattern.compile("([A-Za-z0-9]+\\.\\d+\\.\\d+\\.\\w+)");
        Matcher matcher = pattern.matcher(fingerprint);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    protected static String getDeviceName(String fingerprint) {
        String[] parts = fingerprint.split("/");
        if (parts.length >= 2) {
            return parts[1];
        }
        return "";
    }

    protected static boolean isDeviceTablet(Context context) {
        if (context == null) {
            return false;
        }
        return isLargeScreen(context);
    }

    private static boolean isLargeScreen(Context context) {
        if (isLargeScreen == null) {
            WindowManager windowManager = context.getSystemService(WindowManager.class);
            final Rect bounds = windowManager.getMaximumWindowMetrics().getBounds();
            float smallestWidth =
                    dpiFromPx(
                            Math.min(bounds.width(), bounds.height()),
                            context.getResources().getConfiguration().densityDpi);
            isLargeScreen = smallestWidth >= 600;
        }
        return isLargeScreen;
    }

    private static float dpiFromPx(float size, int densityDpi) {
        float densityRatio = (float) densityDpi / DisplayMetrics.DENSITY_DEFAULT;
        return (size / densityRatio);
    }

    protected static String getProcessName(Context context) {
        ActivityManager manager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) {
            return null;
        }

        List<RunningAppProcessInfo> runningProcesses = null;
        try {
            runningProcesses = manager.getRunningAppProcesses();
        } catch (Exception e) {
            return null;
        }

        if (runningProcesses == null) {
            return null;
        }

        String processName = null;
        for (RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.pid == android.os.Process.myPid()) {
                processName = processInfo.processName;
                break;
            }
        }
        return processName;
    }

    protected static void setPropValue(String key, String value) {
        try {
            dlog("Setting prop " + key + " to " + value.toString());
            Class clazz = Build.class;
            if (key.startsWith("VERSION.")) {
                clazz = Build.VERSION.class;
                key = key.substring(8);
            }
            Field field = clazz.getDeclaredField(key);
            field.setAccessible(true);
            // Cast the value to int/long if it's an integer/long field, otherwise string.
            field.set(
                    null,
                    field.getType().equals(Integer.TYPE)
                            ? Integer.parseInt(value)
                            : field.getType().equals(Long.TYPE) ? Long.parseLong(value) : value);
            field.setAccessible(false);
        } catch (Exception e) {
            dlog("Failed to set prop " + key + " " + e);
        }
    }

    protected static void dlog(String msg) {
        if (DEBUG) Log.d(TAG, msg);
    }
}
