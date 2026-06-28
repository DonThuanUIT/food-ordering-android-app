package com.foodorderingapp.ui.chat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.request.AIRecommendationRequest;
import com.foodorderingapp.model.request.CartItemRequest;
import com.foodorderingapp.model.response.AIRecommendationResponse;
import com.foodorderingapp.model.response.FoodResponse;
import com.foodorderingapp.ui.shop.ShopDetailActivity;
import com.foodorderingapp.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AiRecommendationActivity extends AppCompatActivity implements AiMessageAdapter.OnRecommendationClickListener {

    public static final String EXTRA_SHOP_ID = "SHOP_ID";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 3001;

    private RecyclerView rvMessages;
    private AiMessageAdapter adapter;
    private final List<AiChatMessage> messageList = new ArrayList<>();
    private EditText etInput;
    private ImageButton btnSend;
    private ProgressBar progressLoading;
    private View layoutIntro;
    private String shopId;

    // GPS
    private Double userLat = null;
    private Double userLng = null;
    private boolean locationPermissionRequestedThisSession = false;

    // Danh sách tòa nhà KTX
    private final String[] BUILDING_NAMES = {"A1", "A2", "A3", "B1", "B2", "B3", "C1", "C2", "C3", "D1", "D2", "D3"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_recommendation);

        shopId = getIntent().getStringExtra(EXTRA_SHOP_ID);

        bindViews();
        setupChat();
        checkLocationPermissionSilent();
    }

    private void bindViews() {
        rvMessages = findViewById(R.id.rv_ai_chat_messages);
        etInput = findViewById(R.id.et_ai_input);
        btnSend = findViewById(R.id.btn_send_ai);
        progressLoading = findViewById(R.id.progress_ai_loading);
        layoutIntro = findViewById(R.id.layout_ai_intro);

        findViewById(R.id.btn_back_ai).setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> sendQuery());
        etInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendQuery();
                return true;
            }
            return false;
        });
    }

    private void setupChat() {
        adapter = new AiMessageAdapter(messageList, this);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);
    }

    /**
     * Kiểm tra quyền GPS một cách thụ động (không popup nếu chưa có)
     */
    private void checkLocationPermissionSilent() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getUserLastLocation();
        }
    }

    private void getUserLastLocation() {
        try {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (lastKnown == null) {
                        lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                    if (lastKnown != null) {
                        userLat = lastKnown.getLatitude();
                        userLng = lastKnown.getLongitude();
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("AiRecommendation", "Failed to get location: " + e.getMessage());
        }
    }

    private void sendQuery() {
        String query = etInput.getText().toString().trim();
        if (TextUtils.isEmpty(query)) {
            ToastUtils.info(this, "Vui lòng nhập câu hỏi của bạn");
            return;
        }

        // Add user message
        messageList.add(new AiChatMessage(true, query));
        adapter.notifyItemInserted(messageList.size() - 1);
        rvMessages.scrollToPosition(messageList.size() - 1);

        etInput.setText("");
        layoutIntro.setVisibility(View.GONE);
        setLoading(true);

        // Kiểm tra query có liên quan đến vị trí không
        boolean queryMentionsLocation = containsLocationKeyword(query);

        // Nếu query liên quan vị trí && chưa có GPS && chưa hỏi lần nào → popup xin quyền
        if (queryMentionsLocation && userLat == null && !locationPermissionRequestedThisSession) {
            showLocationPermissionDialog(query);
            return; // Đợi dialog xong sẽ gửi lại
        }

        // Gửi đến agentic chat
        sendToAgenticChat(query);
    }

    private boolean containsLocationKeyword(String query) {
        if (query == null) return false;
        String lower = query.toLowerCase();
        return lower.contains("gần") || lower.contains("xa") || lower.contains("ở đâu")
                || lower.contains("vị trí") || lower.contains("bán kính")
                || lower.contains("tòa") || lower.contains("ktx")
                || lower.contains("gần nhất") || lower.contains("gần đây");
    }

    private void showLocationPermissionDialog(String pendingQuery) {
        locationPermissionRequestedThisSession = true;

        new AlertDialog.Builder(this)
                .setTitle("📍 Bật vị trí để tìm quán gần bạn?")
                .setMessage("Để tôi gợi ý các quán ăn gần vị trí của bạn nhất, hãy cho phép ứng dụng truy cập vị trí nhé!")
                .setPositiveButton("Bật GPS", (dialog, which) -> {
                    // Xin quyền GPS
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            LOCATION_PERMISSION_REQUEST_CODE);

                    // Lưu pending query
                    etInput.setText(pendingQuery);
                    etInput.setSelection(pendingQuery.length());
                })
                .setNegativeButton("Để sau", (dialog, which) -> {
                    // Gửi không có GPS
                    sendToAgenticChat(pendingQuery);
                })
                .setNeutralButton("Tôi ở tòa KTX", (dialog, which) -> {
                    showBuildingSelectorDialog(pendingQuery);
                })
                .show();
    }

    private void showBuildingSelectorDialog(String pendingQuery) {
        new AlertDialog.Builder(this)
                .setTitle("Chọn tòa nhà KTX")
                .setItems(BUILDING_NAMES, (dialog, which) -> {
                    String buildingName = BUILDING_NAMES[which];
                    setLoading(true);
                    // Gửi với buildingName để backend tra cứu tọa độ
                    AIRecommendationRequest request = new AIRecommendationRequest(pendingQuery);
                    request.setBuildingName(buildingName);
                    sendAgenticChatRequest(request);
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    sendToAgenticChat(pendingQuery);
                })
                .show();
    }

    private void sendToAgenticChat(String query) {
        AIRecommendationRequest request = new AIRecommendationRequest(query);
        if (userLat != null && userLng != null) {
            request.setUserLat(userLat);
            request.setUserLng(userLng);
        }
        sendAgenticChatRequest(request);
    }

    private void sendAgenticChatRequest(AIRecommendationRequest request) {
        ApiClient.getApiService().agenticChat(request).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> body = response.body();
                    String text = (String) body.get("response");
                    Boolean suggestGps = (Boolean) body.get("suggestGps");

                    if (text != null) {
                        addAiMessage(text, null);
                    }

                    // Nếu AI đề xuất bật GPS và chưa hỏi, thì tự động popup
                    if (Boolean.TRUE.equals(suggestGps) && userLat == null
                            && !locationPermissionRequestedThisSession
                            && containsLocationKeyword(text)) {
                        showLocationPermissionDialog("");
                    }
                } else {
                    // Fallback về API recommend cũ
                    fallbackToOldRecommendation(request.getQuery());
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                setLoading(false);
                // Fallback về API recommend cũ
                fallbackToOldRecommendation(request.getQuery());
            }
        });
    }

    private void fallbackToOldRecommendation(String query) {
        UUID shopUuid = null;
        if (shopId != null && !shopId.trim().isEmpty()) {
            try {
                shopUuid = UUID.fromString(shopId);
            } catch (IllegalArgumentException e) {
                // Ignore
            }
        }

        AIRecommendationRequest request = new AIRecommendationRequest(query);
        ApiClient.getApiService().getAIRecommendations(request, shopUuid).enqueue(new Callback<List<AIRecommendationResponse>>() {
            @Override
            public void onResponse(Call<List<AIRecommendationResponse>> call, Response<List<AIRecommendationResponse>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<AIRecommendationResponse> recommendations = response.body();
                    if (recommendations.isEmpty()) {
                        addAiMessage("Xin lỗi, tôi không tìm thấy món ăn nào phù hợp với yêu cầu của bạn. Bạn hãy thử diễn đạt khác đi nhé!", null);
                    } else {
                        StringBuilder sb = new StringBuilder("Dưới đây là một số món ăn tôi tìm thấy cho bạn:\n\n");
                        List<FoodResponse> foods = new ArrayList<>();
                        for (AIRecommendationResponse recommendation : recommendations) {
                            FoodResponse food = recommendation.getFood();
                            if (food != null) {
                                foods.add(food);
                                sb.append("• ").append(food.getName()).append(": ").append(recommendation.getReason()).append("\n\n");
                            }
                        }
                        addAiMessage(sb.toString().trim(), foods);
                    }
                } else {
                    addAiMessage("Đã xảy ra lỗi khi trao đổi với trợ lý AI. Vui lòng thử lại sau!", null);
                }
            }

            @Override
            public void onFailure(Call<List<AIRecommendationResponse>> call, Throwable t) {
                setLoading(false);
                addAiMessage("Lỗi kết nối mạng! Vui lòng kiểm tra lại mạng wifi/4G của bạn.", null);
            }
        });
    }

    private void addAiMessage(String text, List<FoodResponse> foods) {
        messageList.add(new AiChatMessage(false, text, foods));
        adapter.notifyItemInserted(messageList.size() - 1);
        rvMessages.scrollToPosition(messageList.size() - 1);
    }

    private void setLoading(boolean loading) {
        progressLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSend.setEnabled(!loading);
        etInput.setEnabled(!loading);
        btnSend.setAlpha(loading ? 0.5f : 1.0f);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLastLocation();
                ToastUtils.success(this, "Đã bật GPS! Giờ hãy thử hỏi lại nhé.");
            } else {
                ToastUtils.info(this, "Bạn có thể chọn tòa nhà KTX thay vì GPS.");
            }
        }
    }

    @Override
    public void onAddToCart(FoodResponse food) {
        if (food == null || food.getId() == null) return;

        CartItemRequest request = new CartItemRequest(food.getId().toString(), 1, "");
        ApiClient.getApiService().addToCart(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    ToastUtils.success(AiRecommendationActivity.this, "Đã thêm " + food.getName() + " vào giỏ hàng!");
                } else {
                    ToastUtils.error(AiRecommendationActivity.this, "Không thể thêm vào giỏ hàng");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                ToastUtils.error(AiRecommendationActivity.this, "Lỗi mạng khi thêm vào giỏ hàng");
            }
        });
    }

    @Override
    public void onFoodClick(FoodResponse food) {
        if (food == null || food.getShopId() == null) return;

        Intent intent = new Intent(this, ShopDetailActivity.class);
        intent.putExtra("SHOP_ID", food.getShopId().toString());
        intent.putExtra("SHOP_NAME", food.getCategoryName() != null ? food.getCategoryName() : "Cửa hàng");
        startActivity(intent);
    }
}