package com.auction.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

public class AuctionSession {
    private final String id;
    private final Product product;
    private final double startingPrice;
    private final double minimumIncrement;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    private final Duration antiSnipingThreshold;
    private final Duration antiSnipingExtension;
    private AuctionStatus status;
    private volatile double currentPrice;
    private volatile User highestBidder;
    private final List<Bid> bidHistory;
    private final ConcurrentMap<String, AutoBidConfig> autoBidConfigs;
    private final ReentrantLock lock;

    public AuctionSession(String id,
                          Product product,
                          double startingPrice,
                          double minimumIncrement,
                          LocalDateTime startTime,
                          LocalDateTime endTime,
                          Duration antiSnipingThreshold,
                          Duration antiSnipingExtension) {
        this.id = id;
        this.product = product;
        this.startingPrice = startingPrice;
        this.minimumIncrement = minimumIncrement;
        this.startTime = startTime;
        this.endTime = endTime;
        this.antiSnipingThreshold = antiSnipingThreshold;
        this.antiSnipingExtension = antiSnipingExtension;
        this.status = AuctionStatus.SCHEDULED;
        this.currentPrice = startingPrice;
        this.bidHistory = new ArrayList<>();
        this.autoBidConfigs = new ConcurrentHashMap<>();
        this.lock = new ReentrantLock(true);
    }

    public String getId() { return id; }
    public Product getProduct() { return product; }
    public double getStartingPrice() { return startingPrice; }
    public double getMinimumIncrement() { return minimumIncrement; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public Duration getAntiSnipingThreshold() { return antiSnipingThreshold; }
    public Duration getAntiSnipingExtension() { return antiSnipingExtension; }
    public AuctionStatus getStatus() { return status; }
    public double getCurrentPrice() { return currentPrice; }
    public User getHighestBidder() { return highestBidder; }
    public ReentrantLock getLock() { return lock; }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void recordBid(Bid bid) {
        bidHistory.add(bid);
        currentPrice = bid.getAmount();
        highestBidder = bid.getBidder();
    }

    public List<Bid> getBidHistory() {
        return List.copyOf(bidHistory);
    }

    public void registerAutoBid(AutoBidConfig config) {
        autoBidConfigs.put(config.getUser().getId(), config);
    }

    public void removeAutoBid(String userId) {
        autoBidConfigs.remove(userId);
    }

    public List<AutoBidConfig> getAutoBidConfigs() {
        return autoBidConfigs.values().stream()
                .sorted(Comparator.comparingDouble(AutoBidConfig::getMaxAmount).reversed())
                .toList();
    }

    public Optional<AutoBidConfig> getAutoBidForUser(String userId) {
        return Optional.ofNullable(autoBidConfigs.get(userId));
    }

    public boolean isLive(LocalDateTime now) {
        return (status == AuctionStatus.ACTIVE || status == AuctionStatus.SCHEDULED)
                && !now.isBefore(startTime)
                && now.isBefore(endTime);
    }
}
