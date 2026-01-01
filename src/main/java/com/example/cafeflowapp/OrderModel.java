package com.example.cafeflowapp;

public class OrderModel {
    private String orderId;
    private String customer; // This maps to customer_name in DB
    private String email;
    private String items;
    private Double total;
    private String status;

    public OrderModel(String orderId, String customer, String email, String items, Double total, String status) {
        this.orderId = orderId;
        this.customer = customer;
        this.email = email;
        this.items = items;
        this.total = total;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public String getCustomer() { return customer; }
    public String getEmail() { return email; }
    public String getItems() { return items; }
    public Double getTotal() { return total; }
    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }
}