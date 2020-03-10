package com.jj.touchawareconstraintlayout.utils;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.lang.reflect.Field;

public class ViewPager2Util {

    /**
     * ViewPager2 从未经过点击,直接接收action_move之后的事件流,将划不动,调用本方法后就可以了(之后viewPager2.setUserInputEnabled(true))
     */
    public static String enableWithoutDownTouch(ViewPager2 viewPager2) {
        if (viewPager2 == null) return "传参viewPager2为空";

        try {
            Class<? extends ViewPager2> viewPager2Class = viewPager2.getClass();
            Field field = viewPager2Class.getDeclaredField("mRecyclerView");
            field.setAccessible(true);
            Object recyclerViewImpl = field.get(viewPager2);
            Class<? extends RecyclerView> recyclerViewClass = (Class<? extends RecyclerView>) recyclerViewImpl.getClass().getSuperclass();
            Field scrollPointerIdFild = recyclerViewClass.getDeclaredField("mScrollPointerId");
            scrollPointerIdFild.setAccessible(true);
            scrollPointerIdFild.setInt(recyclerViewImpl, 0);
            return null;
        } catch (Exception e) {
            return Log.getStackTraceString(e);
        }

    }
}
