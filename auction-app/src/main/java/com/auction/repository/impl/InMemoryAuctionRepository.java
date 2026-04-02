package com.auction.repository.impl;

import com.auction.model.AuctionSession;
import com.auction.repository.AuctionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryAuctionRepository implements AuctionRepository {
    private final ConcurrentMap<String, AuctionSession> auctions = new ConcurrentHashMap<>();

    @Override
    public AuctionSession save(AuctionSession auctionSession) {
        auctions.put(auctionSession.getId(), auctionSession);
        return auctionSession;
    }

    @Override
    public Optional<AuctionSession> findById(String id) {
        return Optional.ofNullable(auctions.get(id));
    }

    @Override
    public List<AuctionSession> findAll() {
        return new ArrayList<>(auctions.values());
    }
}
