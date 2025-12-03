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

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.text.TextUtils;

import org.lineageos.platform.internal.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @hide
 */
public final class PixelPropsUtils extends CommonPropsUtils {

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

    private static final Map<String, String> propsToChangeGeneric;
    private static final Map<String, String> propsToChangeDevice;
    private static final Map<String, ArrayList<String>> propsToKeep;

    static {
        propsToKeep = new HashMap<>();
        propsToChangeGeneric = new HashMap<>();
        propsToChangeGeneric.put("TYPE", "user");
        propsToChangeGeneric.put("TAGS", "release-keys");
        propsToChangeDevice = new HashMap<>();
        propsToChangeDevice.put("BRAND", Build.BRAND);
        propsToChangeDevice.put("BOARD", Build.BOARD);
        propsToChangeDevice.put("MANUFACTURER", Build.MANUFACTURER);
        propsToChangeDevice.put("ID", Build.ID);
        propsToChangeDevice.put("DEVICE", Build.DEVICE);
        propsToChangeDevice.put("PRODUCT", Build.PRODUCT);
        propsToChangeDevice.put("HARDWARE", Build.HARDWARE);
        propsToChangeDevice.put("MODEL", Build.MODEL);
        propsToChangeDevice.put("FINGERPRINT", Build.FINGERPRINT);
    }

    private static Map<String, String> getPropsToChangePixelXL() {
        return createGoogleSpoofProps(getStringArrayResSafely(R.array.config_piHookPropsPixelXL));
    }

    private static Map<String, String> getPropsToChangePixelLegacy() {
        return createGoogleSpoofProps(
                getStringArrayResSafely(R.array.config_piHookPropsPixelLegacy));
    }

    private static Map<String, String> getPropsToChangePixelTablet() {
        return createGoogleSpoofProps(
                getStringArrayResSafely(R.array.config_piHookPropsPixelTablet));
    }

    private static Map<String, String> getPropsToChangePixelExtra() {
        return createGoogleSpoofProps(
                getStringArrayResSafely(R.array.config_piHookPropsPixelExtra));
    }

    private static ArrayList<String> getPackagesToChangePixelExtra() {
        return new ArrayList<String>(
                Arrays.asList(getStringArrayResSafely(R.array.config_piHookProcessPixelExtra)));
    }

    private static Map<String, String> getPropsToChangePixelRecent() {
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

    private static Map<String, String> createSpoofProps(String[] config) {
        Map<String, String> props = new HashMap<>();

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
        props.put("BOARD", getDeviceName(fingerprint));
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

    private static Map<String, String> createGoogleSpoofProps(String[] config) {
        Map<String, String> props = new HashMap<>();

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
        props.put("BOARD", getDeviceName(fingerprint));
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

    public static boolean setPropsForGphotos(Context context) {
        if (context == null) return false;

        final String packageName = context.getPackageName();
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }

        if (PACKAGE_PHOTOS.equals(packageName)) {
            if (SystemProperties.getBoolean("persist.sys.pihooks.gphotos", false)) {
                getPropsToChangePixelXL().forEach((k, v) -> setPropValue(k, v));
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

        if (PACKAGE_GMS.equals(packageName)) {
            setPropValue("TIME", "" + System.currentTimeMillis());
        }

        if (getProcessToKeep().contains(processName)) {
            return;
        }

        if (setPropsForGphotos(context)) {
            return;
        }

        Map<String, String> propsToChange = new HashMap<>();
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
        for (Map.Entry<String, String> prop : propsToChange.entrySet()) {
            String key = prop.getKey();
            String value = prop.getValue();
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
}
