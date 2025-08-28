/*
 * Copyright (C) 2021 The ProtonAOSP Project
 * Copyright (C) 2022-2024 GrapheneOS
 * Copyright (C) 2024-2025 TheParasiteProject
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

import android.annotation.Nullable;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Base64;
import android.util.Log;

import org.lineageos.platform.internal.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @hide
 */
public class PhenotypeFlagsUtils extends CommonPropsUtils {

    private static final String TAG = PhenotypeFlagsUtils.class.getSimpleName();
    private static final boolean DEBUG = true;

    private static Boolean sEnablePhenotypeFlagsUtils =
            SystemProperties.getBoolean("persist.sys.pfhooks.enable", true);

    public static final String PACKAGE_GMS = "com.google.android.gms";
    public static final String PACKAGE_GSF = "com.google.android.gsf";
    public static final String PROCESS_GMS_PERSISTENT = PACKAGE_GMS + ".persistent";

    public static final String PHENOTYPE_URI_PREFIX = "content://" + PACKAGE_GMS + ".phenotype/";
    public static final String PHENOTYPE_ACTION_COMMITTED =
            "com.google.android.gms.phenotype.COMMITTED";
    public static final String PHENOTYPE_ACTION_OVERRIDE =
            "com.google.android.gms.phenotype.FLAG_OVERRIDE";

    public static final String NAMESPACE_GSERVICES = "gservices";
    public static final String GSERVICES_URI =
            "content://" + PACKAGE_GSF + '.' + NAMESPACE_GSERVICES + "/prefix";
    public static final String GSERVICES_ACTION_OVERRIDE =
            "com.google.gservices.intent.action.GSERVICES_OVERRIDE";

    private static HashSet<String> getNamespacesSet(String namespaceArg, boolean isSharedPref) {
        if (namespaceArg == null) return new HashSet<String>();

        String[] global =
                Resources.getSystem().getStringArray(R.array.global_phenotype_package_namespaces);
        String[] device =
                Resources.getSystem().getStringArray(R.array.device_phenotype_package_namespaces);
        String[] all = Arrays.copyOf(global, global.length + device.length);
        System.arraycopy(device, 0, all, global.length, device.length);

        final ArrayMap<String, HashSet<String>> nsMap = new ArrayMap();
        for (String p : all) {
            String[] pn = p.split("=");
            String pkg = pn[0];
            String[] ns = pn[1].split(",");
            for (String n : ns) {
                if (n.startsWith(".")) {
                    nsMap.computeIfAbsent(pkg, k -> new HashSet<>()).add(pkg + n);
                } else {
                    nsMap.computeIfAbsent(pkg, k -> new HashSet<>()).add(n);
                }
            }
            nsMap.computeIfAbsent(pkg, k -> new HashSet<>()).add(pkg);
            nsMap.computeIfAbsent(pkg, k -> new HashSet<>()).add(pkg + "#" + pkg);
        }

        String pkg = namespaceArg;
        if (isSharedPref) {
            pkg = namespaceArg.split("/")[0]; // package name
        } else if (namespaceArg.contains("#")) {
            pkg = namespaceArg.split("#")[1];
        }

        HashSet<String> ret = nsMap.get(pkg);
        if (ret == null) {
            ret = new HashSet<>();
        }
        ret.add(namespaceArg);
        return ret;
    }

    private static String[] getFlagsOverride() {
        String[] globalFlags =
                Resources.getSystem().getStringArray(R.array.global_phenotype_flags_override);
        String[] deviceFlags =
                Resources.getSystem().getStringArray(R.array.device_phenotype_flags_override);
        String[] allFlags = Arrays.copyOf(globalFlags, globalFlags.length + deviceFlags.length);
        System.arraycopy(deviceFlags, 0, allFlags, globalFlags.length, deviceFlags.length);
        return allFlags;
    }

    private static final class FlagsList {
        public static final ArrayList<String> flags = new ArrayList();
        public static final ArrayList<String> values = new ArrayList();
        public static final ArrayList<String> types = new ArrayList();

        public static void add(String flag, String value, String type) {
            flags.add(flag);
            values.add(value);
            types.add(type);
        }

        public static void remove(int index) {
            flags.remove(index);
            values.remove(index);
            types.remove(index);
        }
    }

    private static final int PHENOTYPE_BASE64_FLAGS = Base64.NO_PADDING | Base64.NO_WRAP;

    private static ArrayMap<String, ArrayMap<String, Object>> getFlagsOverrideMap(
            boolean isSharedPref) {
        final ArrayMap<String, ArrayMap<String, Object>> flagMap = new ArrayMap();
        for (String p : getFlagsOverride()) {
            String[] kv = p.split("=");
            String fullKey = kv[0];
            String[] nsKey = fullKey.split("/");

            if (nsKey.length != 3) {
                logd("Invalid config: " + p);
                continue;
            }

            String namespace = nsKey[0];
            String key = nsKey[1];
            String type = nsKey[2];

            Object value = "";
            if (kv.length < 1) {
                flagMap.computeIfAbsent(namespace, k -> new ArrayMap<>()).put(key, value);
                continue;
            }

            if (isSharedPref) {
                if (type.equals("boolean")) {
                    value = kv[1].equals("true") ? "1" : "0";
                }
                flagMap.computeIfAbsent(namespace, k -> new ArrayMap<>()).put(key, value);
                continue;
            }

            switch (type) {
                case "long":
                    value = Long.parseLong(kv[1]);
                    break;
                case "boolean":
                    value = Boolean.parseBoolean(kv[1]);
                    break;
                case "double":
                    value = Double.parseDouble(kv[1]);
                    break;
                case "string":
                    value = kv[1];
                    break;
                case "bytes":
                    value = Base64.decode(kv[1], PHENOTYPE_BASE64_FLAGS);
                    break;
                default:
                    logd("Unsupported type specifier: " + type + " for config: " + p);
                    continue;
            }

            flagMap.computeIfAbsent(namespace, k -> new ArrayMap<>()).put(key, value);
        }

        return flagMap;
    }

    private static ArrayMap<String, String> getGserviceFlagsOverride() {
        final ArrayMap<String, String> flagMap = new ArrayMap();
        for (String p : getFlagsOverride()) {
            String[] kv = p.split("=");
            String fullKey = kv[0];
            String[] nsKey = fullKey.split("/");

            if (nsKey.length != 3) {
                logd("Invalid config: " + p);
                continue;
            }

            String namespace = nsKey[0];
            if (!NAMESPACE_GSERVICES.equals(namespace)) {
                continue;
            }
            String key = nsKey[1];
            String type = nsKey[2];

            String value = "";
            if (kv.length < 1) {
                flagMap.put(key, value);
                continue;
            }

            switch (type) {
                case "string":
                    value = kv[1];
                    break;
                default:
                    logd("Unsupported type specifier: " + type + " for config: " + p);
                    continue;
            }

            flagMap.put(key, value);
        }

        return flagMap;
    }

    private static ArrayMap<String, FlagsList> getPhenotypeFlagsOverride() {
        final ArrayMap<String, FlagsList> flagMap = new ArrayMap();

        for (String p : getFlagsOverride()) {
            String[] kv = p.split("=");
            String fullKey = kv[0];
            String[] nsKey = fullKey.split("/");

            if (nsKey.length != 3) {
                logd("Invalid config: " + p);
                continue;
            }

            String namespace = nsKey[0];
            if (NAMESPACE_GSERVICES.equals(namespace)) {
                continue;
            }

            flagMap.computeIfAbsent(namespace, k -> new FlagsList())
                    .add(nsKey[1], kv.length < 1 ? "" : kv[1], nsKey[2]);
        }

        return flagMap;
    }

    private static boolean maybeUpdateMap(
            String namespaceArg, @Nullable String[] selectionArgs, Map map, boolean isSharedPref) {
        if (namespaceArg == null || map == null) {
            return false;
        }

        final ArrayMap<String, ArrayMap<String, Object>> flagMap = getFlagsOverrideMap(false);

        // Add extra check for gservices flag
        if (selectionArgs != null && namespaceArg.equals(NAMESPACE_GSERVICES)) {
            final ArrayMap<String, Object> gflags = flagMap.get(NAMESPACE_GSERVICES);
            if (gflags == null) return false;

            boolean isMapModified = false;
            for (String sel : selectionArgs) {
                for (String key : gflags.keySet()) {
                    if (key.startsWith(sel)) {
                        logd("maybeUpdateMap: " + namespaceArg + "/" + sel);
                        map.put(key, gflags.get(key));
                        isMapModified = true;
                    }
                }
            }

            return isMapModified;
        } else {
            HashSet<String> namespaces = getNamespacesSet(namespaceArg, isSharedPref);
            if (isSharedPref) {
                String fileName = namespaceArg.split("/")[1]; // file name
                if (!namespaces.contains(fileName)) {
                    return false;
                }
            }

            final ArrayMap<String, Object> pflags = new ArrayMap<>();
            for (String ns : namespaces) {
                if (!flagMap.keySet().contains(ns)) {
                    continue;
                }
                pflags.putAll(flagMap.get(ns));
            }
            if (flagMap.keySet().contains(namespaceArg)) {
                pflags.putAll(flagMap.get(namespaceArg));
            }

            if (!pflags.isEmpty()) {
                map.putAll(pflags);
                return true;
            }
        }

        return false;
    }

    // ContentResolver#query(Uri, String[], Bundle, CancellationSignal)
    public static Cursor maybeModifyQueryResult(
            Uri uri,
            @Nullable String[] projection,
            @Nullable Bundle queryArgs,
            @Nullable Cursor origCursor) {
        if (!sEnablePhenotypeFlagsUtils) return null;

        String uriString = uri.toString();

        Consumer<ArrayMap<String, Object>> mutator = null;

        if (GSERVICES_URI.equals(uriString)) {
            if (queryArgs == null) {
                return null;
            }
            String[] selectionArgs =
                    queryArgs.getStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS);
            if (selectionArgs == null) {
                return null;
            }

            mutator = map -> maybeUpdateMap(NAMESPACE_GSERVICES, selectionArgs, map, false);
        } else if (uriString.startsWith(PHENOTYPE_URI_PREFIX)) {
            List<String> path = uri.getPathSegments();
            if (path.size() != 1) {
                Log.e(TAG, "unknown phenotype uri " + uriString, new Throwable());
                return null;
            }

            String namespace = path.get(0);

            mutator = map -> maybeUpdateMap(namespace, null, map, false);
        }

        if (mutator != null) {
            return modifyKvCursor(origCursor, projection, mutator);
        }

        return null;
    }

    private static Cursor modifyKvCursor(
            @Nullable Cursor origCursor,
            @Nullable String[] projection,
            Consumer<ArrayMap<String, Object>> mutator) {
        final int keyIndex = 0;
        final int valueIndex = 1;
        final int projectionLength = 2;

        if (origCursor != null) {
            projection = origCursor.getColumnNames();
        }

        boolean expectedProjection =
                projection != null
                        && projection.length == projectionLength
                        && "key".equals(projection[keyIndex])
                        && "value".equals(projection[valueIndex]);

        if (!expectedProjection) {
            Log.e(TAG, "unexpected projection " + Arrays.toString(projection), new Throwable());
            return null;
        }

        final ArrayMap<String, Object> map;
        if (origCursor == null) {
            map = new ArrayMap<>();
        } else {
            map = new ArrayMap<>(origCursor.getColumnCount() + 10);
            try (Cursor orig = origCursor) {
                while (orig.moveToNext()) {
                    String key = orig.getString(keyIndex);
                    String value = orig.getString(valueIndex);

                    map.put(key, value);
                }
            }
        }

        mutator.accept(map);

        final int mapSize = map.size();
        MatrixCursor result = new MatrixCursor(projection, mapSize);

        for (int i = 0; i < mapSize; ++i) {
            Object[] row = new Object[projectionLength];
            row[keyIndex] = map.keyAt(i);
            row[valueIndex] = map.valueAt(i);

            result.addRow(row);
        }

        return result;
    }

    public static void setFlags(Context appContext) {
        final String packageName = appContext.getPackageName();
        if (TextUtils.isEmpty(packageName)) return;
        if (!PACKAGE_GMS.equals(packageName)) return;

        final String processName = getProcessName(appContext);
        if (TextUtils.isEmpty(processName)) return;
        if (!PROCESS_GMS_PERSISTENT.equals(processName)) return;

        final BroadcastReceiver receiver =
                new BroadcastReceiver() {
                    private boolean isPhenotypeCommitted = false;

                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (PHENOTYPE_ACTION_COMMITTED.equals(intent.getAction())) {
                            if (isPhenotypeCommitted) {
                                return;
                            }
                            isPhenotypeCommitted = true;
                        }
                        Log.d(TAG, "received " + intent);
                        applyOverrides(appContext);
                    }
                };

        // Most phenotype flags and all Gservices flags are stored on user-encrypted storage,
        // i.e. they can't be updated while the device is in Direct Boot state
        final IntentFilter filter = new IntentFilter(Intent.ACTION_USER_UNLOCKED);
        // In some cases phenotype service isn't ready to accept overrides at user_unlocked time and
        // at process init time. It's always ready by the time phenotype ACTION_COMMITTED is sent.
        filter.addAction(PHENOTYPE_ACTION_COMMITTED);
        appContext.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);

        applyOverrides(appContext);
    }

    private static void applyOverrides(Context appContext) {
        final ArrayMap<String, String> gflags = getGserviceFlagsOverride();
        if (gflags != null) {
            final Intent intent = new Intent(GSERVICES_ACTION_OVERRIDE);
            intent.setPackage(PACKAGE_GMS);
            gflags.forEach(intent::putExtra);
            appContext.sendBroadcast(intent);
        }

        final ArrayMap<String, FlagsList> pflags = getPhenotypeFlagsOverride();
        for (String flagPackageName : pflags.keySet()) {
            FlagsList flagsList = pflags.get(flagPackageName);
            HashSet<String> namespaces = getNamespacesSet(flagPackageName, false);

            for (String namespace : namespaces) {
                final Intent intent = new Intent(PHENOTYPE_ACTION_OVERRIDE);
                intent.setPackage(PACKAGE_GMS);
                intent.putExtra("package", namespace);
                intent.putExtra("user", "*");
                intent.putExtra("flags", flagsList.flags.toArray(new String[0]));
                intent.putExtra("values", flagsList.values.toArray(new String[0]));
                intent.putExtra("types", flagsList.types.toArray(new String[0]));
                appContext.sendBroadcast(intent);
            }
        }
    }

    // SharedPreferencesImpl#getAll
    public static void maybeModifySharedPreferencesValues(String path, Map<String, Object> map) {
        if (!sEnablePhenotypeFlagsUtils) return;

        if (path == null || !path.endsWith(".xml")) {
            return;
        }

        Map<String, Object> mapTmp = map;
        String[] pathStr = path.split("/");
        if (pathStr.length < 5) {
            return;
        }

        String pkg = pathStr[4];
        String fileName = pathStr[pathStr.length - 1];
        // some PhenotypeFlags are stored in SharedPreferences instead of phenotype.db database
        if (maybeUpdateMap(pkg + "/" + fileName, null, mapTmp, true)) {
            map.putAll(mapTmp);
        }
    }

    private static void logd(String msg) {
        if (DEBUG) Log.d(TAG, msg);
    }
}
