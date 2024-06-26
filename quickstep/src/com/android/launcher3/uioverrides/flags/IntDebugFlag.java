/*
 * Copyright (C) 2023 The Android Open Source Project
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
package com.android.launcher3.uioverrides.flags;

import androidx.annotation.Nullable;

import com.android.launcher3.ConstantItem;
import com.android.launcher3.config.FeatureFlags.IntFlag;

public class IntDebugFlag extends IntFlag {
    public final String key;
    private final int mDefaultValueInCode;
    @Nullable
    public final ConstantItem<Integer> launcherPrefFlag;

    public IntDebugFlag(String key, int currentValue, int defaultValueInCode,
            @Nullable ConstantItem<Integer> launcherPrefFlag) {
        super(currentValue);
        this.key = key;
        mDefaultValueInCode = defaultValueInCode;
        this.launcherPrefFlag = launcherPrefFlag;
    }

    @Override
    public String toString() {
        return key + ": mCurrentValue=" + get() + ", defaultValueInCode=" + mDefaultValueInCode;
    }
}
