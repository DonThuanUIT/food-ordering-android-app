package com.foodorderingapp.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.foodorderingapp.model.response.AdminDailyOrderResponse;

import java.util.ArrayList;
import java.util.List;

public class AdminOrderChartView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<AdminDailyOrderResponse> data = new ArrayList<>();

    public AdminOrderChartView(Context context) {
        super(context);
        init();
    }

    public AdminOrderChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AdminOrderChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        barPaint.setColor(Color.parseColor("#B24C08"));
        gridPaint.setColor(Color.parseColor("#E5E7EB"));
        gridPaint.setStrokeWidth(2f);
    }

    public void setData(List<AdminDailyOrderResponse> newData) {
        data.clear();
        if (newData != null) {
            data.addAll(newData);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth() - getPaddingStart() - getPaddingEnd();
        int height = getHeight() - getPaddingTop() - getPaddingBottom();
        int left = getPaddingStart();
        int top = getPaddingTop();
        int bottom = top + height;

        for (int i = 1; i <= 3; i++) {
            float y = top + (height * i / 4f);
            canvas.drawLine(left, y, left + width, y, gridPaint);
        }

        if (data.isEmpty() || width <= 0 || height <= 0) {
            return;
        }

        long max = 1L;
        for (AdminDailyOrderResponse item : data) {
            max = Math.max(max, item.getTotalOrders());
        }

        float slot = width / (float) data.size();
        float barWidth = Math.max(4f, slot * 0.58f);

        for (int i = 0; i < data.size(); i++) {
            float barHeight = height * (data.get(i).getTotalOrders() / (float) max);
            float x = left + (i * slot) + ((slot - barWidth) / 2f);
            canvas.drawRoundRect(x, bottom - barHeight, x + barWidth, bottom, 8f, 8f, barPaint);
        }
    }
}
