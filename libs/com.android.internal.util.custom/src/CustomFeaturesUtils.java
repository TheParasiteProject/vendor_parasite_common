package com.android.internal.util.custom;

import android.app.ActivityThread;

import java.util.Arrays;

public class CustomFeaturesUtils {
    private static final int REPORT_FALSE = 0;
    private static final int REPORT_TRUE = 1;
    private static final int REPORT_SKIP = 255;

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

    private static final String[] featuresP22 = {
            "com.google.android.feature.PIXEL_2022_EXPERIENCE",
            "com.google.android.feature.PIXEL_2022_MIDYEAR_EXPERIENCE",
    };

    private static final String[] featuresP21 = {
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

    public int hasSystemFeatureCustom(String name) {
        String packageName = ActivityThread.currentPackageName();
        if (packageName != null &&
                packageName.equals("com.google.android.apps.photos")) {
            if (Arrays.asList(featuresPixel).contains(name)) return REPORT_FALSE;
            if (Arrays.asList(featuresP22).contains(name)) return REPORT_FALSE;
            if (Arrays.asList(featuresP21).contains(name)) return REPORT_FALSE;
            if (Arrays.asList(featuresNexus).contains(name)) return REPORT_TRUE;
        }
        if (Arrays.asList(featuresPixel).contains(name)) return REPORT_TRUE;
        return REPORT_SKIP;
    }
}
