package demo.solar.tp.hamtv.widget.stack;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.FrameLayout;

import java.util.Random;

import demo.solar.tp.hamtv.R;

/**
 * Created by Filipe Rodrigues on 16/05/17
 *
 * Original credits to: 2016 Frederik Schweiger
 */
public class SwipeStack extends ViewGroup {

    public static final int SWIPE_DIRECTION_BOTH = 0;
    public static final int SWIPE_DIRECTION_ONLY_LEFT = 1;
    public static final int SWIPE_DIRECTION_ONLY_RIGHT = 2;

    public static final int DEFAULT_ANIMATION_DURATION = 300;
    public static final int DEFAULT_STACK_SIZE = 3;
    public static final int DEFAULT_STACK_ROTATION = 8;
    public static final float DEFAULT_SWIPE_ROTATION = 30f;
    public static final float DEFAULT_SWIPE_OPACITY = 1f;
    public static final float DEFAULT_SCALE_FACTOR = 1f;
    public static final boolean DEFAULT_DISABLE_HW_ACCELERATION = true;

    private static final String KEY_SUPER_STATE = "superState";
    private static final String KEY_CURRENT_INDEX = "currentIndex";

    private Adapter mAdapter;
    private Random mRandom;

    private int mAllowedSwipeDirections;
    private int mAnimationDuration;
    private int mCurrentViewIndex;
    private int mNumberOfStackedViews;
    private int mViewSpacing;
    private int mViewRotation;
    private float mSwipeRotation;
    private float mSwipeOpacity;
    private float mScaleFactor;
    private boolean mDisableHwAcceleration;
    private boolean mIsFirstLayout = true;

    private View mTopView;
    private SwipeHelper mSwipeHelper;
    private DataSetObserver mDataObserver;
    private SwipeStackListener mListener;
    private SwipeProgressListener mProgressListener;

    public SwipeStack(Context context) {
        this(context, null);
    }

    public SwipeStack(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeStack(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        readAttributes(attrs);
        initialize();
    }

    private void readAttributes(AttributeSet attributeSet) {
        TypedArray attrs = getContext().obtainStyledAttributes(attributeSet, R.styleable.SwipeStack);

        try {
            mAllowedSwipeDirections =
                    attrs.getInt(R.styleable.SwipeStack_allowed_swipe_directions,
                            SWIPE_DIRECTION_BOTH);
            mAnimationDuration =
                    attrs.getInt(R.styleable.SwipeStack_animation_duration,
                            DEFAULT_ANIMATION_DURATION);
            mNumberOfStackedViews =
                    attrs.getInt(R.styleable.SwipeStack_stack_size, DEFAULT_STACK_SIZE);
            mViewSpacing =
                    attrs.getDimensionPixelSize(R.styleable.SwipeStack_stack_spacing,
                            getResources().getDimensionPixelSize(R.dimen.default_stack_spacing));
            mViewRotation =
                    attrs.getInt(R.styleable.SwipeStack_stack_rotation, DEFAULT_STACK_ROTATION);
            mSwipeRotation =
                    attrs.getFloat(R.styleable.SwipeStack_swipe_rotation, DEFAULT_SWIPE_ROTATION);
            mSwipeOpacity =
                    attrs.getFloat(R.styleable.SwipeStack_swipe_opacity, DEFAULT_SWIPE_OPACITY);
            mScaleFactor =
                    attrs.getFloat(R.styleable.SwipeStack_scale_factor, DEFAULT_SCALE_FACTOR);
            mDisableHwAcceleration =
                    attrs.getBoolean(R.styleable.SwipeStack_disable_hw_acceleration,
                            DEFAULT_DISABLE_HW_ACCELERATION);
        } finally {
            attrs.recycle();
        }
    }

    private void initialize() {
        mRandom = new Random();

        setClipToPadding(false);
        setClipChildren(false);

        mSwipeHelper = new SwipeHelper(this);
        mSwipeHelper.setAnimationDuration(mAnimationDuration);
        mSwipeHelper.setRotation(mSwipeRotation);
        mSwipeHelper.setOpacityEnd(mSwipeOpacity);

        mDataObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                invalidate();
                requestLayout();
            }
        };
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_SUPER_STATE, super.onSaveInstanceState());
        bundle.putInt(KEY_CURRENT_INDEX, mCurrentViewIndex - getChildCount());
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mCurrentViewIndex = bundle.getInt(KEY_CURRENT_INDEX);
            state = bundle.getParcelable(KEY_SUPER_STATE);
        }

        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        if (mAdapter == null || mAdapter.isEmpty()) {
            mCurrentViewIndex = 0;
            removeAllViewsInLayout();
            return;
        }

        for (int x = getChildCount(); x < mNumberOfStackedViews; x++) {
            addNextView();
        }

        reorderItems();

        mIsFirstLayout = false;
    }

    private void addNextView() {
        /** Reset index if end reached **/
        if (mCurrentViewIndex >= mAdapter.getCount()){
            mCurrentViewIndex = 0;
        }

        if (mCurrentViewIndex < mAdapter.getCount()) {

            View bottomView = mAdapter.getView(mCurrentViewIndex, null, this);
            bottomView.setTag(R.id.new_view, true);

            if (!mDisableHwAcceleration) {
                bottomView.setLayerType(LAYER_TYPE_HARDWARE, null);
            }

            if (mViewRotation > 0) {
                bottomView.setRotation(mRandom.nextInt(mViewRotation) - (mViewRotation / 2));
            }

            int width = getWidth() - (getPaddingLeft() + getPaddingRight());
            int height = getHeight() - (getPaddingTop() + getPaddingBottom());

            LayoutParams params = bottomView.getLayoutParams();
            if (params == null) {
                params = new LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT);
            }

            int measureSpecWidth = MeasureSpec.AT_MOST;
            int measureSpecHeight = MeasureSpec.AT_MOST;

            if (params.width == LayoutParams.MATCH_PARENT) {
                measureSpecWidth = MeasureSpec.EXACTLY;
            }

            if (params.height == LayoutParams.MATCH_PARENT) {
                measureSpecHeight = MeasureSpec.EXACTLY;
            }

            bottomView.measure(measureSpecWidth | width, measureSpecHeight | height);
            addViewInLayout(bottomView, 0, params, true);

            mCurrentViewIndex++;
        }
    }

    private void reorderItems() {
        for (int x = 0; x < getChildCount(); x++) {
            View childView = getChildAt(x);
            int topViewIndex = getChildCount() - 1;

            int distanceToViewAbove = (topViewIndex * mViewSpacing) - (x * mViewSpacing);
            int newPositionX = (getWidth() - childView.getMeasuredWidth()) / 2;
            int newPositionY = distanceToViewAbove + getPaddingTop();

            childView.layout(
                    newPositionX,
                    getPaddingTop(),
                    newPositionX + childView.getMeasuredWidth(),
                    getPaddingTop() + childView.getMeasuredHeight());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                childView.setTranslationZ(x);
            }

            boolean isNewView = (boolean) childView.getTag(R.id.new_view);
            float scaleFactor = (float) Math.pow(mScaleFactor, getChildCount() - x);

            if (x == topViewIndex) {
                mSwipeHelper.unregisterObservedView();
                mTopView = childView;
                mSwipeHelper.registerObservedView(mTopView, newPositionX, newPositionY);
            }

            if (!mIsFirstLayout) {

                if (isNewView) {
                    childView.setTag(R.id.new_view, false);
                    childView.setAlpha(0);
                    childView.setY(newPositionY);
                    childView.setScaleY(scaleFactor);
                    childView.setScaleX(scaleFactor);
                }

                childView.animate()
                        .y(newPositionY)
                        .scaleX(scaleFactor)
                        .scaleY(scaleFactor)
                        .alpha(1)
                        .setDuration(mAnimationDuration);

            } else {
                childView.setTag(R.id.new_view, false);
                childView.setY(newPositionY);
                childView.setScaleY(scaleFactor);
                childView.setScaleX(scaleFactor);
            }
        }
    }

    private void removeTopView() {
        if (mTopView != null) {
            removeView(mTopView);
            mTopView = null;
        }

        if (getChildCount() == 0) {
            if (mListener != null) mListener.onStackEmpty();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    public void onSwipeStart() {
        if (mProgressListener != null) mProgressListener.onSwipeStart();
    }

    public void onSwipeProgress(float progress) {
        if (mProgressListener != null)
            mProgressListener.onSwipeProgress(progress);
    }

    public void onSwipeEnd() {
        if (mProgressListener != null) mProgressListener.onSwipeEnd();
    }

    public void onViewSwipedToLeft() {
        if (mListener != null) mListener.onViewSwipedToLeft();
        removeTopView();
    }

    public void onViewSwipedToRight() {
        if (mListener != null) mListener.onViewSwipedToRight();
        removeTopView();
    }

    /**
     * Returns the adapter currently in use in this SwipeStack.
     *
     * @return The adapter currently used to display data in this SwipeStack.
     */
    public Adapter getAdapter() {
        return mAdapter;
    }

    /**
     * Sets the data behind this SwipeView.
     *
     * @param adapter The Adapter which is responsible for maintaining the
     *                data backing this list and for producing a view to represent an
     *                item in that data set.
     * @see #getAdapter()
     */
    public void setAdapter(Adapter adapter) {
        if (mAdapter != null) mAdapter.unregisterDataSetObserver(mDataObserver);
        mAdapter = adapter;
        mAdapter.registerDataSetObserver(mDataObserver);
    }

    /**
     * Returns the allowed swipe directions.
     *
     * @return The currently allowed swipe directions.
     */
    public int getAllowedSwipeDirections() {
        return mAllowedSwipeDirections;
    }

    /**
     * Sets the allowed swipe directions.
     *
     * @param directions One of {@link #SWIPE_DIRECTION_BOTH},
     *                   {@link #SWIPE_DIRECTION_ONLY_LEFT}, or {@link #SWIPE_DIRECTION_ONLY_RIGHT}.
     */
    public void setAllowedSwipeDirections(int directions) {
        mAllowedSwipeDirections = directions;
    }

    /**
     * Register a callback to be invoked when the user has swiped the top view
     * left / right or when the stack gets empty.
     *
     * @param listener The callback that will run
     */
    public void setListener(@Nullable SwipeStackListener listener) {
        mListener = listener;
    }

    /**
     * Register a callback to be invoked when the user starts / stops interacting
     * with the top view of the stack.
     *
     * @param listener The callback that will run
     */
    public void setSwipeProgressListener(@Nullable SwipeProgressListener listener) {
        mProgressListener = listener;
    }

    /**
     * Get the view from the top of the stack.
     *
     * @return The view if the stack is not empty or null otherwise.
     */
    public View getTopView() {
        return mTopView;
    }

    /**
     * Programmatically dismiss the top view to the right.
     */
    public void swipeTopViewToRight(float toAlpha) {
        if (getChildCount() == 0) return;
        mSwipeHelper.swipeViewToRight(toAlpha);
    }

    /**
     * Programmatically dismiss the top view to the left.
     */
    public void swipeTopViewToLeft(float toAlpha) {
        if (getChildCount() == 0) return;
        mSwipeHelper.swipeViewToLeft(toAlpha);
    }

    /**
     * Resets the current adapter position and repopulates the stack.
     */
    public void resetStack() {
        mCurrentViewIndex = 0;
        removeAllViewsInLayout();
        requestLayout();
    }

    /**
     * Interface definition for a callback to be invoked when the top view was
     * swiped to the left / right or when the stack gets empty.
     */
    public interface SwipeStackListener {

        void onViewSwipedToLeft();

        void onViewSwipedToRight();

        void onStackEmpty();
    }

    /**
     * Interface definition for a callback to be invoked when the user
     * starts / stops interacting with the top view of the stack.
     */
    private interface SwipeProgressListener {

        void onSwipeStart();

        void onSwipeProgress(float progress);

        void onSwipeEnd();
    }
}
