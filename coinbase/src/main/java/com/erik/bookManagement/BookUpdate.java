package com.erik.bookManagement;

import com.erik.coinbase.Side;

public class BookUpdate {

    private final String product;
    private final String exchange;
    private Side side;
    private String price;
    private String quantity;

    public BookUpdate(String product, String exchange) {
        this.product = product;
        this.exchange = exchange;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getProduct() {
        return product;
    }

    public String getExchange() {
        return exchange;
    }

    public Side getSide() {
        return side;
    }

    public String getPrice() {
        return price;
    }

    public String getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return "BookUpdate{" +
                "product='" + product + '\'' +
                ", exchange='" + exchange + '\'' +
                ", side=" + side +
                ", price='" + price + '\'' +
                ", quantity='" + quantity + '\'' +
                '}';
    }
}
