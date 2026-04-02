package com.auction.service;

import com.auction.model.*;
import com.auction.observer.AuctionEventListener;
import com.auction.repository.AuctionRepository;
import com.auction.util.IdGenerator;
import com.auction.util.ValidationUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionService {
    private final AuctionRepository auctionRepository;
    private final List<AuctionEventListener> listeners = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public AuctionService(AuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
        scheduler.scheduleAtFixedRate(this::closeExpiredAuctions, 1, 1, TimeUnit.SECONDS);
    }

    public void addListener(AuctionEventListener listener) {
        listeners.add(listener);
    }

    public AuctionSession createAuction(Product product,
                                        double startingPrice,
                                        double minimumIncrement,
                                        LocalDateTime startTime,
                                        LocalDateTime endTime,
                                        Duration antiSnipingThreshold,
                                        Duration antiSnipingExtension) {
        ValidationUtil.require(product != null, "Product is required");
        ValidationUtil.require(startingPrice > 0, "Starting price must be > 0");
        ValidationUtil.require(minimumIncrement > 0, "Minimum increment must be > 0");
        ValidationUtil.require(endTime.isAfter(startTime), "End time must be after start time");

        AuctionSession auction = new AuctionSession(
                IdGenerator.newId(),
                product,
                startingPrice,
                minimumIncrement,
                startTime,
                endTime,
                antiSnipingThreshold,
                antiSnipingExtension
        );
        auctionRepository.save(auction);
        activateIfReady(auction);
        return auction;
    }

    public void registerAutoBid(String auctionId, User user, double maxAmount, double increment) {
        AuctionSession auction = getAuction(auctionId);
        ValidationUtil.require(maxAmount >= auction.getCurrentPrice(), "Max amount must be >= current price");
        ValidationUtil.require(increment >= auction.getMinimumIncrement(), "Auto-bid increment too small");
        auction.registerAutoBid(new AutoBidConfig(user, maxAmount, increment));
    }

    public Bid placeBid(String auctionId, User bidder, double amount) {
        AuctionSession auction = getAuction(auctionId);
        activateIfReady(auction);
        auction.getLock().lock();
        try {
            LocalDateTime now = LocalDateTime.now();
            validateBid(auction, bidder, amount, now);
            Bid manualBid = createAndRecordBid(auction, bidder, amount, false, now);
            maybeExtendAuction(auction, now);
            resolveAutoBids(auction, bidder, now);
            return manualBid;
        } catch (RuntimeException e) {
            listeners.forEach(l -> l.onError(e.getMessage()));
            throw e;
        } finally {
            auction.getLock().unlock();
        }
    }

    private void validateBid(AuctionSession auction, User bidder, double amount, LocalDateTime now) {
        ValidationUtil.require(bidder != null, "Bidder is required");
        ValidationUtil.require(auction.isLive(now), "Auction is not live");
        double minValid = auction.getHighestBidder() == null
                ? auction.getStartingPrice()
                : auction.getCurrentPrice() + auction.getMinimumIncrement();
        ValidationUtil.require(amount >= minValid,
                String.format("Bid must be at least %.2f", minValid));
        ValidationUtil.require(!auction.getProduct().getSeller().getId().equals(bidder.getId()),
                "Seller cannot bid on their own auction");
    }

    private Bid createAndRecordBid(AuctionSession auction, User bidder, double amount, boolean autoBid, LocalDateTime now) {
        Bid bid = new Bid(IdGenerator.newId(), auction.getId(), bidder, amount, now, autoBid);
        auction.recordBid(bid);
        auctionRepository.save(auction);
        listeners.forEach(l -> l.onNewHighestBid(auction, bid));
        return bid;
    }

    private void resolveAutoBids(AuctionSession auction, User triggeringBidder, LocalDateTime now) {
        boolean changed;
        do {
            changed = false;
            User currentLeader = auction.getHighestBidder();
            double current = auction.getCurrentPrice();
            for (AutoBidConfig config : auction.getAutoBidConfigs()) {
                if (config.getUser().equals(currentLeader)) {
                    continue;
                }
                double nextAmount = current + Math.max(config.getIncrement(), auction.getMinimumIncrement());
                if (nextAmount <= config.getMaxAmount()) {
                    createAndRecordBid(auction, config.getUser(), nextAmount, true, now.plusNanos(1));
                    current = auction.getCurrentPrice();
                    currentLeader = auction.getHighestBidder();
                    changed = true;
                }
            }
        } while (changed);
        maybeLimitWinnerToSecondPrice(auction);
        maybeExtendAuction(auction, now);
        if (triggeringBidder != null && auction.getHighestBidder() != null && auction.getHighestBidder().equals(triggeringBidder)) {
            // no-op but keeps intent clear
        }
    }

    private void maybeLimitWinnerToSecondPrice(AuctionSession auction) {
        List<AutoBidConfig> configs = auction.getAutoBidConfigs();
        if (configs.size() < 2 || auction.getHighestBidder() == null) {
            return;
        }
        AutoBidConfig winnerConfig = configs.stream()
                .filter(c -> c.getUser().equals(auction.getHighestBidder()))
                .findFirst().orElse(null);
        AutoBidConfig second = configs.stream()
                .filter(c -> !c.getUser().equals(auction.getHighestBidder()))
                .findFirst().orElse(null);
        if (winnerConfig == null || second == null) {
            return;
        }
        double fairPrice = Math.min(winnerConfig.getMaxAmount(), second.getMaxAmount() + auction.getMinimumIncrement());
        if (fairPrice < auction.getCurrentPrice() && fairPrice >= auction.getStartingPrice()) {
            Bid adjusted = new Bid(IdGenerator.newId(), auction.getId(), winnerConfig.getUser(), fairPrice, LocalDateTime.now(), true);
            auction.recordBid(adjusted);
            listeners.forEach(l -> l.onNewHighestBid(auction, adjusted));
        }
    }

    private void maybeExtendAuction(AuctionSession auction, LocalDateTime now) {
        Duration remaining = Duration.between(now, auction.getEndTime());
        if (!remaining.isNegative() && remaining.compareTo(auction.getAntiSnipingThreshold()) <= 0) {
            auction.setEndTime(auction.getEndTime().plus(auction.getAntiSnipingExtension()));
            listeners.forEach(l -> l.onAuctionExtended(auction));
        }
    }

    private void activateIfReady(AuctionSession auction) {
        if (auction.getStatus() == AuctionStatus.SCHEDULED && !LocalDateTime.now().isBefore(auction.getStartTime())) {
            auction.setStatus(AuctionStatus.ACTIVE);
            listeners.forEach(l -> l.onAuctionStarted(auction));
        }
    }

    public AuctionSession getAuction(String auctionId) {
        return auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found: " + auctionId));
    }

    public List<AuctionSession> listAuctions() {
        auctionRepository.findAll().forEach(this::activateIfReady);
        return auctionRepository.findAll();
    }

    public List<Bid> getBidHistory(String auctionId) {
        return getAuction(auctionId).getBidHistory();
    }

    public void closeExpiredAuctions() {
        LocalDateTime now = LocalDateTime.now();
        for (AuctionSession auction : auctionRepository.findAll()) {
            activateIfReady(auction);
            if (auction.getStatus() == AuctionStatus.ACTIVE && !now.isBefore(auction.getEndTime())) {
                auction.getLock().lock();
                try {
                    if (auction.getStatus() == AuctionStatus.ACTIVE && !LocalDateTime.now().isBefore(auction.getEndTime())) {
                        auction.setStatus(AuctionStatus.ENDED);
                        auctionRepository.save(auction);
                        listeners.forEach(l -> l.onAuctionEnded(auction));
                    }
                } finally {
                    auction.getLock().unlock();
                }
            }
        }
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}
