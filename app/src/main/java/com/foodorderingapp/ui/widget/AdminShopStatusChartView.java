package com.foodorderingapp.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class AdminShopStatusChartView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final String[] labels = {"Chờ duyệt", "Đã duyệt", "Từ chối", "Bị khóa"};
    private final long[] values = new long[4];

    public AdminShopStatusChartView(Context context) {
        super(context);
        init();
    }

    public AdminShopStatusChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AdminShopStatusChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        barPaint.setColor(Color.parseColor("#B24C08"));
        gridPaint.setColor(Color.parseColor("#E5E7EB"));
        gridPaint.setStrokeWidth(2f);
        labelPaint.setColor(Color.parseColor("#5B6370"));
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(sp(11));
        valuePaint.setColor(Color.parseColor("#1A1D26"));
        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setTextSize(sp(14));
        valuePaint.setFakeBoldText(true);
        emptyPaint.setColor(Color.parseColor("#7D8494"));
        emptyPaint.setTextAlign(Paint.Align.CENTER);
        emptyPaint.setTextSize(sp(14));
    }

    public void setData(long pending, long approved, long rejected, long banned) {
        values[0] = Math.max(0, pending);
        values[1] = Math.max(0, approved);
        values[2] = Math.max(0, rejected);
        values[3] = Math.max(0, banned);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth() - getPaddingStart() - getPaddingEnd();
        int height = getHeight() - getPaddingTop() - getPaddingBottom();
        int left = getPaddingStart();
        int top = getPaddingTop();
        int bottom = top + height - dp(32);

        if (width <= 0 || height <= 0) {
            return;
        }

        long max = 0;
        long total = 0;
        for (long value : values) {
            max = Math.max(max, value);
            total += value;
        }

        for (int i = 1; i <= 3; i++) {
            float y = top + ((bottom - top) * i / 4f);
            canvas.drawLine(left, y, left + width, y, gridPaint);
        }

        if (total == 0) {
            canvas.drawText("Chưa có dữ liệu cửa hàng", left + width / 2f, top + height / 2f, emptyPaint);
            return;
        }

        float slot = width / 4f;
        float barWidth = Math.max(dp(18), slot * 0.46f);
        float chartHeight = bottom - top - dp(24);

        for (int i = 0; i < values.length; i++) {
            float centerX = left + (i * slot) + slot / 2f;
            float barHeight = Math.max(dp(6), chartHeight * (values[i] / (float) max));
            float barLeft = centerX - barWidth / 2f;
            float barTop = bottom - barHeight;
            canvas.drawRoundRect(barLeft, barTop, barLeft + barWidth, bottom, dp(8), dp(8), barPaint);
            canvas.drawText(String.valueOf(values[i]), centerX, barTop - dp(7), valuePaint);
            canvas.drawText(labels[i], centerX, top + height - dp(6), labelPaint);
        }
    }

    private float sp(int value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
