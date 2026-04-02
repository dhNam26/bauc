package com.auction.repository;

import com.auction.model.AuctionSession;

import java.util.List;
import java.util.Optional;

public interface AuctionRepository {
    AuctionSession save(AuctionSession auctionSession);
    Optional<AuctionSession> findById(String id);
    List<AuctionSession> findAll();
}
