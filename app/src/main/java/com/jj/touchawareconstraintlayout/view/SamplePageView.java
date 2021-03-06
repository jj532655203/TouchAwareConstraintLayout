package com.jj.touchawareconstraintlayout.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

public class SamplePageView extends ConstraintLayout {

    private static final String TAG = "SamplePageView";
    private ImageView img;

    double addUpScale = 1D;
    float addUpOffsetX;
    float addUpOffsetY;

    public SamplePageView(Context context) {
        this(context, null);
    }

    public SamplePageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SamplePageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //pageView一定要保底消费事件流
        View view = new View(context);
        view.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "SamplePageView onTouch");
                return true;
            }
        });
        LayoutParams params = new LayoutParams(-1, -1);
        addView(view, params);

        img = new ImageView(context);
        LayoutParams contentViewParams = new LayoutParams(-1, -1);
        addView(img, contentViewParams);
        img.setScaleType(ImageView.ScaleType.FIT_XY);

    }

    public void loadImg(Integer bitmapResId) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.outWidth = 1920;
        opts.outHeight = 1080;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), bitmapResId, opts);
        img.setImageBitmap(bitmap);
    }

    public void onScaleOrMultiFingerMove(double scale, float moveOffsetx, float moveOffsety) {

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

}
