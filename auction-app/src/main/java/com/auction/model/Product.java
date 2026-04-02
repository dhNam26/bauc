package com.auction.model;

public class Product {
    private final String id;
    private final String name;
    private final String description;
    private final User seller;

    public Product(String id, String name, String description, User seller) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.seller = seller;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public User getSeller() {
        return seller;
    }

    @Override
    public String toString() {
        return name + " | seller=" + seller.getUsername();
    }
}
