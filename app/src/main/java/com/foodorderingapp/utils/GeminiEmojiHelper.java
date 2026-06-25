package com.foodorderingapp.utils;

import android.os.Handler;
import android.os.Looper;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiEmojiHelper {

    private static final String API_KEY = com.foodorderingapp.BuildConfig.GEMINI_API_KEY;
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface EmojiCallback {
        void onSuccess(String emoji);
        void onFailure(Exception e);
    }

    public static void generateEmojiForCategory(String categoryName, EmojiCallback callback) {
        String prompt = "Bạn là trợ lý AI phân loại thực phẩm. Hãy trả về DUY NHẤT từ 1 đến 2 biểu tượng cảm xúc (emoji) đại diện tốt nhất cho danh mục đồ ăn hoặc đồ uống sau đây. Tuyệt đối không được viết thêm bất kỳ chữ giải thích, dấu câu hay ký tự nào khác ngoài emoji.\n"
                + "Danh mục: " + categoryName;

        GeminiRequest requestPayload = new GeminiRequest(prompt);
        String jsonPayload = gson.toJson(requestPayload);

        RequestBody body = RequestBody.create(
                jsonPayload,
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(GEMINI_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onFailure(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    mainHandler.post(() -> callback.onFailure(new Exception("API error code: " + response.code())));
                    return;
                }

                String responseBody = response.body().string();
                try {
                    GeminiResponse geminiResponse = gson.fromJson(responseBody, GeminiResponse.class);
                    if (geminiResponse != null && geminiResponse.candidates != null && !geminiResponse.candidates.isEmpty()) {
                        Candidate candidate = geminiResponse.candidates.get(0);
                        if (candidate.content != null && candidate.content.parts != null && !candidate.content.parts.isEmpty()) {
                            String emojiResult = candidate.content.parts.get(0).text.trim();
                            // Loại bỏ khoảng trắng hoặc xuống dòng ngẫu nhiên từ LLM
                            emojiResult = emojiResult.replaceAll("\\s+", "");
                            String finalEmoji = emojiResult;
                            mainHandler.post(() -> callback.onSuccess(finalEmoji));
                            return;
                        }
                    }
                    mainHandler.post(() -> callback.onFailure(new Exception("Empty emoji response")));
                } catch (Exception e) {
                    mainHandler.post(() -> callback.onFailure(e));
                }
            }
        });
    }

    // --- JSON Model POJOs for Gemini request ---
    private static class GeminiRequest {
        List<ContentRequest> contents;

        GeminiRequest(String promptText) {
            this.contents = new ArrayList<>();
            this.contents.add(new ContentRequest(promptText));
        }
    }

    private static class ContentRequest {
        List<PartRequest> parts;

        ContentRequest(String text) {
            this.parts = new ArrayList<>();
            this.parts.add(new PartRequest(text));
        }
    }

    private static class PartRequest {
        String text;

        PartRequest(String text) {
            this.text = text;
        }
    }

    // --- JSON Model POJOs for Gemini response ---
    private static class GeminiResponse {
        List<Candidate> candidates;
    }

    private static class Candidate {
        ContentResponse content;
    }

    private static class ContentResponse {
        List<PartResponse> parts;
    }

    private static class PartResponse {
        String text;
    }
}
