package com.foodorderingapp.ui.chat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.request.SendChatMessageRequest;
import com.foodorderingapp.model.response.ChatMessageResponse;
import com.foodorderingapp.model.response.ChatRoomResponse;
import com.foodorderingapp.model.response.UserProfileResponse;
import com.foodorderingapp.ui.adapter.ChatMessageAdapter;
import com.foodorderingapp.utils.ToastUtils;
import com.foodorderingapp.utils.TokenManager;
import com.foodorderingapp.utils.constants.AppConstants;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.LifecycleEvent;
import ua.naiksoftware.stomp.dto.StompHeader;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    public static final String EXTRA_SHOP_ID = "SHOP_ID";
    public static final String EXTRA_SHOP_NAME = "SHOP_NAME";
    public static final String EXTRA_ORDER_ID = "ORDER_ID";
    public static final String EXTRA_ROOM_ID = "ROOM_ID";
    public static final String EXTRA_PEER_NAME = "PEER_NAME";

    private final Handler pollingHandler = new Handler(Looper.getMainLooper());
    private final Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isBlank(roomId)) {
                if (!realtimeConnected) {
                    loadHistory(false);
                }
                pollingHandler.postDelayed(this, 4000);
            }
        }
    };

    private final Gson gson = new Gson();
    private final CompositeDisposable stompDisposables = new CompositeDisposable();
    private ChatMessageAdapter adapter;
    private RecyclerView rvMessages;
    private TextView tvTitle;
    private TextView tvSubtitle;
    private TextView tvEmpty;
    private EditText edtMessage;
    private ImageButton btnSend;
    private String shopId;
    private String shopName;
    private String orderId;
    private String roomId;
    private String peerName;
    private StompClient stompClient;
    private String subscribedRoomId;
    private boolean realtimeConnected;
    private int lastMessageCount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        shopId = getIntent().getStringExtra(EXTRA_SHOP_ID);
        shopName = getIntent().getStringExtra(EXTRA_SHOP_NAME);
        orderId = getIntent().getStringExtra(EXTRA_ORDER_ID);
        roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);
        if (isBlank(shopId) && isBlank(orderId) && isBlank(roomId)) {
            ToastUtils.error(this, "Không tìm thấy cửa hàng");
            finish();
            return;
        }

        bindViews();
        setupMessages();
        loadCurrentUser();
        if (isBlank(roomId)) {
            if (!isBlank(orderId)) {
                getOrCreateRoomByOrder();
            } else {
                getOrCreateRoomByShop();
            }
        } else {
            loadHistory(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectRealtime();
        startPolling();
    }

    @Override
    protected void onPause() {
        pollingHandler.removeCallbacks(pollingRunnable);
        disconnectRealtime();
        super.onPause();
    }

    private void bindViews() {
        tvTitle = findViewById(R.id.tvChatTitle);
        tvSubtitle = findViewById(R.id.tvChatSubtitle);
        rvMessages = findViewById(R.id.rvChatMessages);
        tvEmpty = findViewById(R.id.tvChatEmpty);
        edtMessage = findViewById(R.id.edtChatMessage);
        btnSend = findViewById(R.id.btnSendChat);

        peerName = getIntent().getStringExtra(EXTRA_PEER_NAME);
        if (isBlank(peerName)) {
            peerName = shopName;
        }
        updatePeerName(isBlank(peerName) ? "Tin nhắn" : peerName);
        tvSubtitle.setText("Nhắn tin trực tiếp");

        findViewById(R.id.btnBackChat).setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> sendMessage());
        edtMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void setupMessages() {
        adapter = new ChatMessageAdapter();
        adapter.setPeerName(!isBlank(peerName) ? peerName : "Cửa hàng");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);
    }

    private void loadCurrentUser() {
        ApiClient.getApiService().getMyProfile().enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setCurrentUserId(response.body().getId());
                } else {
                    logApiError("load current user", response, false);
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                logNetworkError("load current user", t, false);
            }
        });
    }

    private void getOrCreateRoomByShop() {
        if (isBlank(shopId)) {
            showEmpty(true);
            return;
        }

        ApiClient.getApiService().getChatRoomByShop(shopId).enqueue(new Callback<ChatRoomResponse>() {
            @Override
            public void onResponse(Call<ChatRoomResponse> call, Response<ChatRoomResponse> response) {
                if (!response.isSuccessful() || response.body() == null || isBlank(response.body().getRoomId())) {
                    logApiError("open shop chat room", response, true);
                    showEmpty(true);
                    return;
                }

                ChatRoomResponse room = response.body();
                roomId = room.getRoomId();
                if (!isBlank(room.getPartnerName())) {
                    updatePeerName(room.getPartnerName());
                }
                loadHistory(true);
                connectRealtime();
                startPolling();
            }

            @Override
            public void onFailure(Call<ChatRoomResponse> call, Throwable t) {
                logNetworkError("open shop chat room", t, true);
                showEmpty(true);
            }
        });
    }

    private void getOrCreateRoomByOrder() {
        if (isBlank(orderId)) {
            showEmpty(true);
            return;
        }

        ApiClient.getApiService().getChatRoomByOrder(orderId).enqueue(new Callback<ChatRoomResponse>() {
            @Override
            public void onResponse(Call<ChatRoomResponse> call, Response<ChatRoomResponse> response) {
                if (!response.isSuccessful() || response.body() == null || isBlank(response.body().getRoomId())) {
                    logApiError("open order chat room", response, true);
                    showEmpty(true);
                    return;
                }

                ChatRoomResponse room = response.body();
                roomId = room.getRoomId();
                if (!isBlank(room.getPartnerName())) {
                    updatePeerName(room.getPartnerName());
                }
                loadHistory(true);
                connectRealtime();
                startPolling();
            }

            @Override
            public void onFailure(Call<ChatRoomResponse> call, Throwable t) {
                logNetworkError("open order chat room", t, true);
                showEmpty(true);
            }
        });
    }

    private void loadHistory(boolean scrollToBottom) {
        if (isBlank(roomId)) {
            showEmpty(true);
            return;
        }

        ApiClient.getApiService().getChatHistory(roomId).enqueue(new Callback<List<ChatMessageResponse>>() {
            @Override
            public void onResponse(Call<List<ChatMessageResponse>> call, Response<List<ChatMessageResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    logApiError("load chat history", response, scrollToBottom);
                    return;
                }

                List<ChatMessageResponse> messages = response.body();
                adapter.submitList(messages);
                showEmpty(messages.isEmpty());

                boolean hasNewMessages = messages.size() != lastMessageCount;
                lastMessageCount = messages.size();
                if (!messages.isEmpty() && (scrollToBottom || hasNewMessages)) {
                    rvMessages.scrollToPosition(messages.size() - 1);
                }
                markRoomRead();
            }

            @Override
            public void onFailure(Call<List<ChatMessageResponse>> call, Throwable t) {
                logNetworkError("load chat history", t, scrollToBottom);
            }
        });
    }

    private void sendMessage() {
        String content = edtMessage.getText().toString().trim();
        if (content.isEmpty()) {
            ToastUtils.info(this, "Nhập nội dung tin nhắn");
            return;
        }

        if (isBlank(roomId) && !isBlank(orderId)) {
            ToastUtils.info(this, "Đang mở phòng chat, vui lòng thử lại sau");
            getOrCreateRoomByOrder();
            return;
        }

        setSending(true);
        SendChatMessageRequest request = new SendChatMessageRequest(
                isBlank(roomId) ? shopId : null,
                isBlank(roomId) ? null : roomId,
                content
        );

        ApiClient.getApiService().sendChatMessage(request).enqueue(new Callback<ChatMessageResponse>() {
            @Override
            public void onResponse(Call<ChatMessageResponse> call, Response<ChatMessageResponse> response) {
                setSending(false);
                if (!response.isSuccessful()) {
                    logApiError("send chat message", response, true);
                    ToastUtils.error(ChatActivity.this, "Không gửi được tin nhắn");
                    return;
                }

                edtMessage.setText("");
                ChatMessageResponse sentMessage = response.body();
                if (sentMessage != null) {
                    if (!isBlank(sentMessage.getRoomId())) {
                        roomId = sentMessage.getRoomId();
                    }
                    appendMessage(sentMessage);
                }
                loadHistory(true);
                connectRealtime();
                startPolling();
            }

            @Override
            public void onFailure(Call<ChatMessageResponse> call, Throwable t) {
                setSending(false);
                logNetworkError("send chat message", t, true);
                ToastUtils.error(ChatActivity.this, "Lỗi kết nối khi gửi tin nhắn");
            }
        });
    }

    private void markRoomRead() {
        if (isBlank(roomId)) {
            return;
        }
        ApiClient.getApiService().markChatRoomRead(roomId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
            }
        });
    }

    private void startPolling() {
        pollingHandler.removeCallbacks(pollingRunnable);
        if (!isBlank(roomId)) {
            pollingHandler.postDelayed(pollingRunnable, 4000);
        }
    }

    private void connectRealtime() {
        if (isBlank(roomId) || roomId.equals(subscribedRoomId)) {
            return;
        }

        String token = TokenManager.getInstance().getAccessToken();
        if (isBlank(token)) {
            realtimeConnected = false;
            return;
        }

        disconnectRealtime();
        subscribedRoomId = roomId;
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, AppConstants.getWsChatUrl());

        ArrayList<StompHeader> headers = new ArrayList<>();
        headers.add(new StompHeader("Authorization", "Bearer " + token));

        Disposable lifecycleDisposable = stompClient.lifecycle()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    if (event.getType() == LifecycleEvent.Type.OPENED) {
                        realtimeConnected = true;
                    } else if (event.getType() == LifecycleEvent.Type.CLOSED
                            || event.getType() == LifecycleEvent.Type.ERROR) {
                        realtimeConnected = false;
                        startPolling();
                    }
                }, throwable -> {
                    realtimeConnected = false;
                    startPolling();
                });

        Disposable topicDisposable = stompClient.topic("/topic/chat/" + roomId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(message -> handleRealtimeMessage(message.getPayload()), throwable -> {
                    realtimeConnected = false;
                    startPolling();
                });

        stompDisposables.add(lifecycleDisposable);
        stompDisposables.add(topicDisposable);
        stompClient.connect(headers);
    }

    private void disconnectRealtime() {
        stompDisposables.clear();
        if (stompClient != null) {
            stompClient.disconnect();
            stompClient = null;
        }
        subscribedRoomId = null;
        realtimeConnected = false;
    }

    private void handleRealtimeMessage(String payload) {
        if (isBlank(payload)) {
            return;
        }

        try {
            ChatMessageResponse message = gson.fromJson(payload, ChatMessageResponse.class);
            if (message == null || isBlank(message.getRoomId())
                    || message.getRoomId().equalsIgnoreCase(roomId)) {
                loadHistory(true);
            }
        } catch (Exception ignored) {
            loadHistory(true);
        }
    }

    private void showEmpty(boolean empty) {
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvMessages.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void appendMessage(ChatMessageResponse message) {
        adapter.appendMessage(message);
        boolean empty = adapter.getMessageCount() == 0;
        showEmpty(empty);
        lastMessageCount = adapter.getMessageCount();
        if (!empty) {
            rvMessages.scrollToPosition(adapter.getMessageCount() - 1);
        }
    }

    private void setSending(boolean sending) {
        btnSend.setEnabled(!sending);
        edtMessage.setEnabled(!sending);
        btnSend.setAlpha(sending ? 0.55f : 1f);
    }

    private void updatePeerName(String name) {
        if (isBlank(name)) {
            return;
        }
        peerName = name.trim();
        if (tvTitle != null) {
            tvTitle.setText(peerName);
        }
        if (adapter != null) {
            adapter.setPeerName(peerName);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void logApiError(String action, Response<?> response, boolean showToast) {
        int code = response != null ? response.code() : -1;
        String detail = "HTTP " + code;
        if (response != null && response.errorBody() != null) {
            try {
                String body = response.errorBody().string();
                if (!isBlank(body)) {
                    detail += ": " + shorten(body);
                }
            } catch (Exception e) {
                Log.w(TAG, "Could not read error body for " + action, e);
            }
        }
        Log.w(TAG, action + " failed: " + detail);
        if (showToast) {
            ToastUtils.error(this, action + " failed (" + code + ")");
        }
    }

    private void logNetworkError(String action, Throwable t, boolean showToast) {
        String message = t != null && t.getMessage() != null ? t.getMessage() : "unknown network error";
        Log.e(TAG, action + " failed: " + message, t);
        if (showToast) {
            ToastUtils.error(this, action + " failed: " + shorten(message));
        }
    }

    private String shorten(String value) {
        if (value == null) {
            return "";
        }
        String compact = value.replace('\n', ' ').replace('\r', ' ').trim();
        return compact.length() > 120 ? compact.substring(0, 120) + "..." : compact;
    }
}
