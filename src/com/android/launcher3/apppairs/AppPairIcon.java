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

package com.android.launcher3.apppairs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.android.launcher3.BubbleTextView;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.R;
import com.android.launcher3.Reorderable;
import com.android.launcher3.dragndrop.DraggableView;
import com.android.launcher3.model.data.FolderInfo;
import com.android.launcher3.model.data.WorkspaceItemInfo;
import com.android.launcher3.util.MultiTranslateDelegate;
import com.android.launcher3.views.ActivityContext;

import java.util.Collections;
import java.util.Comparator;
import java.util.function.Predicate;

/**
 * A {@link android.widget.FrameLayout} used to represent an app pair icon on the workspace.
 * <br>
 * The app pair icon is two parallel background rectangles with rounded corners. Icons of the two
 * member apps are set into these rectangles.
 */
public class AppPairIcon extends FrameLayout implements DraggableView, Reorderable {
    private static final String TAG = "AppPairIcon";

    /**
     * Indicates that the app pair is currently launchable on the current screen.
     */
    private boolean mIsLaunchableAtScreenSize = true;

    // A view that holds the app pair icon graphic.
    private AppPairIconGraphic mIconGraphic;
    // A view that holds the app pair's title.
    private BubbleTextView mAppPairName;
    // The underlying ItemInfo that stores info about the app pair members, etc.
    private FolderInfo mInfo;

    // Required for Reorderable -- handles translation and bouncing movements
    private final MultiTranslateDelegate mTranslateDelegate = new MultiTranslateDelegate(this);
    private float mScaleForReorderBounce = 1f;

    public AppPairIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AppPairIcon(Context context) {
        super(context);
    }

    /**
     * Builds an AppPairIcon to be added to the Launcher.
     */
    public static AppPairIcon inflateIcon(int resId, ActivityContext activity,
            @Nullable ViewGroup group, FolderInfo appPairInfo) {
        DeviceProfile grid = activity.getDeviceProfile();
        LayoutInflater inflater = (group != null)
                ? LayoutInflater.from(group.getContext())
                : activity.getLayoutInflater();
        AppPairIcon icon = (AppPairIcon) inflater.inflate(resId, group, false);

        // Sort contents, so that left-hand app comes first
        Collections.sort(appPairInfo.contents, Comparator.comparingInt(a -> a.rank));

        icon.setClipToPadding(false);
        icon.setTag(appPairInfo);
        icon.setOnClickListener(activity.getItemOnClickListener());
        icon.mInfo = appPairInfo;

        if (icon.mInfo.contents.size() != 2) {
            Log.wtf(TAG, "AppPair contents not 2, size: " + icon.mInfo.contents.size());
            return icon;
        }

        icon.checkScreenSize();

        // Set up icon drawable area
        icon.mIconGraphic = icon.findViewById(R.id.app_pair_icon_graphic);
        icon.mIconGraphic.init(activity.getDeviceProfile(), icon);

        // Set up app pair title
        icon.mAppPairName = icon.findViewById(R.id.app_pair_icon_name);
        icon.mAppPairName.setCompoundDrawablePadding(0);
        FrameLayout.LayoutParams lp =
                (FrameLayout.LayoutParams) icon.mAppPairName.getLayoutParams();
        lp.topMargin = grid.iconSizePx + grid.iconDrawablePaddingPx;
        icon.mAppPairName.setText(appPairInfo.title);

        // Set up accessibility
        icon.setContentDescription(icon.getAccessibilityTitle(appPairInfo));
        icon.setAccessibilityDelegate(activity.getAccessibilityDelegate());

        return icon;
    }

    /**
     * Returns a formatted accessibility title for app pairs.
     */
    public String getAccessibilityTitle(FolderInfo appPairInfo) {
        CharSequence app1 = appPairInfo.contents.get(0).title;
        CharSequence app2 = appPairInfo.contents.get(1).title;
        return getContext().getString(R.string.app_pair_name_format, app1, app2);
    }

    // Required for DraggableView
    @Override
    public int getViewType() {
        return DRAGGABLE_ICON;
    }

    // Required for DraggableView
    @Override
    public void getWorkspaceVisualDragBounds(Rect outBounds) {
        mIconGraphic.getIconBounds(outBounds);
    }

    /** Sets the visibility of the icon's title text */
    public void setTextVisible(boolean visible) {
        if (visible) {
            mAppPairName.setVisibility(VISIBLE);
        } else {
            mAppPairName.setVisibility(INVISIBLE);
        }
    }

    // Required for Reorderable
    @Override
    public MultiTranslateDelegate getTranslateDelegate() {
        return mTranslateDelegate;
    }

    // Required for Reorderable
    @Override
    public void setReorderBounceScale(float scale) {
        mScaleForReorderBounce = scale;
        super.setScaleX(scale);
        super.setScaleY(scale);
    }

    // Required for Reorderable
    @Override
    public float getReorderBounceScale() {
        return mScaleForReorderBounce;
    }

    public FolderInfo getInfo() {
        return mInfo;
    }

    public View getIconDrawableArea() {
        return mIconGraphic;
    }

    public boolean isLaunchableAtScreenSize() {
        return mIsLaunchableAtScreenSize;
    }

    /**
     * Checks if the app pair is launchable in the current device configuration.
     *
     * App pairs can be "disabled" in two ways:
     * 1) One of the member WorkspaceItemInfos is disabled (i.e. the app software itself is paused
     * by the user or can't be launched).
     * 2) This specific instance of an app pair can't be launched due to screen size requirements.
     *
     * This method checks and updates #2. Both #1 and #2 are checked when app pairs are drawn
     * {@link AppPairIconGraphic#dispatchDraw(Canvas)} or clicked on
     * {@link com.android.launcher3.touch.ItemClickHandler#onClickAppPairIcon(View)}
     */
    public void checkScreenSize() {
        DeviceProfile dp = ActivityContext.lookupContext(getContext()).getDeviceProfile();
        // If user is on a small screen, we can't launch if either of the apps is non-resizeable
        mIsLaunchableAtScreenSize =
                dp.isTablet || getInfo().contents.stream().noneMatch(
                        wii -> wii.hasStatusFlag(WorkspaceItemInfo.FLAG_NON_RESIZEABLE));
    }

    /**
     * Called when WorkspaceItemInfos get updated, and the app pair icon may need to be redrawn.
     */
    public void maybeRedrawForWorkspaceUpdate(Predicate<WorkspaceItemInfo> itemCheck) {
        // If either of the app pair icons return true on the predicate (i.e. in the list of
        // updated apps), redraw the icon graphic (icon background and both icons).
        if (getInfo().contents.stream().anyMatch(itemCheck)) {
            checkScreenSize();
            mIconGraphic.invalidate();
        }
    }
}
