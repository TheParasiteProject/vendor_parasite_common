package com.android.internal.util.custom;

import android.app.ActivityThread;
import android.content.res.Resources;
import android.os.SystemProperties;

import org.lineageos.platform.internal.R;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @hide
 */
public final class CustomFeaturesUtils {
    private static final int REPORT_FALSE = 0;
    private static final int REPORT_TRUE = 1;
    private static final int REPORT_SKIP = 255;

    private static Boolean sEnableCustomFeaturesUtils =
            SystemProperties.getBoolean("persist.sys.cfhooks.enable", true);

    private static final Boolean sHasTensorSoC =
            SystemProperties.get("ro.soc.manufacturer", "").toLowerCase().contains("google");

    private static final String PACKAGE_PHOTOS = "com.google.android.apps.photos";

    private static String[] getStringArrayResSafely(int resId) {
        String[] strArr = Resources.getSystem().getStringArray(resId);
        if (strArr == null) strArr = new String[0];
        return strArr;
    }

    private static ArrayList<String> getFeaturesPixel() {
        return new ArrayList<String>(
                Arrays.asList(getStringArrayResSafely(R.array.config_cfHookFeaturesPixel)));
    }

    private static ArrayList<String> getFeaturesPixelOthers() {
        return new ArrayList<String>(
                Arrays.asList(getStringArrayResSafely(R.array.config_cfHookFeaturesPixelOthers)));
    }

    private static ArrayList<String> getFeaturesPixelTensor() {
        return new ArrayList<String>(
                Arrays.asList(getStringArrayResSafely(R.array.config_cfHookFeaturesPixelTensor)));
    }

    private static ArrayList<String> getPackagesToExposeFeaturesPixelTensor() {
        return new ArrayList<String>(
                Arrays.asList(
                        getStringArrayResSafely(R.array.config_cfHookPackagesFeaturesPixelTensor)));
    }

    private static ArrayList<String> getFeaturesNexus() {
        return new ArrayList<String>(
                Arrays.asList(getStringArrayResSafely(R.array.config_cfHookFeaturesNexus)));
    }

    private static ArrayList<String> getFeaturesAndroid() {
        return new ArrayList<String>(
                Arrays.asList(getStringArrayResSafely(R.array.config_cfHookFeaturesAndroid)));
    }

    public static int hasSystemFeatureCustom(String name) {
        if (!sEnableCustomFeaturesUtils) return REPORT_SKIP;

        if (name == null) {
            return REPORT_SKIP;
        }

        String packageName = ActivityThread.currentPackageName();

        if (packageName != null
                && PACKAGE_PHOTOS.equals(packageName)
                && SystemProperties.getBoolean("persist.sys.pixelprops.gphotos", false)) {
            if (getFeaturesPixel().contains(name)) return REPORT_FALSE;
            if (getFeaturesPixelOthers().contains(name)) return REPORT_TRUE;
            if (getFeaturesPixelTensor().contains(name)) return REPORT_FALSE;
            if (getFeaturesNexus().contains(name)) return REPORT_TRUE;
        }
        if (packageName != null && getPackagesToExposeFeaturesPixelTensor().contains(packageName)) {
            if (getFeaturesPixel().contains(name)) return REPORT_TRUE;
            if (getFeaturesPixelOthers().contains(name)) return REPORT_TRUE;
            if (getFeaturesPixelTensor().contains(name)) return REPORT_TRUE;
            if (getFeaturesNexus().contains(name)) return REPORT_TRUE;
        }
        if (getFeaturesPixelTensor().contains(name) && !sHasTensorSoC) {
            return REPORT_FALSE;
        }
        if (getFeaturesAndroid().contains(name)) return REPORT_TRUE;
        if (getFeaturesPixel().contains(name)) return REPORT_TRUE;
        if (getFeaturesPixelOthers().contains(name)) return REPORT_TRUE;
        return REPORT_SKIP;
    }
}
