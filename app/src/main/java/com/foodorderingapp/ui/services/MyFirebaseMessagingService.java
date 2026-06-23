package com.foodorderingapp.ui.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.foodorderingapp.MainActivity;
import com.foodorderingapp.R;
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.utils.TokenManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM_SERVICE";
    private static final String CHANNEL_ID = "food_ordering_notifications";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        TokenManager.init(getApplicationContext());
        TokenManager.getInstance().saveFcmToken(token);
        sendRegistrationToServer(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        String title = null;
        String body = null;

        if (message.getNotification() != null) {
            title = message.getNotification().getTitle();
            body = message.getNotification().getBody();
        }

        if (isBlank(title)) {
            title = valueOrDefault(message.getData().get("title"), getString(R.string.app_name));
        }
        if (isBlank(body)) {
            body = valueOrDefault(message.getData().get("body"), "Ban co thong bao moi");
        }

        showNotification(title, body, message.getData());
    }

    private void showNotification(String title, String body, Map<String, String> data) {
        createNotificationChannel();

        Intent intent = new Intent(this, MainActivity.class);
        String role = TokenManager.getInstance().getRole();
        if (!isBlank(role)) {
            intent.putExtra("USER_ROLE", role);
        }
        String type = data == null ? null : data.get("type");
        if ("ORDER".equalsIgnoreCase(type)) {
            intent.putExtra("OPEN_TAB", "ORDERS");
        } else if ("SHOP_APPROVAL".equalsIgnoreCase(type)) {
            intent.putExtra("OPEN_TAB", "APPROVALS");
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_toast_info)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        try {
            NotificationManagerCompat.from(this)
                    .notify((int) System.currentTimeMillis(), builder.build());
        } catch (SecurityException exception) {
            Log.w(TAG, "Notification permission has not been granted", exception);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Food ordering notifications",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Order, shop, and account updates");

        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private void sendRegistrationToServer(String fcmToken) {
        String accessToken = TokenManager.getInstance().getAccessToken();
        if (isBlank(fcmToken) || isBlank(accessToken)) {
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("fcmToken", fcmToken);
        body.put("deviceInfo", Build.MANUFACTURER + " " + Build.MODEL);

        ApiClient.getApiService().registerDeviceToken(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d(TAG, response.isSuccessful()
                        ? "Device token registered"
                        : "Device token registration failed: " + response.code());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.w(TAG, "Device token registration network error", t);
            }
        });
    }

    private String valueOrDefault(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
