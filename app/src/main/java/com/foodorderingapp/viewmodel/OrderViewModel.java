package com.foodorderingapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.foodorderingapp.data.repository.OrderRepository;
import com.foodorderingapp.model.response.OrderResponse;

import java.util.List;

public class OrderViewModel extends ViewModel {
    private final OrderRepository repository = new OrderRepository();

    private final MutableLiveData<Boolean> checkoutResult = new MutableLiveData<>();
    private final MutableLiveData<List<OrderResponse>> historyOrders = new MutableLiveData<>();
    private final MutableLiveData<List<OrderResponse>> activeOrders = new MutableLiveData<>();

    public LiveData<Boolean> getCheckoutResult() { return checkoutResult; }
    public LiveData<List<OrderResponse>> getHistoryOrders() { return historyOrders; }
    public LiveData<List<OrderResponse>> getActiveOrders() { return activeOrders; }

    public void checkout(String building, String dropOff) {
        repository.checkout(building, dropOff, checkoutResult);
    }

    public void loadHistory() {
        repository.getHistory(historyOrders);
    }

    public void loadActiveOrders() {
        repository.getActiveOrders(activeOrders);
    }
}
