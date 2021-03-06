package com.erik.bookManagement;

import java.math.BigDecimal;

public class BookUpdate {

    private final String product;
    private final String exchange;
    private Side side;
    private BigDecimal price;
    private BigDecimal quantity;

    public BookUpdate(String product, String exchange) {
        this.product = product;
        this.exchange = exchange;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setQuantity(BigDecimal quantity) {
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

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getQuantity() {
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
