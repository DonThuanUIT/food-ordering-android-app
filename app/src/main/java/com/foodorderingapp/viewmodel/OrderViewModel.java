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
    private final MutableLiveData<List<OrderResponse>> orderHistory = new MutableLiveData<>();
    private final MutableLiveData<Boolean> reviewResult = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public LiveData<Boolean> getCheckoutResult() {
        return checkoutResult;
    }

    public LiveData<List<OrderResponse>> getActiveOrders() {
        return activeOrders;
    }

    public LiveData<List<OrderResponse>> getOrderHistory() {
        return orderHistory;
    }

    public LiveData<Boolean> getReviewResult() {
        return reviewResult;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public void checkout(String building, String dropOff) {
        orderRepository.checkout(building, dropOff, checkoutResult);
    }

    public void loadActiveOrders() {
        orderRepository.getActiveOrders(activeOrders, message);
    }

    public void loadOrderHistory() {
        orderRepository.getOrderHistory(orderHistory, message);
    }

    public void createReview(String orderId, int rating, String comment) {
        orderRepository.createReview(orderId, rating, comment, reviewResult, message);
    }
}
