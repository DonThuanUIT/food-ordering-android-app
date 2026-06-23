package com.foodorderingapp.ui.chat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.foodorderingapp.ui.adapter.ChatMessageAdapter;
import com.foodorderingapp.utils.ToastUtils;
import com.foodorderingapp.utils.TokenManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_SHOP_ID = "SHOP_ID";
    public static final String EXTRA_SHOP_NAME = "SHOP_NAME";
    public static final String EXTRA_ROOM_ID = "ROOM_ID";
    public static final String EXTRA_PEER_NAME = "PEER_NAME";

    private final Handler pollingHandler = new Handler(Looper.getMainLooper());
    private final Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            if (roomId != null) {
                loadHistory(false);
                pollingHandler.postDelayed(this, 4000);
            }
        }
    };

    private ChatMessageAdapter adapter;
    private RecyclerView rvMessages;
    private TextView tvEmpty;
    private EditText edtMessage;
    private ImageButton btnSend;
    private String shopId;
    private String shopName;
    private String roomId;
    private int lastMessageCount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        shopId = getIntent().getStringExtra(EXTRA_SHOP_ID);
        shopName = getIntent().getStringExtra(EXTRA_SHOP_NAME);
        roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);
        if (isBlank(shopId) && isBlank(roomId)) {
            ToastUtils.error(this, "Khong tim thay shop");
            finish();
            return;
        }

        bindViews();
        setupMessages();
        if (roomId == null) {
            findExistingRoom();
        } else {
            loadHistory(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startPolling();
    }

    @Override
    protected void onPause() {
        pollingHandler.removeCallbacks(pollingRunnable);
        super.onPause();
    }

    private void bindViews() {
        TextView tvTitle = findViewById(R.id.tvChatTitle);
        TextView tvSubtitle = findViewById(R.id.tvChatSubtitle);
        rvMessages = findViewById(R.id.rvChatMessages);
        tvEmpty = findViewById(R.id.tvChatEmpty);
        edtMessage = findViewById(R.id.edtChatMessage);
        btnSend = findViewById(R.id.btnSendChat);

        String peerName = getIntent().getStringExtra(EXTRA_PEER_NAME);
        tvTitle.setText(!isBlank(peerName) ? peerName : (isBlank(shopName) ? "Chat voi shop" : shopName));
        tvSubtitle.setText("Nhan tin truc tiep voi shop");

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
        adapter.setCurrentPhone(TokenManager.getInstance().getPhone());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);
    }

    private void findExistingRoom() {
        ApiClient.getApiService().getChatRooms().enqueue(new Callback<List<ChatRoomResponse>>() {
            @Override
            public void onResponse(Call<List<ChatRoomResponse>> call, Response<List<ChatRoomResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    showEmpty(true);
                    return;
                }

                ChatRoomResponse room = findRoomForShop(response.body());
                if (room == null || isBlank(room.getId())) {
                    showEmpty(true);
                    return;
                }

                roomId = room.getId();
                loadHistory(true);
                startPolling();
            }

            @Override
            public void onFailure(Call<List<ChatRoomResponse>> call, Throwable t) {
                showEmpty(true);
            }
        });
    }

    private ChatRoomResponse findRoomForShop(List<ChatRoomResponse> rooms) {
        for (ChatRoomResponse room : rooms) {
            if (room != null
                    && room.getShop() != null
                    && shopId.equalsIgnoreCase(room.getShop().getId())) {
                return room;
            }
        }
        return null;
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
            }
        });
    }

    private void sendMessage() {
        String content = edtMessage.getText().toString().trim();
        if (content.isEmpty()) {
            ToastUtils.info(this, "Nhap noi dung tin nhan");
            return;
        }

        setSending(true);
        SendChatMessageRequest request = new SendChatMessageRequest(
                roomId == null ? shopId : null,
                roomId,
                content
        );

        ApiClient.getApiService().sendChatMessage(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                setSending(false);
                if (!response.isSuccessful()) {
                    ToastUtils.error(ChatActivity.this, "Khong gui duoc tin nhan");
                    return;
                }

                edtMessage.setText("");
                if (roomId == null) {
                    findExistingRoom();
                } else {
                    loadHistory(true);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                setSending(false);
                ToastUtils.error(ChatActivity.this, "Loi ket noi khi gui tin nhan");
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
        if (roomId != null) {
            pollingHandler.postDelayed(pollingRunnable, 4000);
        }
    }

    private void showEmpty(boolean empty) {
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvMessages.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void setSending(boolean sending) {
        btnSend.setEnabled(!sending);
        edtMessage.setEnabled(!sending);
        btnSend.setAlpha(sending ? 0.55f : 1f);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
