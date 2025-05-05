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
import android.os.Build;
import android.util.Log;

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
        return context.getResources().getConfiguration().smallestScreenWidthDp >= 600;
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

    protected static void setPropValue(String key, Object newValue) {
        try {
            Field field = getBuildClassField(key);
            if (field == null) {
                dlog("Field " + key + " not found in Build or Build.VERSION classes");
                return;
            }

            Object currentValue = field.get(null);
            if (areObjectsEqual(currentValue, newValue)) {
                return;
            }

            if (field.getType() == int.class) {
                field.setInt(
                        null,
                        newValue instanceof Integer
                                ? (Integer) newValue
                                : Integer.parseInt(newValue.toString()));
            } else if (field.getType() == long.class) {
                field.setLong(
                        null,
                        newValue instanceof Long
                                ? (Long) newValue
                                : Long.parseLong(newValue.toString()));
            } else {
                field.set(null, newValue.toString());
            }
            dlog("Set prop " + key + " to " + newValue);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            dlog("Failed to set prop " + key + " " + e);
        }
    }

    protected static boolean areObjectsEqual(Object oldValue, Object newValue) {
        if (oldValue == null) return newValue == null;
        return oldValue.toString().equals(newValue != null ? newValue.toString() : null);
    }

    protected static Field getBuildClassField(String key) {
        Field field = fieldCache.get(key);
        if (field != null) {
            return field;
        }

        try {
            field = Build.class.getDeclaredField(key);
            dlog("Field " + key + " found in Build.class");
        } catch (NoSuchFieldException e) {
            try {
                field = Build.VERSION.class.getDeclaredField(key);
                dlog("Field " + key + " found in Build.VERSION.class");
            } catch (NoSuchFieldException ex) {
                dlog("Field " + key + " not found " + ex);
                return null;
            }
        }

        field.setAccessible(true);
        fieldCache.put(key, field);
        return field;
    }

    protected static void dlog(String msg) {
        if (DEBUG) Log.d(TAG, msg);
    }
}
