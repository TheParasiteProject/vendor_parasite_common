/*
 * Copyright (C) 2021 The ProtonAOSP Project
 * Copyright (C) 2022-2024 GrapheneOS
 * Copyright (C) 2024 TheParasiteProject
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
import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Base64;
import android.util.Log;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.List;
import java.util.Map;

import org.lineageos.platform.internal.R;

/**
 * @hide
 */
public class PhenotypeFlagsUtils {

    private static final String TAG = PhenotypeFlagsUtils.class.getSimpleName();
    private static final boolean DEBUG = true;

    private static Boolean sEnablePhenotypeFlagsUtils =
            SystemProperties.getBoolean("persist.sys.pfhooks.enable", true);

    private static final String SHARED_PREFS = "SharedPreferences";

    public static final String NAMESPACE_GSERVICES = "gservices";

    public static final String PACKAGE_GMS = "com.google.android.gms";
    public static final String PACKAGE_GSF = "com.google.android.gsf";

    public static final String PHENOTYPE_URI_PREFIX = "content://"
            + PACKAGE_GMS + ".phenotype/";

    public static final String GSERVICES_URI = "content://"
            + PACKAGE_GSF + '.' + NAMESPACE_GSERVICES + "/prefix";

    private static boolean isNamespaceMatches(String namespaceArg, String namespace) {
        if (namespaceArg == null || namespace == null) return false;

        if (namespace.equals(namespaceArg)
            || namespace.equals(namespaceArg + "#" + namespaceArg)) {
            return true;
        }

        String[] global =
            Resources.getSystem().getStringArray(R.array.global_phenotype_package_namespaces);
        String[] device =
            Resources.getSystem().getStringArray(R.array.device_phenotype_package_namespaces);
        String[] all = Arrays.copyOf(global, global.length + device.length);
        System.arraycopy(device, 0, all, global.length, device.length);

        try {
            for (String p : all) {
                String[] pn = p.split("=");
                String pkg = pn[0];
                if (namespaceArg.equals(pkg)) {
                    for (String ns : pn[1].split(",")) {
                        if (namespace.equals(ns)) {
                            return true;
                        }
    
                        if (ns.startsWith(".")) {
                            if (namespace.equals(pkg + ns)) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logd("Exception occured while checking namespace match: " + e);
            return false;
        } 

        return false;
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

    private static final int PHENOTYPE_BASE64_FLAGS = Base64.NO_PADDING | Base64.NO_WRAP;

    private static boolean maybeUpdateMap(
            String namespaceArg,
            @Nullable String[] selectionArgs,
            Map map, boolean isSharedPref) {
        if (namespaceArg == null
            || map == null) {
            return false;
        }

        try {
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

                if (!isNamespaceMatches(namespaceArg, namespace)) {
                    continue;
                }

                Object value = "";
                if (kv.length > 1) {
                    if (isSharedPref) {
                        switch (type) {
                            case "int":
                                value = Long.parseLong(kv[1]);
                                break;
                            case "bool":
                                value = Boolean.parseBoolean(kv[1]);
                                break;
                            case "float":
                                value = Float.parseFloat(kv[1]);
                                break;
                            case "string":
                                value = kv[1];
                                break;
                            case "extension":
                                value = Base64.decode(kv[1], PHENOTYPE_BASE64_FLAGS);
                                break;
                            default:
                                logd("Unsupported type specifier: " + type + " for config: " + p);
                                break;
                        }
                    } else {
                        if (type.equals("bool")) {
                            value = kv[1].equals("true") ? "1" : "0";
                        }
                    }
                }

                // Add extra check for gservices flag
                if (selectionArgs != null
                    && namespace.equals(NAMESPACE_GSERVICES)
                    && namespaceArg.equals(NAMESPACE_GSERVICES)) {
                    for (String sel : selectionArgs) {
                        if (key.startsWith(sel)) {
                            logd("maybeUpdateMap: " + namespace + "/" + sel);
                            map.put(key, value);
                            break;
                        }
                    }
                } else {
                    if (isSharedPref) {
                        if (map.keySet().contains(key)) {
                            map.put(key, value);
                        }
                    } else {
                        map.put(key, value);
                    }
                }
            }

            if (!map.isEmpty()) {
                return true;
            }
            return false;

        } catch (Exception e) {
            logd("Failed to update map: " + e);
        }
        return false;
    }

    // ContentResolver#query(Uri, String[], Bundle, CancellationSignal)
    public static Cursor maybeModifyQueryResult(
            Uri uri, @Nullable String[] projection,
            @Nullable Bundle queryArgs, @Nullable Cursor origCursor) {
        if (!sEnablePhenotypeFlagsUtils) return null;

        String uriString = uri.toString();

        Consumer<ArrayMap<String, Object>> mutator = null;

        if (GSERVICES_URI.equals(uriString)) {
            if (queryArgs == null) {
                return null;
            }
            String[] selectionArgs = queryArgs.getStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS);
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

    private static Cursor modifyKvCursor(@Nullable Cursor origCursor, @Nullable String[] projection,
                                         Consumer<ArrayMap<String, Object>> mutator) {
        final int keyIndex = 0;
        final int valueIndex = 1;
        final int projectionLength = 2;

        if (origCursor != null) {
            projection = origCursor.getColumnNames();
        }

        boolean expectedProjection = projection != null && projection.length == projectionLength
                && "key".equals(projection[keyIndex]) && "value".equals(projection[valueIndex]);

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

    // SharedPreferencesImpl#getAll
    public static void maybeModifySharedPreferencesValues(String path, Map<String, Object> map) {
        if (!sEnablePhenotypeFlagsUtils) return;

        if (path == null || !path.endsWith(".xml")) {
            return;
        }

        Map<String, Object> mapTmp = map;
        // some PhenotypeFlags are stored in SharedPreferences instead of phenotype.db database
        if (maybeUpdateMap(path.split("/")[4], null, mapTmp, true)) {
            map.putAll(mapTmp);
        }
    }

    private static void logd(String msg) {
        if (DEBUG) Log.d(TAG, msg);
    }
}
