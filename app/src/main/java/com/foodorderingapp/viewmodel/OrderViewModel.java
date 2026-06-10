package com.foodorderingapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.foodorderingapp.data.repository.OrderRepository;
import com.foodorderingapp.model.response.OrderResponse;

import java.util.List;

public class OrderViewModel extends ViewModel {
    private final OrderRepository orderRepository = new OrderRepository();
    private final MutableLiveData<Boolean> checkoutResult = new MutableLiveData<>();
    private final MutableLiveData<List<OrderResponse>> activeOrders = new MutableLiveData<>();

    public MutableLiveData<Boolean> getCheckoutResult() {
        return checkoutResult;
    }

    public LiveData<List<OrderResponse>> getActiveOrders() {
        return activeOrders;
    }

    public void checkout(String building, String dropOff) {
        orderRepository.checkout(building, dropOff, checkoutResult);
    }

    public void loadActiveOrders() {
        orderRepository.getActiveOrders(activeOrders);
    }
}
