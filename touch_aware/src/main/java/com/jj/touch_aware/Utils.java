package com.jj.touch_aware;

import android.graphics.PointF;
import android.util.Log;

import java.util.List;

class Utils {
    private static final String TAG = "Utils";

    /**
     * 获取不规则多边形重心点
     */
    public static PointF getCenterOfGravityPoint(List<PointF> mPoints) {
        if (mPoints == null || mPoints.size() == 0) {
            Log.e(TAG, "getCenterOfGravityPoint 传参非法!");
            return null;
        }

        float addUpX = 0;
        float addUpy = 0;

        for (PointF point : mPoints) {
            addUpX += point.x;
            addUpy += point.y;
        }

        int size = mPoints.size();
        return new PointF(addUpX / size, addUpy / size);
    }


    public static double getDistanceOf2Point(PointF pointF0, PointF pointF1) {
        float xDis = pointF0.x - pointF1.x;
        float yDis = pointF0.y - pointF1.y;
        double sqrt = Math.sqrt(xDis * xDis + yDis * yDis);
        Log.d(TAG, "getDistanceOf2Point sqrt=" + sqrt + "?xDis=" + xDis + "?yDis=" + yDis);
        return sqrt;
    }
}
