package com.foodorderingapp.data.repository;

import com.foodorderingapp.data.remote.api.ApiService;
import com.foodorderingapp.model.response.UploadImageResponse;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

public class UploadImageRepository {
    private final ApiService apiService;

    // Yêu cầu truyền ApiService (đã có Token Interceptor) vào đây
    public UploadImageRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public Call<UploadImageResponse> uploadImage(File file) {
        // Tạo RequestBody từ File
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);

        // Tạo MultipartBody.Part ghép nối với key "file" (Phải trùng với @RequestParam bên Spring Boot)
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        // Gọi API thông qua ApiService chuẩn
        return apiService.uploadImage(body);
    }
}