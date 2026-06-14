package com.foodorderingapp.data.repository;

import com.foodorderingapp.data.remote.api.UploadImageApi;
import com.foodorderingapp.model.response.UploadImageResponse;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
public class UploadImageRepository {
    private final UploadImageApi uploadImageApi;

    public UploadImageRepository(UploadImageApi uploadImageApi) {
        this.uploadImageApi = uploadImageApi;
    }

    public Call<UploadImageResponse> uploadImage(File file) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);

        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        return uploadImageApi.uploadImage(body);
    }
}
