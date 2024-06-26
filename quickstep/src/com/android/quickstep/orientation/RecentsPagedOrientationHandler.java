/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.quickstep.orientation;


import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.util.FloatProperty;
import android.util.Pair;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.android.launcher3.DeviceProfile;
import com.android.launcher3.touch.PagedOrientationHandler;
import com.android.launcher3.touch.SingleAxisSwipeDetector;
import com.android.launcher3.util.SplitConfigurationOptions;
import com.android.launcher3.util.SplitConfigurationOptions.SplitBounds;
import com.android.launcher3.util.SplitConfigurationOptions.SplitPositionOption;
import com.android.launcher3.util.SplitConfigurationOptions.StagePosition;
import com.android.quickstep.views.IconAppChipView;

import java.util.List;

/**
 * Abstraction layer to separate horizontal and vertical specific implementations
 * for {@link com.android.quickstep.views.RecentsView}. Majority of these implementations are
 * (should be) as simple as choosing the correct X and Y analogous methods.
 */
public interface RecentsPagedOrientationHandler extends PagedOrientationHandler {

    RecentsPagedOrientationHandler PORTRAIT = new PortraitPagedViewHandler();
    RecentsPagedOrientationHandler LANDSCAPE = new LandscapePagedViewHandler();
    RecentsPagedOrientationHandler SEASCAPE = new SeascapePagedViewHandler();

    <T> void setSecondary(T target, Float2DAction<T> action, float param);
    <T> void set(T target, Int2DAction<T> action, int primaryParam, int secondaryParam);
    int getPrimarySize(View view);
    float getPrimarySize(RectF rect);
    int getSecondaryTranslationDirectionFactor();
    float getDegreesRotated();
    int getRotation();
    boolean isLayoutNaturalToLauncher();

    <T> T getPrimaryValue(T x, T y);
    <T> T getSecondaryValue(T x, T y);
    void setPrimaryScale(View view, float scale);
    void setSecondaryScale(View view, float scale);
    float getStart(RectF rect);
    float getEnd(RectF rect);
    int getClearAllSidePadding(View view, boolean isRtl);
    int getSecondaryDimension(View view);
    FloatProperty<View> getPrimaryViewTranslate();
    FloatProperty<View> getSecondaryViewTranslate();
    int getSplitTranslationDirectionFactor(@StagePosition int stagePosition,
            DeviceProfile deviceProfile);
    Pair<FloatProperty, FloatProperty> getSplitSelectTaskOffset(FloatProperty primary,
            FloatProperty secondary, DeviceProfile deviceProfile);
    int getDistanceToBottomOfRect(DeviceProfile dp, Rect rect);
    List<SplitPositionOption> getSplitPositionOptions(DeviceProfile dp);
    /**
     * @param placeholderHeight height of placeholder view in portrait, width in landscape
     */
    void getInitialSplitPlaceholderBounds(int placeholderHeight, int placeholderInset,
            DeviceProfile dp, @StagePosition int stagePosition, Rect out);

    /**
     * Centers an icon in the split staging area, accounting for insets.
     * @param out The icon that needs to be centered.
     * @param onScreenRectCenterX The x-center of the on-screen staging area (most of the Rect is
     *                        offscreen).
     * @param onScreenRectCenterY The y-center of the on-screen staging area (most of the Rect is
     *                        offscreen).
     * @param fullscreenScaleX A x-scaling factor used to convert coordinates back into pixels.
     * @param fullscreenScaleY A y-scaling factor used to convert coordinates back into pixels.
     * @param drawableWidth The icon's drawable (final) width.
     * @param drawableHeight The icon's drawable (final) height.
     * @param dp The device profile, used to report rotation and hardware insets.
     * @param stagePosition 0 if the staging area is pinned to top/left, 1 for bottom/right.
     */
    void updateSplitIconParams(View out, float onScreenRectCenterX,
            float onScreenRectCenterY, float fullscreenScaleX, float fullscreenScaleY,
            int drawableWidth, int drawableHeight, DeviceProfile dp,
            @StagePosition int stagePosition);

    /**
     * Sets positioning and rotation for a SplitInstructionsView.
     * @param out The SplitInstructionsView that needs to be positioned.
     * @param dp The device profile, used to report rotation and device type.
     * @param splitInstructionsHeight The SplitInstructionView's height.
     * @param splitInstructionsWidth  The SplitInstructionView's width.
     */
    void setSplitInstructionsParams(View out, DeviceProfile dp, int splitInstructionsHeight,
            int splitInstructionsWidth);

    /**
     * @param splitDividerSize height of split screen drag handle in portrait, width in landscape
     * @param stagePosition the split position option (top/left, bottom/right) of the first
     *                           task selected for entering split
     * @param out1 the bounds for where the first selected app will be
     * @param out2 the bounds for where the second selected app will be, complimentary to
     *             {@param out1} based on {@param initialSplitOption}
     */
    void getFinalSplitPlaceholderBounds(int splitDividerSize, DeviceProfile dp,
            @StagePosition int stagePosition, Rect out1, Rect out2);

    int getDefaultSplitPosition(DeviceProfile deviceProfile);

    /**
     * @param outRect This is expected to be the rect that has the dimensions for a non-split,
     *                fullscreen task in overview. This will directly be modified.
     * @param desiredStagePosition Which stage position (topLeft/rightBottom) we want to resize
     *                           outRect for
     */
    void setSplitTaskSwipeRect(DeviceProfile dp, Rect outRect, SplitBounds splitInfo,
            @SplitConfigurationOptions.StagePosition int desiredStagePosition);

    void measureGroupedTaskViewThumbnailBounds(View primarySnapshot, View secondarySnapshot,
            int parentWidth, int parentHeight,
            SplitBounds splitBoundsConfig, DeviceProfile dp, boolean isRtl);

    /**
     * Creates two Points representing the dimensions of the two tasks in a GroupedTaskView
     *
     * @return first -> primary task snapshot, second -> secondary task snapshot.
     * x -> width, y -> height
     */
    Pair<Point, Point> getGroupedTaskViewSizes(DeviceProfile dp, SplitBounds splitBoundsConfig,
            int parentWidth, int parentHeight);

    // Overview TaskMenuView methods
    /** Sets layout params on a task's app icon. Only use this when app chip is disabled. */
    void setTaskIconParams(FrameLayout.LayoutParams iconParams,
            int taskIconMargin, int taskIconHeight, int thumbnailTopMargin, boolean isRtl);

    /**
     * Sets layout params on the children of an app chip. Only use this when app chip is enabled.
     */
    void setIconAppChipChildrenParams(
            FrameLayout.LayoutParams iconParams, int chipChildMarginStart);

    void setIconAppChipMenuParams(IconAppChipView iconAppChipView,
            FrameLayout.LayoutParams iconMenuParams,
            int iconMenuMargin, int thumbnailTopMargin);
    void setSplitIconParams(View primaryIconView, View secondaryIconView,
            int taskIconHeight, int primarySnapshotWidth, int primarySnapshotHeight,
            int groupedTaskViewHeight, int groupedTaskViewWidth, boolean isRtl,
            DeviceProfile deviceProfile, SplitBounds splitConfig);

    /*
     * The following two methods try to center the TaskMenuView in landscape by finding the center
     * of the thumbnail view and then subtracting half of the taskMenu width. In this case, the
     * taskMenu width is the same size as the thumbnail width (what got set below in
     * getTaskMenuWidth()), so we directly use that in the calculations.
     */
    float getTaskMenuX(float x, View thumbnailView, DeviceProfile deviceProfile,
            float taskInsetMargin, View taskViewIcon);
    float getTaskMenuY(float y, View thumbnailView, int stagePosition,
            View taskMenuView, float taskInsetMargin, View taskViewIcon);
    int getTaskMenuWidth(View thumbnailView, DeviceProfile deviceProfile,
            @StagePosition int stagePosition);

    int getTaskMenuHeight(float taskInsetMargin, DeviceProfile deviceProfile, float taskMenuX,
            float taskMenuY);
    /**
     * Sets linear layout orientation for {@link com.android.launcher3.popup.SystemShortcut} items
     * inside task menu view.
     */
    void setTaskOptionsMenuLayoutOrientation(DeviceProfile deviceProfile,
            LinearLayout taskMenuLayout, int dividerSpacing,
            ShapeDrawable dividerDrawable);
    /**
     * Sets layout param attributes for {@link com.android.launcher3.popup.SystemShortcut} child
     * views inside task menu view.
     */
    void setLayoutParamsForTaskMenuOptionItem(LinearLayout.LayoutParams lp,
            LinearLayout viewGroup, DeviceProfile deviceProfile);

    /**
     * Calculates the position where a Digital Wellbeing Banner should be placed on its parent
     * TaskView.
     * @return A Pair of Floats representing the proper x and y translations.
     */
    Pair<Float, Float> getDwbLayoutTranslations(int taskViewWidth,
            int taskViewHeight, SplitBounds splitBounds, DeviceProfile deviceProfile,
            View[] thumbnailViews, int desiredTaskId, View banner);

    // The following are only used by TaskViewTouchHandler.
    /** @return Either VERTICAL or HORIZONTAL. */
    SingleAxisSwipeDetector.Direction getUpDownSwipeDirection();
    /** @return Given {@link #getUpDownSwipeDirection()}, whether POSITIVE or NEGATIVE is up. */
    int getUpDirection(boolean isRtl);
    /** @return Whether the displacement is going towards the top of the screen. */
    boolean isGoingUp(float displacement, boolean isRtl);
    /** @return Either 1 or -1, a factor to multiply by so the animation goes the correct way. */
    int getTaskDragDisplacementFactor(boolean isRtl);

    /**
     * Maps the velocity from the coordinate plane of the foreground app to that
     * of Launcher's (which now will always be portrait)
     */
    void adjustFloatingIconStartVelocity(PointF velocity);

    /**
     * Ensures that outStartRect left bound is within the DeviceProfile's visual boundaries
     * @param outStartRect The start rect that will directly be modified
     */
    void fixBoundsForHomeAnimStartRect(RectF outStartRect, DeviceProfile deviceProfile);

    /**
     * Determine the target translation for animating the FloatingTaskView out. This value could
     * either be an x-coordinate or a y-coordinate, depending on which way the FloatingTaskView was
     * docked.
     *
     * @param floatingTask The FloatingTaskView.
     * @param onScreenRect The current on-screen dimensions of the FloatingTaskView.
     * @param stagePosition STAGE_POSITION_TOP_OR_LEFT or STAGE_POSITION_BOTTOM_OR_RIGHT.
     * @param dp The device profile.
     * @return A float. When an animation translates the FloatingTaskView to this position, it will
     * appear to tuck away off the edge of the screen.
     */
    float getFloatingTaskOffscreenTranslationTarget(View floatingTask, RectF onScreenRect,
            @StagePosition int stagePosition, DeviceProfile dp);

    /**
     * Sets the translation of a FloatingTaskView along its "slide-in/slide-out" axis (could be
     * either x or y), depending on how the view is oriented.
     *
     * @param floatingTask The FloatingTaskView to be translated.
     * @param translation The target translation value.
     * @param dp The current device profile.
     */
    void setFloatingTaskPrimaryTranslation(View floatingTask, float translation, DeviceProfile dp);

    /**
     * Gets the translation of a FloatingTaskView along its "slide-in/slide-out" axis (could be
     * either x or y), depending on how the view is oriented.
     *
     * @param floatingTask The FloatingTaskView in question.
     * @param dp The current device profile.
     * @return The current translation value.
     */
    Float getFloatingTaskPrimaryTranslation(View floatingTask, DeviceProfile dp);
}
