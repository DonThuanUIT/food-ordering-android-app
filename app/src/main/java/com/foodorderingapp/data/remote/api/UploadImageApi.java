package com.foodorderingapp.data.remote.api;

import com.foodorderingapp.model.response.UploadImageResponse;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
public interface UploadImageApi {
    @Multipart
    @POST("upload/image")
    Call<UploadImageResponse> uploadImage(
            @Part MultipartBody.Part file
    );
}
