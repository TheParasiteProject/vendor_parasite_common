/*
 * Copyright (C) 2022 The Pixel Experience Project
 *               2021-2022 crDroid Android Project
 * Copyright (C) 2022 Paranoid Android
 * Copyright (C) 2022 StatiXOS
 * Copyright (C) 2023 the RisingOS Android Project
 *           (C) 2023 ArrowOS
 *           (C) 2023 The LibreMobileOS Foundation
 *           (C) 2019-2024 The Evolution X Project
 *           (C) 2024 TheParasiteProject
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
import android.app.ActivityTaskManager;
import android.app.ActivityTaskManager.RootTaskInfo;
import android.app.TaskStackListener;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Build;
import android.os.Process;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.android.internal.util.custom.certification.Android;

import org.lineageos.platform.internal.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @hide
 */
public final class PixelPropsUtils {

    private static final String TAG = PixelPropsUtils.class.getSimpleName();
    private static final boolean DEBUG = false;

    private static Boolean sEnablePixelProps =
            SystemProperties.getBoolean("persist.sys.pihooks.enable", true);

    private static final String sDeviceModel =
            SystemProperties.get("ro.product.model", Build.MODEL);
    private static final String sDeviceFingerprint =
            SystemProperties.get("ro.product.fingerprint", Build.FINGERPRINT);
    private static final Boolean sDeviceIsPixel =
            SystemProperties.get("ro.product.manufacturer", "").toLowerCase().contains("google");
    private static final Boolean sForceSpoofGmsProcessToDevice =
            SystemProperties.getBoolean("persist.sys.pihooks.force.spoof.gms.process", false);
    private static final String sNetflixModel =
            SystemProperties.get("persist.sys.pihooks.netflix_model", "");

    private static final String PACKAGE_NETFLIX = "com.netflix.mediaclient";
    private static final String PACKAGE_SETTINGS_INTELLIGENCE =
            "com.google.android.settings.intelligence";
    private static final String PACKAGE_ARCORE = "com.google.ar.core";
    private static final String PACKAGE_PHOTOS = "com.google.android.apps.photos";
    private static final String PACKAGE_GMS = "com.google.android.gms";
    private static final String PROCESS_GMS_UI = PACKAGE_GMS + ".ui";
    private static final String PROCESS_GMS_UNSTABLE = PACKAGE_GMS + ".unstable";
    private static final ComponentName GMS_ADD_ACCOUNT_ACTIVITY =
            ComponentName.unflattenFromString(
                    PACKAGE_GMS + "/.auth.uiflows.minutemaid.MinuteMaidActivity");

    private static final Map<String, Object> propsToChangeGeneric;
    private static final Map<String, Object> propsToChangeDevice;
    private static final Map<String, ArrayList<String>> propsToKeep;

    static {
        propsToKeep = new HashMap<>();
        propsToChangeGeneric = new HashMap<>();
        propsToChangeGeneric.put("TYPE", "user");
        propsToChangeGeneric.put("TAGS", "release-keys");
        propsToChangeDevice = new HashMap<>();
        propsToChangeDevice.put("BRAND", Build.BRAND);
        propsToChangeDevice.put("MANUFACTURER", Build.MANUFACTURER);
        propsToChangeDevice.put("ID", Build.ID);
        propsToChangeDevice.put("DEVICE", Build.DEVICE);
        propsToChangeDevice.put("PRODUCT", Build.PRODUCT);
        propsToChangeDevice.put("HARDWARE", Build.HARDWARE);
        propsToChangeDevice.put("MODEL", Build.MODEL);
        propsToChangeDevice.put("FINGERPRINT", Build.FINGERPRINT);
    }

    private static String[] getStringArrayResSafely(int resId) {
        String[] strArr = Resources.getSystem().getStringArray(resId);
        if (strArr == null) strArr = new String[0];
        return strArr;
    }

    private static Map<String, Object> getPropsToChangePixelXL() {
        return createGoogleSpoofProps(getStringArrayResSafely(R.array.config_piHookPropsPixelXL));
    }

    private static Map<String, Object> getPropsToChangePixelLegacy() {
        return createGoogleSpoofProps(
                getStringArrayResSafely(R.array.config_piHookPropsPixelLegacy));
    }

    private static Map<String, Object> getPropsToChangePixelTablet() {
        return createGoogleSpoofProps(
                getStringArrayResSafely(R.array.config_piHookPropsPixelTablet));
    }

    private static Map<String, Object> getPropsToChangePixelExtra() {
        return createGoogleSpoofProps(
                getStringArrayResSafely(R.array.config_piHookPropsPixelExtra));
    }

    private static ArrayList<String> getPackagesToChangePixelExtra() {
        return new ArrayList<String>(
                Arrays.asList(getStringArrayResSafely(R.array.config_piHookProcessPixelExtra)));
    }

    private static Map<String, Object> getPropsToChangePixelRecent() {
        return createGoogleSpoofProps(
                getStringArrayResSafely(R.array.config_piHookPropsPixelRecent));
    }

    private static ArrayList<String> getPackagesToChangePixelRecent() {
        return new ArrayList<String>(
                Arrays.asList(getStringArrayResSafely(R.array.config_piHookProcessPixelRecent)));
    }

    // Although the name is getProcessToChangePixelLegacy,
    // this list also applied to actual Pixel devices for unspoofing
    private static ArrayList<String> getProcessToChangePixelLegacy() {
        return new ArrayList<String>(
                Arrays.asList(getStringArrayResSafely(R.array.config_piHookProcessPixelLegacy)));
    }

    private static ArrayList<String> getProcessToKeep() {
        return new ArrayList<String>(
                Arrays.asList(getStringArrayResSafely(R.array.config_piHookProcessKeep)));
    }

    private static String getBuildID(String fingerprint) {
        Pattern pattern = Pattern.compile("([A-Za-z0-9]+\\.\\d+\\.\\d+\\.\\w+)");
        Matcher matcher = pattern.matcher(fingerprint);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private static String getDeviceName(String fingerprint) {
        String[] parts = fingerprint.split("/");
        if (parts.length >= 2) {
            return parts[1];
        }
        return "";
    }

    private static Map<String, Object> createSpoofProps(String[] config) {
        Map<String, Object> props = new HashMap<>();

        if (config == null || config.length != 4) {
            dlog("createSpoofProps: Config is empty");
            return props;
        }

        final String brand = config[0];
        final String manufacturer = config[1];
        final String model = config[2];
        final String fingerprint = config[3];
        if (TextUtils.isEmpty(model)
                || TextUtils.isEmpty(fingerprint)
                || model.contains("/")
                || !fingerprint.contains("/")) {
            dlog("createSpoofProps: Config is invalid");
            return props;
        }

        props.put("BRAND", brand);
        props.put("MANUFACTURER", manufacturer);
        props.put("ID", getBuildID(fingerprint));
        props.put("DEVICE", getDeviceName(fingerprint));
        props.put("PRODUCT", getDeviceName(fingerprint));
        props.put("HARDWARE", getDeviceName(fingerprint));
        props.put("MODEL", model);
        props.put("FINGERPRINT", fingerprint);
        props.put("TYPE", "user");
        props.put("TAGS", "release-keys");
        return props;
    }

    private static Map<String, Object> createGoogleSpoofProps(String[] config) {
        Map<String, Object> props = new HashMap<>();

        if (config == null || config.length != 2) {
            dlog("createGoogleSpoofProps: Config is empty");
            return props;
        }

        final String model = config[0];
        final String fingerprint = config[1];
        if (TextUtils.isEmpty(model)
                || TextUtils.isEmpty(fingerprint)
                || model.contains("/")
                || !fingerprint.contains("/")) {
            dlog("createGoogleSpoofProps: Config is invalid");
            return props;
        }

        props.put("BRAND", "google");
        props.put("MANUFACTURER", "Google");
        props.put("ID", getBuildID(fingerprint));
        props.put("DEVICE", getDeviceName(fingerprint));
        props.put("PRODUCT", getDeviceName(fingerprint));
        props.put("HARDWARE", getDeviceName(fingerprint));
        props.put("MODEL", model);
        props.put("FINGERPRINT", fingerprint);
        props.put("TYPE", "user");
        props.put("TAGS", "release-keys");
        return props;
    }

    private static boolean isDeviceTablet(Context context) {
        if (context == null) {
            return false;
        }
        Configuration configuration = context.getResources().getConfiguration();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager =
                (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        }
        return (configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                        >= Configuration.SCREENLAYOUT_SIZE_LARGE
                || displayMetrics.densityDpi == DisplayMetrics.DENSITY_XHIGH
                || displayMetrics.densityDpi == DisplayMetrics.DENSITY_XXHIGH
                || displayMetrics.densityDpi == DisplayMetrics.DENSITY_XXXHIGH;
    }

    private static String getProcessName(Context context) {
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

    private static boolean isGmsAddAccountActivityOnTop() {
        try {
            final RootTaskInfo focusedTask =
                    ActivityTaskManager.getService().getFocusedRootTaskInfo();
            return focusedTask != null && GMS_ADD_ACCOUNT_ACTIVITY.equals(focusedTask.topActivity);
        } catch (Exception e) {
            Log.e(TAG, "Unable to get top activity!", e);
        }
        return false;
    }

    private static boolean shouldTryToCertifyDevice(Context context) {
        if (!Android.isCertHookEnabled()) return false;

        final String processName = getProcessName(context);
        if (TextUtils.isEmpty(processName)) return false;

        if (!PROCESS_GMS_UNSTABLE.equals(processName)) return false;

        if (Android.isCertifiedPropsEmpty()) return false;

        final boolean was = isGmsAddAccountActivityOnTop();
        final TaskStackListener taskStackListener =
                new TaskStackListener() {
                    @Override
                    public void onTaskStackChanged() {
                        final boolean is = isGmsAddAccountActivityOnTop();
                        if (is ^ was) {
                            dlog(
                                    "GmsAddAccountActivityOnTop is:"
                                            + is
                                            + " was:"
                                            + was
                                            + ", killing myself!");
                            // process will restart automatically later
                            Process.killProcess(Process.myPid());
                        }
                    }
                };
        if (!was) {
            Android.newApplication();
        } else {
            dlog("Skip spoofing build for GMS, because GmsAddAccountActivityOnTop!");
        }
        try {
            ActivityTaskManager.getService().registerTaskStackListener(taskStackListener);
        } catch (Exception e) {
            Log.e(TAG, "Failed to register task stack listener!", e);
        }
        return true;
    }

    public static boolean setPropsForGphotos(Context context) {
        if (context == null) return false;

        final String packageName = context.getPackageName();
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }

        if (PACKAGE_PHOTOS.equals(packageName)) {
            if (SystemProperties.getBoolean("persist.sys.pixelprops.gphotos", false)) {
                getPropsToChangePixelXL().forEach((k, v) -> setPropValue(k, v));
                return true;
            }
        }
        return false;
    }

    public static void setProps(Context context) {
        if (!sEnablePixelProps) {
            dlog("Pixel props is disabled by config");
            if (shouldTryToCertifyDevice(context)) return;
            setPropsForGphotos(context);
            return;
        }

        if (context == null) return;

        final String packageName = context.getPackageName();
        if (TextUtils.isEmpty(packageName)) {
            return;
        }

        final String processName = getProcessName(context);
        if (TextUtils.isEmpty(processName)) return;

        propsToChangeGeneric.forEach((k, v) -> setPropValue(k, v));

        final boolean sIsTablet = isDeviceTablet(context);

        if (PACKAGE_GMS.equals(packageName)) {
            setPropValue("TIME", System.currentTimeMillis());
        }

        if (shouldTryToCertifyDevice(context)) {
            return;
        }

        if (getProcessToKeep().contains(processName)) {
            return;
        }

        if (setPropsForGphotos(context)) {
            return;
        }

        Map<String, Object> propsToChange = new HashMap<>();
        if (getProcessToChangePixelLegacy().contains(processName)) {
            if (!sForceSpoofGmsProcessToDevice) {
                propsToChange = getPropsToChangePixelLegacy();
            } else {
                propsToChange = propsToChangeDevice;
            }
        } else if (getPackagesToChangePixelExtra().contains(processName)
                || getPackagesToChangePixelExtra().contains(packageName)) {
            propsToChange = getPropsToChangePixelExtra();
        } else if (getPackagesToChangePixelRecent().contains(processName)
                || getPackagesToChangePixelRecent().contains(packageName)) {
            propsToChange = getPropsToChangePixelRecent();
        } else if (sIsTablet) {
            propsToChange = getPropsToChangePixelTablet();
        }

        if (propsToChange == null || propsToChange.isEmpty()) return;

        dlog("Defining props for: " + packageName);
        for (Map.Entry<String, Object> prop : propsToChange.entrySet()) {
            String key = prop.getKey();
            Object value = prop.getValue();
            if (propsToKeep.containsKey(packageName)
                    && propsToKeep.get(packageName).contains(key)) {
                dlog("Not defining " + key + " prop for: " + packageName);
                continue;
            }
            dlog("Defining " + key + " prop for: " + packageName);
            setPropValue(key, value);
        }
        // Show correct model name on gms services
        if (PROCESS_GMS_UI.equals(processName)) {
            setPropValue("MODEL", sDeviceModel);
            return;
        }
        // Set proper indexing fingerprint
        if (PACKAGE_SETTINGS_INTELLIGENCE.equals(packageName)) {
            setPropValue("FINGERPRINT", Build.VERSION.INCREMENTAL);
            return;
        }
        if (PACKAGE_ARCORE.equals(packageName)) {
            setPropValue("FINGERPRINT", sDeviceFingerprint);
            return;
        }
        if (!TextUtils.isEmpty(sNetflixModel) && PACKAGE_NETFLIX.equals(packageName)) {
            dlog("Setting model to " + sNetflixModel + " for Netflix");
            setPropValue("MODEL", sNetflixModel);
            return;
        }
    }

    private static void setPropValue(String key, Object value) {
        try {
            if (value == null || (value instanceof String && ((String) value).isEmpty())) {
                dlog(TAG + " Skipping setting empty value for key: " + key);
                return;
            }
            dlog(TAG + " Setting property for key: " + key + ", value: " + value.toString());
            Field field;
            Class<?> targetClass;
            try {
                targetClass = Build.class;
                field = targetClass.getDeclaredField(key);
            } catch (NoSuchFieldException e) {
                targetClass = Build.VERSION.class;
                field = targetClass.getDeclaredField(key);
            }
            if (field != null) {
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                if (fieldType == int.class || fieldType == Integer.class) {
                    if (value instanceof Integer) {
                        field.set(null, value);
                    } else if (value instanceof String) {
                        int convertedValue = Integer.parseInt((String) value);
                        field.set(null, convertedValue);
                        dlog(TAG + " Converted value for key " + key + ": " + convertedValue);
                    }
                } else if (fieldType == long.class || fieldType == Long.class) {
                    if (value instanceof Long) {
                        field.set(null, value);
                    } else if (value instanceof String) {
                        long convertedValue = Long.parseLong((String) value);
                        field.set(null, convertedValue);
                        dlog(TAG + " Converted value for key " + key + ": " + convertedValue);
                    }
                } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                    if (value instanceof Boolean) {
                        field.set(null, value);
                    } else if (value instanceof String) {
                        boolean convertedValue = Boolean.parseBoolean((String) value);
                        field.set(null, convertedValue);
                        dlog(TAG + " Converted value for key " + key + ": " + convertedValue);
                    }
                } else if (fieldType == String.class) {
                    field.set(null, String.valueOf(value));
                }
                field.setAccessible(false);
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            dlog(TAG + " Failed to set prop " + key);
        } catch (NumberFormatException e) {
            dlog(TAG + " Failed to parse value for field " + key);
        }
    }

    public static boolean shouldBypassTaskPermission(Context context) {
        // GMS doesn't have MANAGE_ACTIVITY_TASKS permission
        final int callingUid = Binder.getCallingUid();
        final int gmsUid;
        try {
            gmsUid = context.getPackageManager().getApplicationInfo(PACKAGE_GMS, 0).uid;
            dlog("shouldBypassTaskPermission: gmsUid:" + gmsUid + " callingUid:" + callingUid);
        } catch (Exception e) {
            Log.e(TAG, "shouldBypassTaskPermission: unable to get gms uid", e);
            return false;
        }
        return gmsUid == callingUid;
    }

    public static void dlog(String msg) {
        if (DEBUG) Log.d(TAG, msg);
    }
}
