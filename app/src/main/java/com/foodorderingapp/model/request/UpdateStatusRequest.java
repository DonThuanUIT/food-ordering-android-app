package com.foodorderingapp.model.request;

public class UpdateStatusRequest {
    private String status;
    private String cancelReason;

    public UpdateStatusRequest() {
    }

    public UpdateStatusRequest(String status, String cancelReason) {
        this.status = status;
        this.cancelReason = cancelReason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }
}
