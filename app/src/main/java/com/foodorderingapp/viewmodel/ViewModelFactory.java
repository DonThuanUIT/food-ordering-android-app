package com.foodorderingapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.data.repository.UploadImageRepository;

public class ViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;

    public ViewModelFactory(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(UploadImageViewModel.class)) {

            UploadImageRepository repository = new UploadImageRepository(ApiClient.getApiService());

            return (T) new UploadImageViewModel(application, repository);
        }

        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}