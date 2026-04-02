package com.auction.model;

public class AutoBidConfig {
    private final User user;
    private final double maxAmount;
    private final double increment;

    public AutoBidConfig(User user, double maxAmount, double increment) {
        this.user = user;
        this.maxAmount = maxAmount;
        this.increment = increment;
    }

    public User getUser() {
        return user;
    }

    public double getMaxAmount() {
        return maxAmount;
    }

    public double getIncrement() {
        return increment;
    }
}
