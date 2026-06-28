package com.foodorderingapp.model.response;

import java.util.List;

public class SpendingSummaryResponse {
    private double totalSpent;
    private List<SpendingBreakdown> breakdown;

    public double getTotalSpent() {
        return totalSpent;
    }

    public List<SpendingBreakdown> getBreakdown() {
        return breakdown;
    }

    public static class SpendingBreakdown {
        private String period;
        private double total;

        public String getPeriod() {
            return period;
        }

        public void setPeriod(String period) {
            this.period = period;
        }

        public double getTotal() {
            return total;
        }

        public void setTotal(double total) {
            this.total = total;
        }
    }
}
