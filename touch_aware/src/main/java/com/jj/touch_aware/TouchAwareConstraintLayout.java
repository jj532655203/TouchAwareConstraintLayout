package com.jj.touch_aware;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * Jay
 * 能感知单指/双指/三指/四指/一块大面积接触(腕部接触)/笔写的ConstraintLayout容器
 */
public class TouchAwareConstraintLayout extends ConstraintLayout {

    public TouchAwareConstraintLayout(Context context) {
        this(context, null);
    }

    public TouchAwareConstraintLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TouchAwareConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // TODO: 2020/3/8
        return super.dispatchTouchEvent(ev);
    }
}
