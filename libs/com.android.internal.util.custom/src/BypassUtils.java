/*
 * Copyright (C) 2023-2024 the risingOS Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.internal.util.custom;

import android.app.ActivityThread;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.os.Binder;
import android.os.UserHandle;
import android.util.Log;

import org.lineageos.platform.internal.R;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @hide
 */
public class BypassUtils {

    private static final String TAG = BypassUtils.class.getSimpleName();
    private static final boolean DEBUG = false;

    private static final String PACKAGE_GMS = "com.google.android.gms";
    private static final String PACKAGE_NEXUS_LAUNCHER = "com.google.android.apps.nexuslauncher";

    private static Set<String> mLauncherPkgs;
    private static Set<String> mExemptedUidPkgs;

    private static String[] getStringArrayResSafely(int resId) {
        try {
            return Resources.getSystem().getStringArray(resId);
        } catch (Resources.NotFoundException e) {
            return new String[0];
        }
    }

    public static boolean isPackageGoogle(String pkg) {
        return pkg != null && pkg.toLowerCase().contains("google");
    }

    private static Set<String> getLauncherPkgs() {
        if (mLauncherPkgs == null || mLauncherPkgs.isEmpty()) {
            mLauncherPkgs =
                    new HashSet<>(
                            Arrays.asList(
                                    getStringArrayResSafely(R.array.config_launcherPackages)));
        }
        return mLauncherPkgs;
    }

    private static Set<String> getExemptedUidPkgs() {
        if (mExemptedUidPkgs == null || mExemptedUidPkgs.isEmpty()) {
            mExemptedUidPkgs = new HashSet<>();
            mExemptedUidPkgs.add(PACKAGE_GMS);
            mExemptedUidPkgs.addAll(getLauncherPkgs());
        }
        return mExemptedUidPkgs;
    }

    public static boolean isSystemLauncher(Context context) {
        try {
            return isSystemLauncherInternal(
                    context.getPackageManager().getNameForUid(android.os.Binder.getCallingUid()));
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isSystemLauncher(int callingUid) {
        try {
            return isSystemLauncherInternal(
                    ActivityThread.getPackageManager().getNameForUid(callingUid));
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isSystemLauncherInternal(String callerPackage) {
        return getLauncherPkgs().contains(callerPackage);
    }

    public static boolean isNexusLauncher(Context context) {
        try {
            return PACKAGE_NEXUS_LAUNCHER.equals(
                    context.getPackageManager().getNameForUid(android.os.Binder.getCallingUid()));
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean shouldBypassTaskPermission(int callingUid) {
        for (String pkg : getExemptedUidPkgs()) {
            try {
                ApplicationInfo appInfo =
                        ActivityThread.getPackageManager()
                                .getApplicationInfo(pkg, 0, UserHandle.getUserId(callingUid));
                if (appInfo.uid == callingUid) {
                    return true;
                }
            } catch (Exception e) {
            }
        }
        return false;
    }

    public static boolean shouldBypassManageActivityTaskPermission(Context context) {
        final int callingUid = Binder.getCallingUid();
        return isSystemLauncher(callingUid)
                || isPackageGoogle(context.getPackageManager().getNameForUid(callingUid));
    }

    public static boolean shouldBypassMonitorInputPermission(Context context) {
        final int callingUid = Binder.getCallingUid();
        return shouldBypassTaskPermission(callingUid)
                || isPackageGoogle(context.getPackageManager().getNameForUid(callingUid));
    }

    // Whitelist of package names to bypass FGS type validation
    public static boolean shouldBypassFGSValidation(String packageName) {
        // Check if the app is whitelisted
        if (Arrays.asList(getStringArrayResSafely(R.array.config_fgsTypeValidationBypassPackages))
                .contains(packageName)) {
            dlog(
                    "shouldBypassFGSValidation: "
                            + "Bypassing FGS type validation for whitelisted app: "
                            + packageName);
            return true;
        }
        return false;
    }

    // Whitelist of package names to bypass alarm manager validation
    public static boolean shouldBypassAlarmManagerValidation(String packageName) {
        // Check if the app is whitelisted
        if (Arrays.asList(
                        getStringArrayResSafely(
                                R.array.config_alarmManagerValidationBypassPackages))
                .contains(packageName)) {
            dlog(
                    "shouldBypassAlarmManagerValidation: "
                            + "Bypassing alarm manager validation for whitelisted app: "
                            + packageName);
            return true;
        }
        return false;
    }

    // Whitelist of package names to bypass broadcast reciever validation
    public static boolean shouldBypassBroadcastReceiverValidation(String packageName) {
        // Check if the app is whitelisted
        if (Arrays.asList(
                        getStringArrayResSafely(
                                R.array.config_broadcastReceiverValidationBypassPackages))
                .contains(packageName)) {
            dlog(
                    "shouldBypassBroadcastReceiverValidation: "
                            + "Bypassing broadcast receiver validation for whitelisted app: "
                            + packageName);
            return true;
        }
        return false;
    }

    // Whitelist of package names to bypass idle whitelist validation
    public static boolean shouldBypasIdleWhitelistValidation(String packageName) {
        // Check if the app is whitelisted
        if (Arrays.asList(
                        getStringArrayResSafely(
                                R.array.config_idleWhitelistValidationBypassPackages))
                .contains(packageName)) {
            dlog(
                    "shouldBypasIdleWhitelistValidation: Bypassing idle whitelist"
                            + " validation for whitelisted app: "
                            + packageName);
            return true;
        }
        return false;
    }

    public static void dlog(String msg) {
        if (DEBUG) Log.d(TAG, msg);
    }
}
