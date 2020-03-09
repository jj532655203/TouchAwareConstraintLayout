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

        if (mPoints.size() == 1) return mPoints.get(0);

        if (mPoints.size() == 2)
            return new PointF((mPoints.get(1).x + mPoints.get(0).x) / 2, (mPoints.get(1).y + mPoints.get(0).y) / 2);

        float area = 0;//多边形面积
        float Gx = 0, Gy = 0;// 重心的x、y
        for (int i = 1; i <= mPoints.size(); i++) {
            double iLat = mPoints.get(i % mPoints.size()).y;
            double iLng = mPoints.get(i % mPoints.size()).x;
            double nextLat = mPoints.get(i - 1).y;
            double nextLng = mPoints.get(i - 1).x;
            double temp = (iLat * nextLng - iLng * nextLat) / 2.0;
            area += temp;
            Gx += temp * (iLat + nextLat) / 3.0;
            Gy += temp * (iLng + nextLng) / 3.0;
        }
        Gx = Gx / area;
        Gy = Gy / area;
        Log.d(TAG, "getCenterOfGravityPoint gx=" + Gx + "?Gy=" + Gy);
        return new PointF(Gx, Gy);
    }

    public static boolean IsPointInCircle(PointF p, PointF circleCenter, double r) {
        //到圆心的距离 是否大于半径。半径是R
        //如O(x,y)点圆心，任意一点P（x1,y1） （x-x1）*(x-x1)+(y-y1)*(y-y1)>R*R 那么在圆外 反之在圆内
        float x = circleCenter.x;
        float y = circleCenter.y;
        float x1 = p.x;
        float y1 = p.y;


        if (!((x - x1) * (x - x1) + (y - y1) * (y - y1) > r * r)) {
            return true;    //当前点在圆内
        } else {
            return false;    //当前点在圆外
        }
    }

    public static double getDistanceOf2Point(PointF pointF0, PointF pointF1) {
        float xDis = pointF0.x - pointF1.x;
        float yDis = pointF0.y - pointF1.y;
        double sqrt = Math.sqrt(xDis * xDis + yDis * yDis);
        Log.d(TAG, "getDistanceOf2Point sqrt=" + sqrt + "?xDis=" + xDis + "?yDis=" + yDis);
        return sqrt;
    }
}
