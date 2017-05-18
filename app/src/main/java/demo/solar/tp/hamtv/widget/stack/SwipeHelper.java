package demo.solar.tp.hamtv.widget.stack;

import android.animation.Animator;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import demo.solar.tp.hamtv.widget.stack.util.AnimationUtils;

/**
 * Created by Filipe Rodrigues on 16/05/17
 *
 * Original credits to: 2016 Frederik Schweiger
 */

class SwipeHelper implements View.OnTouchListener {

    private final SwipeStack mSwipeStack;
    private View mObservedView;

    private boolean mListenForTouchEvents;
    private float mDownX;
    private float mDownY;
    private float mInitialX;
    private float mInitialY;
    private int mPointerId;

    private float mRotateDegrees = SwipeStack.DEFAULT_SWIPE_ROTATION;
    private float mOpacityEnd = SwipeStack.DEFAULT_SWIPE_OPACITY;
    private int mAnimationDuration = SwipeStack.DEFAULT_ANIMATION_DURATION;

    SwipeHelper(SwipeStack swipeStack) {
        mSwipeStack = swipeStack;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(!mListenForTouchEvents || !mSwipeStack.isEnabled()) {
                    return false;
                }

                v.getParent().requestDisallowInterceptTouchEvent(true);
                mSwipeStack.onSwipeStart();
                mPointerId = event.getPointerId(0);
                mDownX = event.getX(mPointerId);
                mDownY = event.getY(mPointerId);

                return true;

            case MotionEvent.ACTION_MOVE:
                int pointerIndex = event.findPointerIndex(mPointerId);
                if (pointerIndex < 0) return false;

                float dx = event.getX(pointerIndex) - mDownX;
                float dy = event.getY(pointerIndex) - mDownY;

                float newX = mObservedView.getX() + dx;
                float newY = mObservedView.getY() + dy;

                mObservedView.setX(newX);
                mObservedView.setY(newY);

                float dragDistanceX = newX - mInitialX;
                float swipeProgress = Math.min(Math.max(
                        dragDistanceX / mSwipeStack.getWidth(), -1), 1);

                mSwipeStack.onSwipeProgress(swipeProgress);

                if (mRotateDegrees > 0) {
                    float rotation = mRotateDegrees * swipeProgress;
                    mObservedView.setRotation(rotation);
                }

                if (mOpacityEnd < 1f) {
                    float alpha = 1 - Math.min(Math.abs(swipeProgress * 2), 1);
                    mObservedView.setAlpha(alpha);
                }

                return true;

            case MotionEvent.ACTION_UP:
                v.getParent().requestDisallowInterceptTouchEvent(false);
                mSwipeStack.onSwipeEnd();
                checkViewPosition();

                return true;

        }

        return false;
    }

    private void checkViewPosition() {
        if(!mSwipeStack.isEnabled()) {
            resetViewPosition();
            return;
        }

        float viewCenterHorizontal = mObservedView.getX() + (mObservedView.getWidth() / 2);
        float parentFirstThird = mSwipeStack.getWidth() / 3f;
        float parentLastThird = parentFirstThird * 2;

        if (viewCenterHorizontal < parentFirstThird &&
                mSwipeStack.getAllowedSwipeDirections() != SwipeStack.SWIPE_DIRECTION_ONLY_RIGHT) {
            swipeViewToLeft(mAnimationDuration / 2);
        } else if (viewCenterHorizontal > parentLastThird &&
                mSwipeStack.getAllowedSwipeDirections() != SwipeStack.SWIPE_DIRECTION_ONLY_LEFT) {
            swipeViewToRight(mAnimationDuration / 2);
        } else {
            resetViewPosition();
        }
    }

    private void resetViewPosition() {
        mObservedView.animate()
                .x(mInitialX)
                .y(mInitialY)
                .rotation(0)
                .alpha(1)
                .setDuration(mAnimationDuration)
                .setInterpolator(new OvershootInterpolator(1.4f))
                .setListener(null);
    }

    /**
     * Automatically swipe observed view to the left
     *
     * @param duration The duration of the animation
     * @param toAlpha Transition to alpha value while animating
     */
    private void swipeViewToLeft(int duration, float toAlpha) {
        if (!mListenForTouchEvents) return;
        mListenForTouchEvents = false;
        mObservedView.animate().cancel();
        mObservedView.animate()
                .x(-mSwipeStack.getWidth() + mObservedView.getX())
                .rotation(-mRotateDegrees)
                .alpha(toAlpha)
                .setDuration(duration)
                .setListener(new AnimationUtils.AnimationEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mSwipeStack.onViewSwipedToLeft();
                    }
                });
    }

    /**
     * Automatically swipe observed view to the right
     *
     * @param duration The duration of the animation
     * @param toAlpha Transition to alpha value while animating
     */
    private void swipeViewToRight(int duration, float toAlpha) {
        if (!mListenForTouchEvents) return;
        mListenForTouchEvents = false;
        mObservedView.animate().cancel();
        mObservedView.animate()
                .x(mSwipeStack.getWidth() + mObservedView.getX())
                .rotation(mRotateDegrees)
                .alpha(toAlpha)
                .setDuration(duration)
                .setListener(new AnimationUtils.AnimationEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mSwipeStack.onViewSwipedToRight();
                    }
                });
    }

    /**
     * Reigster the view to be observable and that will receive touch events
     *
     * @param view Is the view being observed and receiving touch events
     * @param initialX Original X position
     * @param initialY Original Y position
     */
    void registerObservedView(View view, float initialX, float initialY) {
        if (view == null) return;
        mObservedView = view;
        mObservedView.setOnTouchListener(this);
        mInitialX = initialX;
        mInitialY = initialY;
        mListenForTouchEvents = true;
    }

    /**
     * Unregister the observed view
     *
     */
    void unregisterObservedView() {
        if (mObservedView != null) {
            mObservedView.setOnTouchListener(null);
        }
        mObservedView = null;
        mListenForTouchEvents = false;
    }

    void setAnimationDuration(int duration) {
        mAnimationDuration = duration;
    }

    void setRotation(float rotation) {
        mRotateDegrees = rotation;
    }

    void setOpacityEnd(float alpha) {
        mOpacityEnd = alpha;
    }

    void swipeViewToLeft(float toAlpha) {
        swipeViewToLeft(mAnimationDuration, toAlpha);
    }

    void swipeViewToRight(float toAlpha) {
        swipeViewToRight(mAnimationDuration, toAlpha);
    }

}
