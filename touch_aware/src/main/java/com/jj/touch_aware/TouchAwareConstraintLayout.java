package com.jj.touch_aware;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;

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
    private static final int FIRST_CHILD_VIEW_ID = 101;
    private OnTouchAwareListener mOnTouchAwareListener;

    public TouchAwareConstraintLayout(Context context) {
        this(context, null);
    }

    public TouchAwareConstraintLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TouchAwareConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //容器内最底下的子view是消费事件的-->为了保证事件流会传下来
        View view = new View(this.getContext()) {
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                return true;
            }
        };
        view.setId(FIRST_CHILD_VIEW_ID);
        super.addView(view);

    }

    @Override
    public void addView(View child, int index) {
        if (index == 0) index = 1;
        super.addView(child, index);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (index == 0) index = 1;
        super.addView(child, index, params);
    }

    @Override
    public void removeView(View view) {
        if (view.getId() == FIRST_CHILD_VIEW_ID) return;
        super.removeView(view);
    }

    @Override
    public void removeViewAt(int index) {
        if (index == 0) return;
        super.removeViewAt(index);
    }

    @Override
    public void removeViews(int start, int count) {
        if (count == 0) return;
        if (start == 0) start = 1;
        super.removeViews(start, count);
    }

    @Override
    public void removeAllViews() {
        int childCount = getChildCount();
        if (childCount == 1) return;
        removeViews(1, childCount);
        super.removeAllViews();
    }

    @Override
    public void removeViewInLayout(View view) {
        if (view.getId() == FIRST_CHILD_VIEW_ID) return;
        super.removeViewInLayout(view);
    }

    @Override
    public void removeViewsInLayout(int start, int count) {
        if (count == 0) return;
        if (start == 0) start = 1;
        super.removeViewsInLayout(start, count);
    }

    @Override
    public void removeAllViewsInLayout() {
        int childCount = getChildCount();
        if (childCount == 1) return;
        removeViews(1, childCount);
        super.removeAllViewsInLayout();
    }

    public void setOnTouchAwareListener(OnTouchAwareListener onTouchAwareListener) {
        mOnTouchAwareListener = onTouchAwareListener;
    }


    private boolean multiFinger = false;
    private PointF downPoint = new PointF(0, 0);
    private PointF lastGravityCenterPoint;
    private int lastActivePointerId = 0;

    Map<Integer, PointF> pointerIdLocationMap = new HashMap<>();

    /**
     * 为了保证子view申请不拦截情况下依然能处理事件流,所以重新本方法处理(而不是重写onInterceptTouchEvent)
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Log.d(TAG, "dispatchTouchEvent event=" + event.toString());

        if (mOnTouchAwareListener == null) {
            return super.dispatchTouchEvent(event);
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                pointerIdLocationMap.put(0, new PointF(event.getX(), event.getY()));

                downPoint.set(event.getX(), event.getY());
                mOnTouchAwareListener.onSingleFingerTouchEvent(event);
            }
            break;
            case MotionEvent.ACTION_POINTER_DOWN: {
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
            case MotionEvent.ACTION_MOVE:
                int pointerCount = event.getPointerCount();
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

                //3.activePointerId向重心移动-->缩,反之-->放大
//                double r = Utils.getDistanceOf2Point(lastGravityCenterPoint, activePointStart);
//                boolean shrink = !Double.isInfinite(r) && r != 0 && Utils.IsPointInCircle(activePoint, lastGravityCenterPoint, r);
//                Log.d(TAG, "motionEventMove 半径r=" + r );

                //4.上次activePointerId距离旧重心的距离,本次motionEvent中activePointerId距离本次重心的距离,2者比例即为缩放比例
                double distanceOf2NewCenter = Utils.getDistanceOf2Point(activePoint, newCenterOfGravityPoint);
                double distanceOf2OldCenter = Utils.getDistanceOf2Point(pointerIdLocationMap.get(activePointerId), lastGravityCenterPoint);
                double scale = distanceOf2NewCenter / distanceOf2OldCenter;
                Log.d(TAG, "motionEventMove scale=" + scale + "?distanceOf2OldCenter=" + distanceOf2OldCenter + "?distanceOf2NewCenter=" + distanceOf2NewCenter);

                //5.前后重心的距离就是移动量
                double offSet = Utils.getDistanceOf2Point(lastGravityCenterPoint, newCenterOfGravityPoint);

                Log.d(TAG, "motionEventMove scale=" + scale + "?offset=" + offSet);
                if (mOnTouchAwareListener != null) {
                    mOnTouchAwareListener.onScaleOrMultiFingerMove(scale, newCenterOfGravityPoint.x - lastGravityCenterPoint.x, newCenterOfGravityPoint.y - lastGravityCenterPoint.y);
                }

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


    //判断当前触摸类型
//        int toolType = event.getToolType(0);
//        if (toolType == MotionEvent.TOOL_TYPE_STYLUS) {
////            ToastUtil.show(this,"当前为触控笔");
//        } else if (toolType == MotionEvent.TOOL_TYPE_FINGER) {
////            ToastUtil.show(this,"当前为手指");
//        } else if (toolType == MotionEvent.TOOL_TYPE_MOUSE) {
////            ToastUtil.show(this,"当前为鼠标");
//        } else if (toolType == MotionEvent.TOOL_TYPE_UNKNOWN) {
////            ToastUtil.show(this,"当前为未知物品");
//        }


    public interface OnTouchAwareListener {

        public static final int DIRECTION_LEFT = 0;
        public static final int DIRECTION_TOP = 1;
        public static final int DIRECTION_RIGHT = 2;
        public static final int DIRECTION_BOTTOM = 3;

        /**
         * 缩放
         *
         * @param scale       缩放值,>1放大,<1缩小,=1未缩放(表示只移动)
         * @param moveOffsetx x轴移动偏移量
         * @param moveOffsety y轴移动偏移量
         */
        void onScaleOrMultiFingerMove(double scale, float moveOffsetx, float moveOffsety);

        /**
         * 单指的onTouchEvent
         */
        void onSingleFingerTouchEvent(MotionEvent event);

        /**
         * 单指事件取消(一般是由于手指数量发生变化)
         */
        void onActionSingleFingerCancel();


        /**
         * 2指往一个方向滑动(左/上/右/下)
         */
        void on2FingerActionMove(MotionEvent event, int direction);


        /**
         * 2指往一个方向甩(左/上/右/下)
         * 以最慢的那根手指速度为准
         */
        void on2FingerActionFling(MotionEvent event, int direction);


        /**
         * 2指事件取消(一般是由于手指数量发生变化)
         */
        void onActionDoubleFingerCancel();


        /**
         * 3指往一个方向滑动(左/上/右/下)
         */
        void on3FingerActionMove(MotionEvent event, int direction);


        /**
         * 3指往一个方向甩(左/上/右/下)
         * 以最慢的那根手指速度为准
         */
        void on3FingerActionFling(MotionEvent event, int direction);

        /**
         * 3指事件取消(一般是由于手指数量发生变化)
         */
        void onAction3FingerCancel();


        /**
         * 4指往一个方向滑动(左/上/右/下)
         */
        void on4FingerActionMove(MotionEvent event, int direction);


        /**
         * 4指往一个方向甩(左/上/右/下)
         * 以最慢的那根手指速度为准
         */
        void on4FingerActionFling(MotionEvent event, int direction);

        /**
         * 4指事件取消(一般是由于手指数量发生变化)
         */
        void onAction4FingerCancel();


        /**
         * 手指触控事件取消(一般是由于触控笔事件发生了)
         */
        void onActionFingerCancel();

        /**
         * 触控笔事件
         */
        void onStylusTouchEvent(MotionEvent event);


    }

    public static class SimpleOnTouchAwareListener implements OnTouchAwareListener {

        @Override
        public void onScaleOrMultiFingerMove(double scale, float moveOffsetx, float moveOffsety) {
        }

        @Override
        public void onSingleFingerTouchEvent(MotionEvent event) {
        }

        @Override
        public void onActionSingleFingerCancel() {
        }

        @Override
        public void on2FingerActionMove(MotionEvent event, int direction) {
        }

        @Override
        public void on2FingerActionFling(MotionEvent event, int direction) {
        }

        @Override
        public void onActionDoubleFingerCancel() {
        }

        @Override
        public void on3FingerActionMove(MotionEvent event, int direction) {
        }

        @Override
        public void on3FingerActionFling(MotionEvent event, int direction) {
        }

        @Override
        public void onAction3FingerCancel() {
        }

        @Override
        public void on4FingerActionMove(MotionEvent event, int direction) {
        }

        @Override
        public void on4FingerActionFling(MotionEvent event, int direction) {
        }

        @Override
        public void onAction4FingerCancel() {
        }

        @Override
        public void onActionFingerCancel() {
        }

        @Override
        public void onStylusTouchEvent(MotionEvent event) {
        }
    }

}
