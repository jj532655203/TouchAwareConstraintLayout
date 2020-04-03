package com.jj.touch_aware;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.jj.touch_aware.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Jay
 * 能感知触控笔/单指/双指/三指/四指/一块大面积接触(腕部接触)
 * 注意!:事件流到达本容器,若上面的子view都不消费,一定会被id为FIRST_CHILD_VIEW_ID的子view消费,意味着:
 * 1.位于本viewGroup下方的兄弟view将收不到事件流;
 * 2.事件流不会从本viewGroup回到父容器中:
 * id为FIRST_CHILD_VIEW_ID的子view的兄弟view优先消费事件流(因为id为FIRST_CHILD_VIEW_ID的子view永远位于最下方)
 */
public class TouchAwareConstraintLayout extends ConstraintLayout {

    private static final String TAG = "TouchAwareConstra";
    private OnTouchAwareListener mOnTouchAwareListener;
    private float holdDownPressure = 6;
    private float holdDownArea = 300;
    private boolean[] consumedMultiFingerEvent = new boolean[1];

    public float getHoldDownPressure() {
        return holdDownPressure;
    }

    public void setHoldDownPressure(float holdDownPressure) {
        this.holdDownPressure = holdDownPressure;
    }

    public float getHoldDownArea() {
        return holdDownArea;
    }

    public void setHoldDownArea(float holdDownArea) {
        this.holdDownArea = holdDownArea;
    }

    public TouchAwareConstraintLayout(Context context) {
        this(context, null);
    }

    public TouchAwareConstraintLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TouchAwareConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }


    public void setOnTouchAwareListener(OnTouchAwareListener onTouchAwareListener) {
        mOnTouchAwareListener = onTouchAwareListener;
    }


    private boolean multiFinger = false;
    private PointF lastGravityCenterPoint;
    private Map<Integer, PointF> pointerIdLocationMap = new HashMap<>();

    /**
     * 为了保证子view申请不拦截情况下依然能处理事件流,所以重新本方法处理(而不是重写onInterceptTouchEvent)
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Log.d(TAG, "dispatchTouchEvent event=" + event.toString());

        if (mOnTouchAwareListener == null) {
            return super.dispatchTouchEvent(event);
        }

        int toolType = event.getToolType(0);
        if (toolType == MotionEvent.TOOL_TYPE_STYLUS) {

            //触控笔入场,其它触控cancel
            mOnTouchAwareListener.onActionFingerCancel();
            mOnTouchAwareListener.onStylusTouchEvent(event);
            return super.dispatchTouchEvent(event);
        }


        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {

                //所有值还原
                consumedMultiFingerEvent[0] = false;
                pointerIdLocationMap.clear();
                multiFinger = false;
                lastGravityCenterPoint = null;

                pointerIdLocationMap.put(0, new PointF(event.getX(), event.getY()));

                mOnTouchAwareListener.onSingleFingerTouchEvent(event);

            }
            break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                if (consumedMultiFingerEvent[0]) return super.dispatchTouchEvent(event);

                //记录新落下的点的位置
                int pointerIn = event.getActionIndex();
                pointerIdLocationMap.put(event.getPointerId(pointerIn), new PointF(event.getX(pointerIn), event.getY(pointerIn)));

                //记录新重心
                int pointerCount = event.getPointerCount();
                List<PointF> points = new ArrayList<>(pointerCount);
                for (int i = 0; i < pointerCount; i++) {
                    points.add(new PointF(event.getX(i), event.getY(i)));
                }
                lastGravityCenterPoint = Utils.getCenterOfGravityPoint(points);

                multiFinger = true;
                mOnTouchAwareListener.onActionSingleFingerCancel();
            }
            break;
            case MotionEvent.ACTION_POINTER_UP: {
                if (consumedMultiFingerEvent[0]) return super.dispatchTouchEvent(event);
                //不再记录该离开的点
                int pointerIn = event.getActionIndex();
                pointerIdLocationMap.remove(event.getPointerId(pointerIn));

                //记录新重心
                int pointerCount = event.getPointerCount();
                List<PointF> points = new ArrayList<>(pointerCount);
                for (int i = 0; i < pointerCount; i++) {
                    if (i == pointerIn) continue;
                    points.add(new PointF(event.getX(i), event.getY(i)));
                }
                lastGravityCenterPoint = Utils.getCenterOfGravityPoint(points);

            }
            break;
            case MotionEvent.ACTION_UP: {
                mOnTouchAwareListener.onActionUp();
                if (consumedMultiFingerEvent[0]) return super.dispatchTouchEvent(event);
                if (!multiFinger) {
                    mOnTouchAwareListener.onSingleFingerTouchEvent(event);
                }
            }
            break;
            case MotionEvent.ACTION_MOVE:
                if (consumedMultiFingerEvent[0]) return super.dispatchTouchEvent(event);

                if (!multiFinger) {
                    mOnTouchAwareListener.onSingleFingerTouchEvent(event);
                }

                int pointerCount = event.getPointerCount();

                //holdDown场景为笔写准备场景
                float activePressure = 0;
                float activeArea = 0;
                for (int i = 0; i < pointerCount; i++) {
                    float _pressure = event.getPressure();
                    if (activePressure < _pressure) activePressure = _pressure;
                    float _area = event.getSize();
                    if (activeArea < _area) activeArea = _area;
                }
                Log.d(TAG, "ACTION_MOVE activePressure = " + activePressure + "?activeArea=" + activeArea);
                if (activePressure > holdDownPressure || activeArea > holdDownArea) {
                    mOnTouchAwareListener.onActionFingerCancel();
                    mOnTouchAwareListener.onHoldDown();
                    break;
                }

                if (pointerCount > 4) {
                    //不处理5根以上手指触控
                    break;
                }
                motionEventMove(pointerCount, event);
                break;
        }

        return super.dispatchTouchEvent(event);
    }

    private void motionEventMove(int pointerCount, MotionEvent event) {
        switch (pointerCount) {
            case 1:
                mOnTouchAwareListener.onSingleFingerTouchEvent(event);
                break;
            case 3:
            case 4:
            case 2: {

                //缩放和移动分不开的
                //1.找到新的重心
                List<PointF> points = new ArrayList<>(pointerCount);
                List<Integer> pointerIds = new ArrayList<>();
                for (int i = 0; i < pointerCount; i++) {
                    points.add(new PointF(event.getX(i), event.getY(i)));
                    pointerIds.add(event.getPointerId(i));
                }
                Log.d(TAG, "手指数量 =" + points.size());
                PointF newCenterOfGravityPoint = Utils.getCenterOfGravityPoint(points);
                Log.d(TAG, "motionEventMove getCenterOfGravityPoint 新重心gx=" + newCenterOfGravityPoint.x + "?Gy=" + newCenterOfGravityPoint.y + "?旧重心x=" + lastGravityCenterPoint.x + "?y=" + lastGravityCenterPoint.y);

                //2.找到偏移量最大的activePointerId(以它的偏移量为准计算缩放比例)
                double activeOffset = 0;
                int activePointerId = -1;
                PointF activePoint = null;
                PointF activePointStart = null;
                float offsetX = 0;
                float offsety = 0;
                for (int pin = 0; pin < pointerCount; pin++) {
                    int pointerId = event.getPointerId(pin);
                    PointF newPoint = new PointF(event.getX(pin), event.getY(pin));
                    PointF oldPoint = pointerIdLocationMap.get(pointerId);
                    assert oldPoint != null;
                    double _distance = Utils.getDistanceOf2Point(newPoint, oldPoint);
                    if (activeOffset < _distance) {
                        activeOffset = _distance;
                        activePointerId = pointerId;
                        activePoint = newPoint;
                        activePointStart = oldPoint;
                        offsetX = newPoint.x - oldPoint.x;
                        offsety = newPoint.y - oldPoint.y;
                    }
                }
                if (activePointerId < 0) {
                    Log.d(TAG, "motionEventMove 逻辑异常:biggerOffsetPid<0");
                    return;
                }
                if (activeOffset <= 0) {
                    Log.d(TAG, "motionEventMove 偏移量最大的点移动量==0 return");
                    return;
                }

                Log.d(TAG, "motionEventMove 找到偏移量最大的点 activePointerId=" + activePointerId + "?offsetX=" + offsetX + "?offsety=" + offsety);

                //3.新权重半径/旧权重半径=scale
                double newRAddUp = 0;
                for (PointF point : points) {
                    newRAddUp += Utils.getDistanceOf2Point(point, newCenterOfGravityPoint);
                }
                double newR = newRAddUp / points.size();

                double oldRAddUp = 0;
                for (PointF point : pointerIdLocationMap.values()) {
                    oldRAddUp += Utils.getDistanceOf2Point(point, lastGravityCenterPoint);
                }
                double oldR = oldRAddUp / pointerIdLocationMap.values().size();
                double scale = newR / oldR;
                Log.d(TAG, "motionEventMove scale=" + scale + "?newR=" + newR + "?oldR=" + oldR);

                //4.前后重心的距离就是移动量
                double offSet = Utils.getDistanceOf2Point(lastGravityCenterPoint, newCenterOfGravityPoint);

                Log.d(TAG, "motionEventMove scale=" + scale + "?offset=" + offSet);

                float moveOffsetX = newCenterOfGravityPoint.x - lastGravityCenterPoint.x;
                float moveOffsetY = newCenterOfGravityPoint.y - lastGravityCenterPoint.y;
                mOnTouchAwareListener.onScaleOrMultiFingerMove(scale, moveOffsetX, moveOffsetY, pointerCount, consumedMultiFingerEvent);

                //更新重心
                lastGravityCenterPoint = newCenterOfGravityPoint;
                //更新所有点的位置
                for (int i = 0; i < pointerCount; i++) {
                    int pointerId = event.getPointerId(i);
                    pointerIdLocationMap.put(pointerId, new PointF(event.getX(i), event.getY(i)));
                }

            }
            break;
        }
    }


    public interface OnTouchAwareListener {

        /**
         * 缩放
         *
         * @param scale        缩放值,>1放大,<1缩小,=1未缩放(表示只移动)
         * @param moveOffsetx  x轴移动偏移量
         * @param moveOffsety  y轴移动偏移量
         * @param pointerCount 当前事件手指数
         * @param consumed     这是一个输出参数,事件流是否被消费
         */
        void onScaleOrMultiFingerMove(double scale, float moveOffsetx, float moveOffsety, int pointerCount, boolean[] consumed);

        /**
         * 单指的onTouchEvent
         * (用法:可用于单指触屏书写)
         */
        void onSingleFingerTouchEvent(MotionEvent event);

        /**
         * 单指事件取消(一般是由于手指数量发生变化)
         */
        void onActionSingleFingerCancel();


        /**
         * 手指触控事件取消(一般是由于触控笔事件发生了)
         */
        void onActionFingerCancel();

        /**
         * 触控笔事件
         * (用法:笔写)
         */
        void onStylusTouchEvent(MotionEvent event);


        /**
         * 被腕部按住或屏幕被大力按压
         */
        void onHoldDown();

        void onActionUp();
    }

    public static class SimpleOnTouchAwareListener implements OnTouchAwareListener {

        @Override
        public void onScaleOrMultiFingerMove(double scale, float moveOffsetx, float moveOffsety, int pointerCount, boolean[] consumed) {
        }

        @Override
        public void onSingleFingerTouchEvent(MotionEvent event) {
        }

        @Override
        public void onActionSingleFingerCancel() {
        }

        @Override
        public void onActionFingerCancel() {
        }

        @Override
        public void onStylusTouchEvent(MotionEvent event) {
        }

        @Override
        public void onHoldDown() {
        }

        @Override
        public void onActionUp() {
        }


    }

}
