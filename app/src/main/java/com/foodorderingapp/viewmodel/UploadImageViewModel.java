package com.foodorderingapp.viewmodel;

import android.app.Application;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.foodorderingapp.data.respository.UploadImageRepository;
import com.foodorderingapp.model.response.UploadImageResponse;
import com.foodorderingapp.utils.FileUtils;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class UploadImageViewModel extends AndroidViewModel{
    private final UploadImageRepository uploadRepository;
    private final ExecutorService executorService;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> uploadSuccessUrl = new MutableLiveData<>();
    private final MutableLiveData<String> uploadError = new MutableLiveData<>();

    public UploadImageViewModel(@NonNull Application application, UploadImageRepository uploadRepository) {
        super(application);
        this.uploadRepository = uploadRepository;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getUploadSuccessUrl() { return uploadSuccessUrl; }
    public LiveData<String> getUploadError() { return uploadError; }


    public void uploadImage(Uri uri) {
        isLoading.setValue(true);

        executorService.execute(() -> {
            try {
                Context context = getApplication().getApplicationContext();
                File file = FileUtils.getFileFromUri(context, uri);

                uploadRepository.uploadImage(file).enqueue(new Callback<UploadImageResponse>() {

                    @Override
                    public void onResponse(Call<UploadImageResponse> call, Response<UploadImageResponse> response) {
                        isLoading.postValue(false);

                        if (response.isSuccessful() && response.body() != null) {
                            uploadSuccessUrl.postValue(response.body().getUrl());
                        } else {
                            uploadError.postValue("server error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<UploadImageResponse> call, Throwable t) {
                        isLoading.postValue(false);
                        uploadError.postValue("Network error: Unable to connect to server!");
                    }
                });
            } catch (Exception e) {
                isLoading.postValue(false);
                uploadError.postValue("File processing error: " + e.getMessage());
            }
        });
    }
}
