package com.auction.model;

import java.time.LocalDateTime;

public class Bid {
    private final String id;
    private final String auctionId;
    private final User bidder;
    private final double amount;
    private final LocalDateTime timestamp;
    private final boolean autoBid;

    public Bid(String id, String auctionId, User bidder, double amount, LocalDateTime timestamp, boolean autoBid) {
        this.id = id;
        this.auctionId = auctionId;
        this.bidder = bidder;
        this.amount = amount;
        this.timestamp = timestamp;
        this.autoBid = autoBid;
    }

    public String getId() {
        return id;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public User getBidder() {
        return bidder;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isAutoBid() {
        return autoBid;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s bid %.2f%s", timestamp, bidder.getUsername(), amount, autoBid ? " (AUTO)" : "");
    }
}
