package com.foodorderingapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.foodorderingapp.data.repository.AdminRepository;
import com.foodorderingapp.model.response.AdminOverviewResponse;
import com.foodorderingapp.model.response.AdminUserResponse;
import com.foodorderingapp.model.response.PageResponse;
import com.foodorderingapp.model.response.ShopResponse;

public class AdminViewModel extends ViewModel {

    private final AdminRepository repository = new AdminRepository();
    private final MutableLiveData<AdminOverviewResponse> overview = new MutableLiveData<>();
    private final MutableLiveData<PageResponse<ShopResponse>> pendingShops = new MutableLiveData<>();
    private final MutableLiveData<PageResponse<AdminUserResponse>> users = new MutableLiveData<>();
    private final MutableLiveData<Boolean> shopStatusResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> userLockResult = new MutableLiveData<>();

    public LiveData<AdminOverviewResponse> getOverview() {
        return overview;
    }

    public LiveData<PageResponse<ShopResponse>> getPendingShops() {
        return pendingShops;
    }

    public LiveData<PageResponse<AdminUserResponse>> getUsers() {
        return users;
    }

    public LiveData<Boolean> getShopStatusResult() {
        return shopStatusResult;
    }

    public LiveData<Boolean> getUserLockResult() {
        return userLockResult;
    }

    public void loadOverview() {
        repository.getOverview(overview);
    }

    public void loadPendingShops() {
        repository.getPendingShops(pendingShops);
    }

    public void loadShops(String status) {
        repository.getShopsByStatus(status, pendingShops);
    }

    public void loadShops(String status, int page, int size) {
        repository.getShopsByStatus(status, page, size, pendingShops);
    }

    public void updateShopStatus(String shopId, String status) {
        repository.updateShopStatus(shopId, status, shopStatusResult);
    }

    public void loadUsers(String search) {
        repository.getUsers(search, users);
    }

    public void loadUsers(String search, String role) {
        repository.getUsers(search, role, users);
    }

    public void loadUsers(String search, String role, int page, int size) {
        repository.getUsers(search, role, page, size, users);
    }

    public void updateUserLock(String userId, boolean locked) {
        repository.updateUserLock(userId, locked, userLockResult);
    }

    public void clearShopStatusResult() {
        shopStatusResult.setValue(null);
    }

    public void clearUserLockResult() {
        userLockResult.setValue(null);
    }
}
