package com.foodorderingapp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.foodorderingapp.data.repository.OrderRepository;

public class OrderViewModel extends ViewModel {
    private final OrderRepository orderRepository = new OrderRepository();
    private final MutableLiveData<Boolean> checkoutResult = new MutableLiveData<>();

    public MutableLiveData<Boolean> getCheckoutResult() {
        return checkoutResult;
    }

    public void checkout(String building, String dropOff) {
        orderRepository.checkout(building, dropOff, checkoutResult);
    }
}
