package com.foodorderingapp.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;

import com.foodorderingapp.R;

public final class ToastUtils {
    private static Toast currentToast;

    private ToastUtils() {
    }

    public static void success(Context context, String message) {
        show(context, message, Toast.LENGTH_SHORT, Color.parseColor("#00A843"), R.drawable.ic_toast_success);
    }

    public static void error(Context context, String message) {
        show(context, message, Toast.LENGTH_LONG, Color.parseColor("#D92D20"), R.drawable.ic_toast_error);
    }

    public static void info(Context context, String message) {
        show(context, message, Toast.LENGTH_SHORT, Color.parseColor("#1A1D26"), R.drawable.ic_toast_info);
    }

    @SuppressWarnings("deprecation")
    private static void show(Context context, String message, int duration, int backgroundColor,
                             @DrawableRes int iconRes) {
        if (context == null || message == null || message.trim().isEmpty()) {
            return;
        }

        Context appContext = context.getApplicationContext();
        LinearLayout container = new LinearLayout(appContext);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setPadding(dp(appContext, 16), dp(appContext, 12), dp(appContext, 18), dp(appContext, 12));

        GradientDrawable background = new GradientDrawable();
        background.setColor(backgroundColor);
        background.setCornerRadius(dp(appContext, 18));
        background.setStroke(dp(appContext, 1), Color.argb(42, 255, 255, 255));
        container.setBackground(background);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            container.setElevation(dp(appContext, 10));
        }

        ImageView iconView = new ImageView(appContext);
        iconView.setImageResource(iconRes);
        iconView.setColorFilter(Color.WHITE);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(appContext, 22), dp(appContext, 22));
        iconParams.setMarginEnd(dp(appContext, 10));
        container.addView(iconView, iconParams);

        TextView messageView = new TextView(appContext);
        messageView.setText(message.trim());
        messageView.setTextColor(Color.WHITE);
        messageView.setTextSize(14);
        messageView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        messageView.setMaxLines(3);
        messageView.setLineSpacing(dp(appContext, 2), 1f);
        messageView.setMaxWidth(appContext.getResources().getDisplayMetrics().widthPixels - dp(appContext, 96));
        container.addView(messageView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        if (currentToast != null) {
            currentToast.cancel();
        }

        currentToast = new Toast(appContext);
        currentToast.setDuration(duration);
        currentToast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, dp(appContext, 94));
        currentToast.setView(container);
        currentToast.show();
    }

    private static int dp(Context context, int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}
