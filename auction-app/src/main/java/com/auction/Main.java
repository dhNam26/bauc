package com.auction;

import com.auction.controller.AuctionController;
import com.auction.model.AuctionSession;
import com.auction.model.Product;
import com.auction.model.User;
import com.auction.observer.ConsoleNotificationListener;
import com.auction.repository.impl.InMemoryAuctionRepository;
import com.auction.repository.impl.InMemoryProductRepository;
import com.auction.repository.impl.InMemoryUserRepository;
import com.auction.service.AuctionService;
import com.auction.service.ProductService;
import com.auction.service.UserService;
import com.auction.view.ConsoleView;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws Exception {
        InMemoryUserRepository userRepo = new InMemoryUserRepository();
        InMemoryProductRepository productRepo = new InMemoryProductRepository();
        InMemoryAuctionRepository auctionRepo = new InMemoryAuctionRepository();

        UserService userService = new UserService(userRepo);
        ProductService productService = new ProductService(productRepo);
        AuctionService auctionService = new AuctionService(auctionRepo);
        auctionService.addListener(new ConsoleNotificationListener());

        AuctionController auctionController = new AuctionController(auctionService);
        ConsoleView view = new ConsoleView();

        User seller = userService.register("seller01", "seller@app.com");
        User alice = userService.register("alice", "alice@app.com");
        User bob = userService.register("bob", "bob@app.com");
        User charlie = userService.register("charlie", "charlie@app.com");

        Product laptop = productService.createProduct("Gaming Laptop", "RTX, 32GB RAM", seller);

        AuctionSession auction = auctionController.createAuction(
                laptop,
                500.0,
                10.0,
                LocalDateTime.now().minusSeconds(1),
                LocalDateTime.now().plusSeconds(12)
        );

        auctionController.enableAutoBid(auction.getId(), bob, 700.0, 10.0);
        auctionController.enableAutoBid(auction.getId(), charlie, 730.0, 15.0);

        view.showAuctions(auctionController.listAuctions());

        ExecutorService bidders = Executors.newFixedThreadPool(2);
        bidders.submit(() -> auctionController.placeBid(auction.getId(), alice, 520.0));
        bidders.submit(() -> auctionController.placeBid(auction.getId(), alice, 560.0));
        bidders.shutdown();
        bidders.awaitTermination(5, TimeUnit.SECONDS);

        Thread.sleep(1500);
        view.showAuctions(auctionController.listAuctions());
        view.showBidHistory(auctionController.bidHistory(auction.getId()));

        System.out.println("Waiting for auction to finish...");
        Thread.sleep(25000);
        view.showAuctions(auctionController.listAuctions());
        auctionService.shutdown();
    }
}
