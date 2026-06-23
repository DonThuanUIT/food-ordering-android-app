package com.foodorderingapp.ui.services;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM_SERVICE";

    /**
     * Hàm này tự động chạy khi Firebase cấp phát 1 Token mới cho điện thoại này.
     * Thường chạy ở lần đầu cài app hoặc khi xóa data app.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);

        // Lưu token này vào SharedPreferences (Local)
        // TokenManager.getInstance(this).saveFcmToken(token);

        // Gọi API lên Backend (POST /api/notifications/device-token) để báo cáo
        // sendRegistrationToServer(token);
    }

    /**
     * Hàm này chạy khi App ĐANG MỞ trên màn hình (Foreground) mà có thông báo tới.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        // Lấy data ngầm từ Backend gửi xuống (type, referenceId)
        if (message.getData().size() > 0) {
            String type = message.getData().get("type");
            String referenceId = message.getData().get("referenceId");
            Log.d(TAG, "Message data payload: " + type + " - " + referenceId);
        }

        // Lấy giao diện hiển thị (Title, Body)
        if (message.getNotification() != null) {
            String title = message.getNotification().getTitle();
            String body = message.getNotification().getBody();
            Log.d(TAG, "Message Notification Body: " + body);

            // Tự code logic hiển thị Notification (Ting Ting) bằng NotificationCompat ở đây
            // showNotification(title, body);
        }
    }
}