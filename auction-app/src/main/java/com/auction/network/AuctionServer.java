package com.auction.network;

import com.auction.controller.AuctionController;
import com.auction.model.AuctionSession;
import com.auction.model.User;
import com.auction.service.UserService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.stream.Collectors;

public class AuctionServer implements Runnable {
    private final int port;
    private final AuctionController controller;
    private final UserService userService;

    public AuctionServer(int port, AuctionController controller, UserService userService) {
        this.port = port;
        this.controller = controller;
        this.userService = userService;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("AuctionServer listening on port " + port);
            while (!Thread.currentThread().isInterrupted()) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket, controller, userService)).start();
            }
        } catch (IOException e) {
            System.out.println("AuctionServer stopped: " + e.getMessage());
        }
    }

    public static String help() {
        return "Commands: LIST | BID <auctionId> <userId> <amount> | USERS";
    }

    public static String auctionsToText(Iterable<AuctionSession> auctions) {
        return java.util.stream.StreamSupport.stream(auctions.spliterator(), false)
                .map(a -> a.getId() + " | " + a.getProduct().getName() + " | price=" + a.getCurrentPrice())
                .collect(Collectors.joining("\n"));
    }

    public static String usersToText(Iterable<User> users) {
        return java.util.stream.StreamSupport.stream(users.spliterator(), false)
                .map(u -> u.getId() + " | " + u.getUsername())
                .collect(Collectors.joining("\n"));
    }
}
