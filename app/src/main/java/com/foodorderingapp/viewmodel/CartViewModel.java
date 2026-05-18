package com.foodorderingapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.foodorderingapp.data.repository.CartRepository;
import com.foodorderingapp.model.response.CartResponse;

public class CartViewModel extends ViewModel {
    private final CartRepository repository = new CartRepository();

    private final MutableLiveData<Boolean> addResult = new MutableLiveData<>();
    private final MutableLiveData<CartResponse> cartData = new MutableLiveData<>();

    public LiveData<Boolean> getAddResult() {
        return addResult;
    }

    public LiveData<CartResponse> getCartData() {
        return cartData;
    }

    public void addToCart(String foodId) {
        repository.addToCart(foodId, 1, "", addResult);
    }

    public void loadCart() {
        repository.getCart(cartData);
    }
}
