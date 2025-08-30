package com.android.internal.util.custom;

import android.content.ContentResolver;
import android.os.SystemProperties;
import android.provider.Settings;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @hide
 */
public class HideDeveloperStatusUtils {
    private static final Set<String> SETTINGS_TO_HIDE =
            Collections.unmodifiableSet(
                    new HashSet<>(
                            Arrays.asList(
                                    Settings.Global.ADB_ENABLED,
                                    Settings.Global.ADB_WIFI_ENABLED,
                                    Settings.Global.DEVELOPMENT_SETTINGS_ENABLED)));

    private static boolean isBootCompleted() {
        return SystemProperties.getBoolean("sys.boot_completed", false);
    }

    public static boolean shouldHideDevStatus(ContentResolver cr, String packageName, String name) {
        if (!isBootCompleted()
                || cr == null
                || packageName == null
                || name == null
                || !SETTINGS_TO_HIDE.contains(name)) {
            return false;
        }
        Set<String> apps = getApps(cr);
        return !apps.isEmpty() && apps.contains(packageName);
    }

    private static Set<String> getApps(ContentResolver cr) {
        if (cr == null) {
            return Collections.emptySet();
        }
        String apps = Settings.Secure.getString(cr, Settings.Secure.HIDE_DEVELOPER_STATUS);
        if (apps == null || apps.isEmpty() || apps.equals(",")) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(apps.split(","))));
    }
}
