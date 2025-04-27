/*
 * Copyright (C) 2017-2022 crDroid Android Project
 * Copyright (C) 2023 PixelBlaster-OS
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

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.os.RemoteException;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;

import java.util.ArrayList;
import java.util.List;

/**
 * @hide
 */
public class CustomUtils {

    public static boolean isAvailableApp(String packageName, Context context) {
        Context mContext = context;
        final PackageManager pm = mContext.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            int enabled = pm.getApplicationEnabledSetting(packageName);
            return enabled != PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    && enabled != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isPackageInstalled(
            Context context, String packageName, boolean ignoreState) {
        if (packageName != null) {
            try {
                PackageInfo pi = context.getPackageManager().getPackageInfo(packageName, 0);
                if ((!pi.applicationInfo.enabled || !pi.applicationInfo.isProduct())
                        && !ignoreState) {
                    return false;
                }
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }
        return true;
    }

    public static boolean isPackageInstalled(Context context, String packageName) {
        return isPackageInstalled(context, packageName, true);
    }

    public static boolean isPackageEnabled(Context context, String packageName) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(packageName, 0);
            return pi.applicationInfo.enabled;
        } catch (PackageManager.NameNotFoundException notFound) {
            return false;
        }
    }

    public static void performKeyActionFromIntSafe(Context context, int actionCode) {
        switch (actionCode) {
            case 4: // VOICE_SEARCH
                launchVoiceAssist(context);
                return;
            case 8: // LAST_APP
                switchToLastApp(context);
                return;
            case 13: // VOLUME_PANEL
                toggleVolumePanel(context);
                return;
        }

        IWindowManager wm = WindowManagerGlobal.getWindowManagerService();
        try {
            wm.performKeyActionFromIntSafe(actionCode);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    // Launch voice search
    public static void launchVoiceAssist(Context context) {
        Intent intent = new Intent(Intent.ACTION_SEARCH_LONG_PRESS);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // Volume panel
    public static void toggleVolumePanel(Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        am.adjustVolume(AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
    }

    // Switch to last app
    public static void switchToLastApp(Context context) {
        final ActivityManager am =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.RunningTaskInfo lastTask = getLastTask(context, am);

        if (lastTask != null) {
            am.moveTaskToFront(
                    lastTask.id,
                    ActivityManager.MOVE_TASK_NO_USER_ACTION,
                    getAnimation(context).toBundle());
        }
    }

    private static ActivityOptions getAnimation(Context context) {
        return ActivityOptions.makeCustomAnimation(
                context,
                com.android.internal.R.anim.task_open_enter,
                com.android.internal.R.anim.task_open_exit);
    }

    private static ActivityManager.RunningTaskInfo getLastTask(
            Context context, final ActivityManager am) {
        final List<String> packageNames = getCurrentLauncherPackages(context);
        final List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(5);
        for (int i = 1; i < tasks.size(); i++) {
            String packageName = tasks.get(i).topActivity.getPackageName();
            if (!packageName.equals(context.getPackageName())
                    && !packageName.equals("com.android.systemui")
                    && !packageNames.contains(packageName)) {
                return tasks.get(i);
            }
        }
        return null;
    }

    private static List<String> getCurrentLauncherPackages(Context context) {
        final PackageManager pm = context.getPackageManager();
        final List<ResolveInfo> homeActivities = new ArrayList<>();
        pm.getHomeActivities(homeActivities);
        final List<String> packageNames = new ArrayList<>();
        for (ResolveInfo info : homeActivities) {
            final String name = info.activityInfo.packageName;
            if (!name.equals("com.android.settings")) {
                packageNames.add(name);
            }
        }
        return packageNames;
    }
}
