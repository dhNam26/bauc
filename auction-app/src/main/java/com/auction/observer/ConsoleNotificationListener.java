package com.auction.observer;

import com.auction.model.AuctionSession;
import com.auction.model.Bid;

public class ConsoleNotificationListener implements AuctionEventListener {
    @Override
    public void onAuctionStarted(AuctionSession auction) {
        System.out.println("[EVENT] Auction started: " + auction.getProduct().getName());
    }

    @Override
    public void onNewHighestBid(AuctionSession auction, Bid bid) {
        System.out.printf("[EVENT] %s | highest bid = %.2f by %s%n",
                auction.getProduct().getName(), bid.getAmount(), bid.getBidder().getUsername());
    }

    @Override
    public void onAuctionExtended(AuctionSession auction) {
        System.out.println("[EVENT] Auction extended until " + auction.getEndTime());
    }

    @Override
    public void onAuctionEnded(AuctionSession auction) {
        String winner = auction.getHighestBidder() == null ? "No winner" : auction.getHighestBidder().getUsername();
        System.out.printf("[EVENT] Auction ended: %s | winner=%s | final price=%.2f%n",
                auction.getProduct().getName(), winner, auction.getCurrentPrice());
    }

    @Override
    public void onError(String message) {
        System.out.println("[ERROR] " + message);
    }
}
