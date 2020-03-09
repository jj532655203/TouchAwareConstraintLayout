package com.jj.touchawareconstraintlayout;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.jj.touch_aware.TouchAwareConstraintLayout;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    double addUpScale = 1D;
    float addUpOffsetX;
    float addUpOffsetY;

    private TouchAwareConstraintLayout.SimpleOnTouchAwareListener simpleOnTouchAwareListener = new TouchAwareConstraintLayout.SimpleOnTouchAwareListener() {
        @Override
        public void onScaleOrMultiFingerMove(double scale, float moveOffsetx, float moveOffsety) {
            super.onScaleOrMultiFingerMove(scale, moveOffsetx, moveOffsety);
            Log.d(TAG, "onScaleOrMultiFingerMove scale=" + scale + " ? moveOffsetx=" + moveOffsetx + "?moveOffsety=" + moveOffsety);

            if (Double.isNaN(moveOffsetx) || Double.isNaN(moveOffsety) || Double.isInfinite(moveOffsetx) || Double.isInfinite(moveOffsety)) {
                Log.d(TAG, "onScaleOrMultiFingerMove 移动值有问题 忽略移动");
                return;
            }


            //移动
            addUpOffsetX += moveOffsetx;
            addUpOffsetY += moveOffsety;
            img.setTranslationX(addUpOffsetX);
            img.setTranslationY(addUpOffsetY);

            //缩放
            addUpScale *= scale;
            img.setScaleX((float) addUpScale);
            img.setScaleY((float) addUpScale);

        }
    };
    private ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        img = findViewById(R.id.img);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.outWidth = 1920;
        opts.outHeight = 1080;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.exercise_pic_cn_write, opts);
        img.setImageBitmap(bitmap);

        TouchAwareConstraintLayout touchAwareConstraintLayout = findViewById(R.id.touch_aware_container);
        touchAwareConstraintLayout.setOnTouchAwareListener(simpleOnTouchAwareListener);

    }
}
