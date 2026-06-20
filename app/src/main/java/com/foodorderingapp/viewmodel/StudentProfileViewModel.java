package com.foodorderingapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.foodorderingapp.data.repository.StudentRepository;
import com.foodorderingapp.model.request.UpdateProfileRequest;
import com.foodorderingapp.model.response.BuildingResponse;
import com.foodorderingapp.model.response.SpendingSummaryResponse;
import com.foodorderingapp.model.response.UserProfileResponse;

import java.util.List;

public class StudentProfileViewModel extends ViewModel {
    private final StudentRepository repository = new StudentRepository();
    private final MutableLiveData<UserProfileResponse> profile = new MutableLiveData<>();
    private final MutableLiveData<SpendingSummaryResponse> spendingSummary = new MutableLiveData<>();
    private final MutableLiveData<List<BuildingResponse>> buildings = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateResult = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public LiveData<UserProfileResponse> getProfile() {
        return profile;
    }

    public LiveData<SpendingSummaryResponse> getSpendingSummary() {
        return spendingSummary;
    }

    public LiveData<List<BuildingResponse>> getBuildings() {
        return buildings;
    }

    public LiveData<Boolean> getUpdateResult() {
        return updateResult;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public void loadProfile() {
        repository.getMyProfile(profile, message);
    }

    public void loadSpendingSummary() {
        repository.getSpendingSummary(spendingSummary, message);
    }

    public void loadBuildings() {
        repository.getBuildings(buildings, message);
    }

    public void updateProfile(UpdateProfileRequest request) {
        repository.updateMyProfile(request, profile, updateResult, message);
    }

    public void clearUpdateResult() {
        updateResult.setValue(null);
    }
}
