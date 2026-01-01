package com.example.cafeflowapp;

public class MenuItem {
    private int id;
    private String name;
    private Double price;
    private Integer stock;

    public MenuItem(int id, String name, Double price, Integer stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public MenuItem(String name, Double price, Integer stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public Double getPrice() { return price; }
    public Integer getStock() { return stock; }

    public void setStock(Integer stock) { this.stock = stock; }
}