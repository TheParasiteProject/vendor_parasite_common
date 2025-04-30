/*
 * Copyright (C) 2023-2024 The Android Open Source Project
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

import android.content.Context;
import android.os.SystemProperties;
import android.text.TextUtils;

import org.lineageos.platform.internal.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @hide
 */
public final class GamesPropsUtils extends CommonPropsUtils {

    private static final String TAG = GamesPropsUtils.class.getSimpleName();
    private static final boolean DEBUG = false;

    private static Boolean sEnableGameProps =
            SystemProperties.getBoolean("persist.sys.gamehooks.enable", false);

    private static final Map<String, Map<String, Object>> propsToChange = new HashMap<>();
    private static final Map<String, String[]> packagesToChange = new HashMap<>();

    public static void init() {
        String[] input = getStringArrayResSafely(R.array.config_gameHook);
        if (input.length == 0) return;

        String currentKey = null;
        List<String> packagesRaw = new ArrayList<>();

        for (int i = 0; i < input.length; i++) {
            if (currentKey == null && !input[i].contains(":")) {
                continue;
            }
            if (currentKey != null && !input[i].contains(":")) {
                packagesRaw.add(input[i]);
                if (i == input.length - 1) {
                    List<String> packages = packagesRaw;
                    packagesToChange.put(currentKey, packages.toArray(new String[0]));
                }
                continue;
            }
            if (currentKey != null && input[i].contains(":")) {
                List<String> packages = packagesRaw;
                packagesToChange.put(currentKey, packages.toArray(new String[0]));
                packagesRaw.clear();
                currentKey = null;
            }
            String[] itemsRaw = input[i].split(":");

            if (itemsRaw.length < 2) {
                continue;
            }

            currentKey = itemsRaw[0];
            if (currentKey == null) {
                continue;
            }

            Object[] propsRaw = itemsRaw[1].split(",");
            if (propsRaw == null || propsRaw.length < 4) {
                currentKey = null;
                continue;
            }

            if (currentKey != null) {
                Map<String, Object> props = new HashMap<>();
                props.put("BRAND", propsRaw[0]);
                props.put("DEVICE", propsRaw[1]);
                props.put("MANUFACTURER", propsRaw[2]);
                props.put("MODEL", propsRaw[3]);
                propsToChange.put(currentKey, props);
            }
        }
    }

    static {
        init();
    }

    public static void setProps(Context context) {
        if (!sEnableGameProps) {
            return;
        }

        final String packageName = context.getPackageName();

        if (TextUtils.isEmpty(packageName)) {
            return;
        }

        for (String device : packagesToChange.keySet()) {
            String[] packages = packagesToChange.get(device);
            if (Arrays.asList(packages).contains(packageName)) {
                dlog("Defining props for: " + packageName);
                Map<String, Object> props = propsToChange.get(device);
                for (Map.Entry<String, Object> prop : props.entrySet()) {
                    String key = prop.getKey();
                    Object value = prop.getValue();
                    setPropValue(key, value);
                }
                break;
            }
        }
    }
}
