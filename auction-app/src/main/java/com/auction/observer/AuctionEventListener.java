package com.auction.observer;

import com.auction.model.AuctionSession;
import com.auction.model.Bid;

public interface AuctionEventListener {
    void onAuctionStarted(AuctionSession auction);
    void onNewHighestBid(AuctionSession auction, Bid bid);
    void onAuctionExtended(AuctionSession auction);
    void onAuctionEnded(AuctionSession auction);
    void onError(String message);
}
