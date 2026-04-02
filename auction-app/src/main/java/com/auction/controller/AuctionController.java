package com.auction.controller;

import com.auction.model.AuctionSession;
import com.auction.model.Bid;
import com.auction.model.Product;
import com.auction.model.User;
import com.auction.service.AuctionService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class AuctionController {
    private final AuctionService auctionService;

    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    public AuctionSession createAuction(Product product,
                                        double startingPrice,
                                        double minimumIncrement,
                                        LocalDateTime start,
                                        LocalDateTime end) {
        return auctionService.createAuction(
                product,
                startingPrice,
                minimumIncrement,
                start,
                end,
                Duration.ofSeconds(15),
                Duration.ofSeconds(20)
        );
    }

    public Bid placeBid(String auctionId, User bidder, double amount) {
        return auctionService.placeBid(auctionId, bidder, amount);
    }

    public void enableAutoBid(String auctionId, User bidder, double maxAmount, double increment) {
        auctionService.registerAutoBid(auctionId, bidder, maxAmount, increment);
    }

    public List<AuctionSession> listAuctions() {
        return auctionService.listAuctions();
    }

    public List<Bid> bidHistory(String auctionId) {
        return auctionService.getBidHistory(auctionId);
    }
}
