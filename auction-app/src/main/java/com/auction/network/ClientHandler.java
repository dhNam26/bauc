package com.auction.network;

import com.auction.controller.AuctionController;
import com.auction.model.User;
import com.auction.service.UserService;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final AuctionController controller;
    private final UserService userService;

    public ClientHandler(Socket socket, AuctionController controller, UserService userService) {
        this.socket = socket;
        this.controller = controller;
        this.userService = userService;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println(AuctionServer.help());
            String line;
            while ((line = in.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 0) {
                    out.println("Empty command");
                    continue;
                }
                String command = parts[0].toUpperCase();
                try {
                    switch (command) {
                        case "LIST" -> out.println(AuctionServer.auctionsToText(controller.listAuctions()));
                        case "USERS" -> out.println(AuctionServer.usersToText(userService.listUsers()));
                        case "BID" -> {
                            if (parts.length < 4) {
                                out.println("Usage: BID <auctionId> <userId> <amount>");
                                continue;
                            }
                            User user = userService.getById(parts[2]);
                            controller.placeBid(parts[1], user, Double.parseDouble(parts[3]));
                            out.println("Bid accepted");
                        }
                        default -> out.println("Unknown command");
                    }
                } catch (Exception ex) {
                    out.println("ERROR: " + ex.getMessage());
                }
            }
        } catch (IOException ignored) {
        }
    }
}
