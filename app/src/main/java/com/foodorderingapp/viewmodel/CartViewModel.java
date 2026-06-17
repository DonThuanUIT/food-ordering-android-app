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
    private final MutableLiveData<Boolean> updateQuantityResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> deleteItemResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> clearShopResult = new MutableLiveData<>();

    public LiveData<Boolean> getAddResult() {
        return addResult;
    }

    public LiveData<CartResponse> getCartData() {
        return cartData;
    }

    public LiveData<Boolean> getUpdateQuantityResult() {
        return updateQuantityResult;
    }

    public LiveData<Boolean> getDeleteItemResult() {
        return deleteItemResult;
    }

    public LiveData<Boolean> getClearShopResult() {
        return clearShopResult;
    }

    public void addToCart(String foodId) {
        repository.addToCart(foodId, 1, "", addResult);
    }

    public void loadCart() {
        repository.getCart(cartData);
    }

    public void updateCartItemQuantity(String cartItemId, int quantity) {
        repository.updateCartItemQuantity(cartItemId, quantity, updateQuantityResult);
    }

    public void deleteCartItem(String cartItemId) {
        repository.deleteCartItem(cartItemId, deleteItemResult);
    }

    public void clearShopCart(String shopId) {
        repository.clearShopCart(shopId, clearShopResult);
    }
}
