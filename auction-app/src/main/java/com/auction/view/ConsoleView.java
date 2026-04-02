package com.auction.view;

import com.auction.model.AuctionSession;
import com.auction.model.Bid;
import com.auction.util.PriceCurvePrinter;

import java.util.List;

public class ConsoleView {
    public void showAuctions(List<AuctionSession> auctions) {
        System.out.println("\n==== AUCTIONS ====");
        for (AuctionSession auction : auctions) {
            String leader = auction.getHighestBidder() == null ? "-" : auction.getHighestBidder().getUsername();
            System.out.printf("ID=%s | product=%s | price=%.2f | leader=%s | status=%s | ends=%s%n",
                    auction.getId(),
                    auction.getProduct().getName(),
                    auction.getCurrentPrice(),
                    leader,
                    auction.getStatus(),
                    auction.getEndTime());
        }
    }

    public void showBidHistory(List<Bid> bids) {
        System.out.println("\n==== BID HISTORY ====");
        bids.forEach(System.out::println);
        System.out.println();
        System.out.println(PriceCurvePrinter.toAsciiChart(bids));
    }
}
