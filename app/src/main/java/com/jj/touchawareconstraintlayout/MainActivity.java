package com.jj.touchawareconstraintlayout;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.jj.touch_aware.TouchAwareConstraintLayout;
import com.jj.touchawareconstraintlayout.adapter.SetHomeworkPageAdapter;
import com.jj.touchawareconstraintlayout.utils.ViewPager2Util;
import com.jj.touchawareconstraintlayout.view.SamplePageView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private final TouchAwareConstraintLayout.SimpleOnTouchAwareListener simpleOnTouchAwareListener = new TouchAwareConstraintLayout.SimpleOnTouchAwareListener() {

        @Override
        public void onScaleOrMultiFingerMove(double scale, float moveOffsetx, float moveOffsety, int pointerCount, boolean[] consumed) {
            Log.d(TAG, "onScaleOrMultiFingerMove scale=" + scale + " ? moveOffsetx=" + moveOffsetx + "?moveOffsety=" + moveOffsety + "?pointerCount=" + pointerCount);

            if (Double.isNaN(moveOffsetx) || Double.isNaN(moveOffsety) || Double.isInfinite(moveOffsetx) || Double.isInfinite(moveOffsety)) {
                Log.d(TAG, "onScaleOrMultiFingerMove 移动值有问题 忽略移动");
                return;
            }

            //在此定义翻页规则
            if (pointerCount == 4 && Math.abs(moveOffsetx) > Math.abs(moveOffsety) && !consumed[0]) {
                consumed[0] = true;
                String errorMsg = ViewPager2Util.enableWithoutDownTouch(viewPager2);
                if (!TextUtils.isEmpty(errorMsg)) {
                    Log.e(TAG, "onScaleOrMultiFingerMove 反射获取RecyclerView失败 e=" + errorMsg);
                }
                if (!viewPager2.isUserInputEnabled()) viewPager2.setUserInputEnabled(true);
                return;
            }

            //缩放/移动图片
            SamplePageView pageView = viewPager2.findViewWithTag(viewPager2.getCurrentItem());
            if (pageView == null) {
                Log.e(TAG, "onScaleOrMultiFingerMove 低概率事件");
                return;
            }
            pageView.onScaleOrMultiFingerMove(scale, moveOffsetx, moveOffsety);

        }

        /**
         * 触控笔落笔啦
         */
        @Override
        public void onStylusTouchEvent(MotionEvent event) {
            super.onStylusTouchEvent(event);
            // TODO: 2020/3/9 开始笔写啦...
        }

        /**
         * 学生按住作业了,准备写字啦
         */
        @Override
        public void onHoldDown() {
            super.onHoldDown();
            // TODO: 2020/3/9 准备开始书写啦...
        }

        @Override
        public void onActionFingerCancel() {
            super.onActionFingerCancel();
//            viewPager2.setUserInputEnabled(false);
        }

    };
    private ViewPager2 viewPager2;
    //    private boolean fourFingerMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        TouchAwareConstraintLayout touchAwareConstraintLayout = findViewById(R.id.touch_aware_container);
        touchAwareConstraintLayout.setOnTouchAwareListener(simpleOnTouchAwareListener);
        touchAwareConstraintLayout.setHoldDownArea(300);
        touchAwareConstraintLayout.setHoldDownPressure(6);

        viewPager2 = findViewById(R.id.view_pager);
        viewPager2.setUserInputEnabled(false);

        setUpViewPager2();

    }

    private void setUpViewPager2() {
        int capacity = 10;
        List<Integer> adapterData = new ArrayList<>(capacity);
        for (int i = 0; i < capacity; i++) {
            adapterData.add(R.mipmap.exam);
        }
        viewPager2.setAdapter(new SetHomeworkPageAdapter(R.layout.layout_item_sample_page, adapterData));

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                viewPager2.setUserInputEnabled(false);
            }
        });

    }
}
