package com.foodorderingapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.foodorderingapp.data.repository.ShopRepository;
import com.foodorderingapp.model.response.PageResponse;
import com.foodorderingapp.model.response.ShopResponse;
import com.foodorderingapp.model.response.ShopDetailResponse;

public class ShopViewModel extends ViewModel {
    private final ShopRepository repository = new ShopRepository();
    private final MutableLiveData<PageResponse<ShopResponse>> shopData = new MutableLiveData<>();
    private final MutableLiveData<ShopDetailResponse> shopDetail = new MutableLiveData<>();
    public LiveData<PageResponse<ShopResponse>> getShopData() {
        return shopData;
    }

    public void loadShops(String keyword) {
        repository.getShops(0, 20, keyword, shopData);
    }

    public LiveData<ShopDetailResponse> getShopDetail() {
    return shopDetail;
}

    public void loadShopDetail(String shopId) {
        repository.getShopDetail(shopId, shopDetail);
    }
}