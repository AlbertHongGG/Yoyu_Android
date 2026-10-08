package com.jasonhong.yoyu.core.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.jasonhong.yoyu.R;

public class DashedDividerView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private float dashWidth = 14f;
    private float dashGap = 10f;

    public DashedDividerView(Context context) {
        super(context);
        init(context, null);
    }

    public DashedDividerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DashedDividerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(ContextCompat.getColor(context, R.color.divider_light));
        setLayerType(LAYER_TYPE_SOFTWARE, paint); // Required for DashPathEffect
    }

    public void setDividerColor(int color) {
        paint.setColor(color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float y = getHeight() / 2f;
        path.reset();
        path.moveTo(0, y);
        path.lineTo(getWidth(), y);
        paint.setPathEffect(new DashPathEffect(new float[]{dashWidth, dashGap}, 0));
        canvas.drawPath(path, paint);
    }
}
