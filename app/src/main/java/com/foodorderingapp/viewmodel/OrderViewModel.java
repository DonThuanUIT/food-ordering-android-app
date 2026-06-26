package com.foodorderingapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.foodorderingapp.data.repository.OrderRepository;
import com.foodorderingapp.model.response.BuildingResponse;
import com.foodorderingapp.model.response.DropOffPointResponse;
import com.foodorderingapp.model.response.OrderResponse;
import com.foodorderingapp.model.response.VoucherResponse;

import java.util.List;

public class OrderViewModel extends ViewModel {
    private final OrderRepository orderRepository = new OrderRepository();
    private final MutableLiveData<Boolean> checkoutResult = new MutableLiveData<>();
    private final MutableLiveData<List<OrderResponse>> activeOrders = new MutableLiveData<>();
    private final MutableLiveData<List<OrderResponse>> orderHistory = new MutableLiveData<>();
    private final MutableLiveData<List<BuildingResponse>> buildings = new MutableLiveData<>();
    private final MutableLiveData<List<DropOffPointResponse>> dropOffPoints = new MutableLiveData<>();
    private final MutableLiveData<List<VoucherResponse>> vouchers = new MutableLiveData<>();
    private final MutableLiveData<Boolean> reviewResult = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    private final MutableLiveData<List<OrderResponse>> shipperAvailableOrders = new MutableLiveData<>();
    private final MutableLiveData<List<OrderResponse>> shipperActiveOrders = new MutableLiveData<>();
    private final MutableLiveData<List<OrderResponse>> shipperOrderHistory = new MutableLiveData<>();
    private final MutableLiveData<Boolean> claimResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> locationUpdateResult = new MutableLiveData<>();

    public LiveData<List<OrderResponse>> getShipperAvailableOrders() {
        return shipperAvailableOrders;
    }

    public LiveData<List<OrderResponse>> getShipperActiveOrders() {
        return shipperActiveOrders;
    }

    public LiveData<List<OrderResponse>> getShipperOrderHistory() {
        return shipperOrderHistory;
    }

    public LiveData<Boolean> getClaimResult() {
        return claimResult;
    }

    public LiveData<Boolean> getLocationUpdateResult() {
        return locationUpdateResult;
    }

    public LiveData<Boolean> getCheckoutResult() {
        return checkoutResult;
    }

    public LiveData<List<OrderResponse>> getActiveOrders() {
        return activeOrders;
    }

    public LiveData<List<OrderResponse>> getOrderHistory() {
        return orderHistory;
    }

    public LiveData<List<BuildingResponse>> getBuildings() {
        return buildings;
    }

    public LiveData<List<DropOffPointResponse>> getDropOffPoints() {
        return dropOffPoints;
    }

    public LiveData<List<VoucherResponse>> getVouchers() {
        return vouchers;
    }

    public LiveData<Boolean> getReviewResult() {
        return reviewResult;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public void checkout(String shopId, List<String> cartItemIds, String buildingId,
                         String dropOffPointId, String voucherCode) {
        orderRepository.checkout(shopId, cartItemIds, buildingId, dropOffPointId,
                voucherCode, checkoutResult, message);
    }

    public void loadBuildings() {
        orderRepository.getBuildings(buildings, message);
    }

    public void loadDropOffPoints(String buildingId) {
        orderRepository.getDropOffPoints(buildingId, dropOffPoints, message);
    }

    public void loadVouchers(String shopId) {
        orderRepository.getActiveVouchers(shopId, vouchers, message);
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

    public void loadShipperAvailableOrders() {
        orderRepository.getAvailableOrdersForDelivery(shipperAvailableOrders, message);
    }

    public void claimOrder(String orderId) {
        orderRepository.claimOrder(orderId, claimResult, message);
    }

    public void loadShipperActiveOrders() {
        orderRepository.getShipperActiveOrders(shipperActiveOrders, message);
    }

    public void loadShipperOrderHistory() {
        orderRepository.getShipperOrderHistory(shipperOrderHistory, message);
    }

    public void updateShipperLocation(String orderId, Double latitude, Double longitude) {
        orderRepository.updateShipperLocation(orderId, latitude, longitude, locationUpdateResult);
    }
}
