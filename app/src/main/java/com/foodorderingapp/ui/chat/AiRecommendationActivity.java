package com.foodorderingapp.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AiRecommendationActivity extends AppCompatActivity implements AiMessageAdapter.OnRecommendationClickListener {

    public static final String EXTRA_SHOP_ID = "SHOP_ID";

    private RecyclerView rvMessages;
    private AiMessageAdapter adapter;
    private final List<AiChatMessage> messageList = new ArrayList<>();
    private EditText etInput;
    private ImageButton btnSend;
    private ProgressBar progressLoading;
    private View layoutIntro;
    private String shopId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_recommendation);

        shopId = getIntent().getStringExtra(EXTRA_SHOP_ID);

        bindViews();
        setupChat();
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

        UUID shopUuid = null;
        if (shopId != null && !shopId.trim().isEmpty()) {
            try {
                shopUuid = UUID.fromString(shopId);
            } catch (IllegalArgumentException e) {
                // Ignore invalid UUID
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
                        addAiMessage("Xin lỗi, tôi không tìm thấy món ăn nào phù hợp với yêu cầu của bạn. Bạn hãy thử diễn đạt khác đi hoặc thử từ khóa khác nhé!", null);
                    } else {
                        // Construct the AI response content
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
