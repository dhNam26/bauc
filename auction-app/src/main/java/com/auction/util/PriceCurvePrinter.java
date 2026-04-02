package com.auction.util;

import com.auction.model.Bid;

import java.util.List;

public final class PriceCurvePrinter {
    private PriceCurvePrinter() {}

    public static String toAsciiChart(List<Bid> bids) {
        if (bids.isEmpty()) {
            return "No bids yet.";
        }
        double max = bids.stream().mapToDouble(Bid::getAmount).max().orElse(1);
        StringBuilder sb = new StringBuilder();
        sb.append("Bid History Visualization\n");
        for (int i = 0; i < bids.size(); i++) {
            Bid bid = bids.get(i);
            int barLength = (int) Math.max(1, Math.round((bid.getAmount() / max) * 30));
            sb.append(String.format("%02d | %-12s | %8.2f | %s%n",
                    i + 1,
                    bid.getBidder().getUsername(),
                    bid.getAmount(),
                    "#".repeat(barLength)));
        }
        return sb.toString();
    }
}
