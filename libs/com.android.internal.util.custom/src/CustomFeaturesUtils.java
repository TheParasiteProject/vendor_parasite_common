package com.android.internal.util.custom;

import android.app.ActivityThread;

import java.util.Arrays;

public class CustomFeaturesUtils {
    private static final int REPORT_FALSE = 0;
    private static final int REPORT_TRUE = 1;
    private static final int REPORT_SKIP = 255;

    private static final String[] pTensorCodenames = {
            "lynx",
            "cheetah",
            "panther",
            "bluejay",
            "oriole",
            "raven"
    };

    private static final String[] featuresPixel = {
            "com.google.android.apps.photos.PIXEL_2019_PRELOAD",
            "com.google.android.apps.photos.PIXEL_2019_MIDYEAR_PRELOAD",
            "com.google.android.apps.photos.PIXEL_2018_PRELOAD",
            "com.google.android.apps.photos.PIXEL_2017_PRELOAD",
            "com.google.android.feature.PIXEL_2020_EXPERIENCE",
            "com.google.android.feature.PIXEL_2020_MIDYEAR_EXPERIENCE",
            "com.google.android.feature.PIXEL_2019_EXPERIENCE",
            "com.google.android.feature.PIXEL_2019_MIDYEAR_EXPERIENCE",
            "com.google.android.feature.PIXEL_2018_EXPERIENCE",
            "com.google.android.feature.PIXEL_2017_EXPERIENCE",
            "com.google.android.feature.PIXEL_EXPERIENCE",
            "com.google.android.feature.GOOGLE_BUILD",
            "com.google.android.feature.GOOGLE_EXPERIENCE"
    };

    private static final String[] featuresPixelOthers = {
            "com.google.android.feature.ASI",
            "com.google.android.feature.ANDROID_ONE_EXPERIENCE",
            "com.google.android.feature.GOOGLE_FI_BUNDLED",
            "com.google.android.feature.LILY_EXPERIENCE",
            "com.google.android.feature.TURBO_PRELOAD",
            "com.google.android.feature.WELLBEING",
            "com.google.lens.feature.IMAGE_INTEGRATION",    
            "com.google.lens.feature.CAMERA_INTEGRATION",
            "com.google.photos.trust_debug_certs",
            "com.google.android.feature.AER_OPTIMIZED",
            "com.google.android.feature.NEXT_GENERATION_ASSISTANT",
            "android.software.game_service",
            "com.google.android.feature.EXCHANGE_6_2",
            "com.google.android.apps.dialer.call_recording_audio",
            "com.google.android.apps.dialer.SUPPORTED"
    };

    private static final String[] featuresP23 = {
            "com.google.android.feature.PIXEL_2024_EXPERIENCE",
            "com.google.android.feature.PIXEL_2024_MIDYEAR_EXPERIENCE",
            "com.google.android.feature.PIXEL_2023_EXPERIENCE",
            "com.google.android.feature.PIXEL_2023_MIDYEAR_EXPERIENCE",
    };

    private static final String[] featuresTensor = {
            "com.google.android.feature.PIXEL_2022_EXPERIENCE",
            "com.google.android.feature.PIXEL_2022_MIDYEAR_EXPERIENCE",
            "com.google.android.feature.PIXEL_2021_EXPERIENCE",
            "com.google.android.feature.PIXEL_2021_MIDYEAR_EXPERIENCE"
    };

    private static final String[] featuresNexus = {
            "com.google.android.apps.photos.NEXUS_PRELOAD",
            "com.google.android.apps.photos.nexus_preload",
            "com.google.android.feature.PIXEL_EXPERIENCE",
            "com.google.android.feature.GOOGLE_BUILD",
            "com.google.android.feature.GOOGLE_EXPERIENCE"
    };

    private static final String[] featuresAndroid = {
            "android.software.freeform_window_management"
    };

    public int hasSystemFeatureCustom(String name) {
        String packageName = ActivityThread.currentPackageName();
        if (packageName != null &&
                packageName.equals("com.google.android.apps.photos")) {
            if (Arrays.asList(featuresPixel).contains(name)) return REPORT_FALSE;
            if (Arrays.asList(featuresPixelOthers).contains(name)) return REPORT_TRUE;
            if (Arrays.asList(featuresP23).contains(name)) return REPORT_FALSE;
            if (Arrays.asList(featuresTensor).contains(name)) return REPORT_FALSE;
            if (Arrays.asList(featuresNexus).contains(name)) return REPORT_TRUE;
        }
        if (Arrays.asList(featuresTensor).contains(name) &&
                !Arrays.asList(pTensorCodenames).contains(SystemProperties.get("ro.product.device"))) {
            return false;
        } else if (packageName != null && Arrays.asList(featuresTensor).contains(name)) {
            if (packageName.contains("com.google.android.as")
                || packageName.contains("com.google.android.apps.nexuslauncher")) {
                return false;
            }
        }
        if (Arrays.asList(featuresAndroid).contains(name)) return REPORT_TRUE;
        if (Arrays.asList(featuresPixel).contains(name)) return REPORT_TRUE;
        if (Arrays.asList(featuresPixelOthers).contains(name)) return REPORT_TRUE;
        return REPORT_SKIP;
    }
}
