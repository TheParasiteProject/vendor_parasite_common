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

import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager;
import android.app.ActivityTaskManager.RootTaskInfo;
import android.app.ActivityTaskManager;
import android.app.Application;
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
    private static final Boolean sDeviceIsPixel =
            SystemProperties.get("ro.product.manufacturer", "").toLowerCase().contains("google");
    private static final Boolean sForceSpoofGmsProcess =
            SystemProperties.getBoolean("persist.sys.pihooks.force.spoof.gms.process", false);
    private static final String sNetflixModel =
            SystemProperties.get("persist.sys.pihooks.netflix_model", "");

    private static final ComponentName GMS_ADD_ACCOUNT_ACTIVITY = ComponentName.unflattenFromString(
            "com.google.android.gms/.auth.uiflows.minutemaid.MinuteMaidActivity");

    private static volatile boolean sIsGmsUnstable;

    private static final Map<String, Object> propsToChangeGeneric;
    private static final Map<String, ArrayList<String>> propsToKeep;
    static {
        propsToKeep = new HashMap<>();
        propsToKeep.put(
                "com.google.android.settings.intelligence",
                new ArrayList<>(Collections.singletonList("FINGERPRINT")));
        propsToChangeGeneric = new HashMap<>();
        propsToChangeGeneric.put("TYPE", "user");
        propsToChangeGeneric.put("TAGS", "release-keys");
    }

    private static final Map<String, Object> propsToChangeDevice =
            createGoogleSpoofProps(
                Build.MODEL,
                Build.FINGERPRINT
            );

    private static final Map<String, Object> propsToChangePixelXL =
            createGoogleSpoofProps(
                "Pixel XL",
                "google/marlin/marlin:10/QP1A.191005.007.A3/5972272:user/release-keys"
            );

    private static final Map<String, Object> propsToChangePixel5a =
            createGoogleSpoofProps(
                "Pixel 5a",
                "google/barbet/barbet:14/AP2A.240805.005/12025142:user/release-keys"
            );

    private static final Map<String, Object> propsToChangePixelTablet =
            createGoogleSpoofProps(
                "Pixel Tablet",
                "google/tangorpro/tangorpro:14/AP2A.240805.005/12025142:user/release-keys"
            );

    private static final Map<String, Object> propsToChangeRecentPixel =
            createGoogleSpoofProps(
                "Pixel 9 Pro",
                "google/caiman/caiman:14/AD1A.240530.047.U1/12150698:user/release-keys"
            );

    private static final ArrayList<String> packagesToChangeRecentPixel = 
        new ArrayList<String> (
            Arrays.asList(
                "com.amazon.avod.thirdpartyclient",
                "com.android.chrome",
                "com.breel.wallpapers20",
                "com.disney.disneyplus",
                "com.google.android.apps.aiwallpapers",
                "com.google.android.apps.bard",
                "com.google.android.apps.customization.pixel",
                "com.google.android.apps.emojiwallpaper",
                "com.google.android.apps.nexuslauncher",
                "com.google.android.apps.photos",
                "com.google.android.apps.privacy.wildlife",
                "com.google.android.apps.subscriptions.red",
                "com.google.android.apps.wallpaper",
                "com.google.android.apps.wallpaper.pixel",
                "com.google.android.apps.weather",
                "com.google.android.gms",
                "com.google.android.googlequicksearchbox",
                "com.google.android.wallpaper.effects",
                "com.google.pixel.livewallpaper",
                "com.microsoft.android.smsorganizer",
                "com.netflix.mediaclient",
                "com.nhs.online.nhsonline",
                "com.nothing.smartcenter",
                "in.startv.hotstar",
                "jp.id_credit_sp2.android"
        ));

    private static final ArrayList<String> gmsProcessToChangePixel5a = 
        new ArrayList<String> (
            Arrays.asList(
                "com.google.android.gms.gapps",
                "com.google.android.gms.gservice",
                "com.google.android.gms.learning",
                "com.google.android.gms.persistent"
        ));

    private static final ArrayList<String> processToKeep = 
        new ArrayList<String> (
            Arrays.asList(
                "com.google.android.apps.cameralite",
                "com.google.android.apps.dreamliner",
                "com.google.android.apps.dreamlinerupdater",
                "com.google.android.apps.miphone.aiai.AiaiApplication",
                "com.google.android.apps.motionsense.bridge",
                "com.google.android.apps.pixelmigrate",
                "com.google.android.apps.restore",
                "com.google.android.apps.subscriptions.red",
                "com.google.android.apps.tachyon",
                "com.google.android.apps.tips",
                "com.google.android.apps.tycho",
                "com.google.android.apps.wearables.maestro.companion",
                "com.google.android.apps.youtube.kids",
                "com.google.android.apps.youtube.music",
                "com.google.android.as",
                "com.google.android.backuptransport",
                "com.google.android.backupuses",
                "com.google.android.euicc",
                "com.google.android.GoogleCamera",
                "com.google.android.inputmethod.latin",
                "com.google.android.MTCL83",
                "com.google.android.UltraCVM",
                "com.google.android.youtube",
                "com.google.ar.core",
                "com.google.intelligence.sense",
                "com.google.oslo",
                "it.ingdirect.app"
        ));

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

    private static Map<String, Object> createGoogleSpoofProps(String model, String fingerprint) {
        Map<String, Object> props = new HashMap<>();
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
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
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
            return focusedTask != null &&
                    GMS_ADD_ACCOUNT_ACTIVITY.equals(focusedTask.topActivity);
        } catch (Exception e) {
            Log.e(TAG, "Unable to get top activity!", e);
        }
        return false;
    }

    private static boolean shouldTryToCertifyDevice() {
        if (!sIsGmsUnstable) return false;

        if (Android.isCertifiedPropsEmpty()) return false;

        final boolean was = isGmsAddAccountActivityOnTop();
        final TaskStackListener taskStackListener = new TaskStackListener() {
            @Override
            public void onTaskStackChanged() {
                final boolean is = isGmsAddAccountActivityOnTop();
                if (is ^ was) {
                    dlog("GmsAddAccountActivityOnTop is:" + is + " was:" + was +
                            ", killing myself!"); // process will restart automatically later
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

        if ("com.google.android.apps.photos".equals(packageName)) {
            if (SystemProperties.getBoolean("persist.sys.pixelprops.gphotos", false)) {
                propsToChangePixelXL.forEach((k, v) -> setPropValue(k, v));
                return true;
            }
        }
        return false;
    }

    public static void setProps(Context context) {
        if (!sEnablePixelProps) {
            dlog("Pixel props is disabled by config");
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
        sIsGmsUnstable = "com.google.android.gms.unstable".equals(processName);

        if ("com.google.android.gms".equals(packageName)) {
            setPropValue("TIME", System.currentTimeMillis());
        }

        if (shouldTryToCertifyDevice()) {
            return;
        }

        if (processToKeep.contains(processName)) {
            return;
        }

        if (setPropsForGphotos(context)) {
            return;
        }

        Map<String, Object> propsToChange = new HashMap<>();
        if (gmsProcessToChangePixel5a.contains(processName)) {
            if (!sDeviceIsPixel) {
                propsToChange = propsToChangePixel5a;
            } else if (sForceSpoofGmsProcess) {
                propsToChange = propsToChangeDevice;
            }
        } else if (packagesToChangeRecentPixel.contains(processName)) {
            propsToChange = propsToChangeRecentPixel;
        } else if (packagesToChangeRecentPixel.contains(packageName)) {
            propsToChange = propsToChangeRecentPixel;
        } else if (sIsTablet) {
            propsToChange = propsToChangePixelTablet;
        }

        if (propsToChange == null || propsToChange.isEmpty()) return;

        dlog("Defining props for: " + packageName);
        for (Map.Entry<String, Object> prop : propsToChange.entrySet()) {
            String key = prop.getKey();
            Object value = prop.getValue();
            if (propsToKeep.containsKey(packageName) && propsToKeep.get(packageName).contains(key)) {
                dlog("Not defining " + key + " prop for: " + packageName);
                continue;
            }
            dlog("Defining " + key + " prop for: " + packageName);
            setPropValue(key, value);
        }
        // Show correct model name on gms services
        if ("com.google.android.gms.ui".equals(processName)) {
            setPropValue("MODEL", sDeviceModel);
            return;
        }
        // Set proper indexing fingerprint
        if ("com.google.android.settings.intelligence".equals(packageName)) {
            setPropValue("FINGERPRINT", Build.VERSION.INCREMENTAL);
            return;
        }
        if (!TextUtils.isEmpty(sNetflixModel) && "com.netflix.mediaclient".equals(packageName)) {
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
            gmsUid = context.getPackageManager().getApplicationInfo("com.google.android.gms", 0).uid;
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
