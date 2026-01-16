package com.example;

public class Payment {
    private String id;
    private int amount;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    @Override
    public String toString() {
        return "Payment{id='" + id + "', amount=" + amount + "}";
    }
}
