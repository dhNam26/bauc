package com.auction.network;

import java.io.*;
import java.net.Socket;

public class AuctionClient {
    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket("localhost", 9090);
             BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            System.out.println(in.readLine());
            String line;
            while ((line = input.readLine()) != null) {
                out.println(line);
                System.out.println(in.readLine());
            }
        }
    }
}
