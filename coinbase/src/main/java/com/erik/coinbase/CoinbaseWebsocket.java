package com.erik.coinbase;

import com.erik.bookManagement.Book;
import com.erik.bookManagement.BookUpdate;
import com.erik.bookManagement.Side;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class CoinbaseWebsocket extends WebSocketClient {
    private static final Logger logger=LogManager.getLogger(CoinbaseWebsocket.class);
    private final Book book;

    private final String product;
    private JsonParser parser=new JsonParser();
    public static final String EXCHANGE = "coinbase";

    // NOTE the constructor and the establish connection methods are intentionally limited to just 1 product
    // at this time.
    public CoinbaseWebsocket(Book book, String product) throws URISyntaxException {
        super(new URI("wss://ws-feed.pro.coinbase.com"));
        this.book = book;
        this.product=product;
    }

    // This will start an ETH-USD stream.  To keep things simple, lets stick with a single book for now
    public void establishConnection() throws InterruptedException {
        this.connectBlocking();
        this.send("{\"type\": \"subscribe\",\"product_ids\": [\""+product+"\"],\"channels\": [\"level2\",\"heartbeat\"]}");
    }

    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("ON OPEN: "+serverHandshake.toString());
    }

    public void onMessage(String s) {
        JsonObject e=parser.parse(s).getAsJsonObject();
        if(!e.has("type")){
            logger.error("Got a message without a type:{}",s);
            return;
        }
        String type=e.get("type").getAsString();
        if(type.equals("snapshot")){
            book.clearBook();
            book.receiveBookUpdate(getForSnapshot(e));
        }else if(type.equals("l2update")){
            book.receiveBookUpdate(getForL2Update(e));
        }else if(type.equals("heartbeat")) {
            logger.debug("Received a heartbeat message for {}",this);
        }else if(type.equals("subscriptions")) {
            logger.info("Successfully subscribed for {} and got this response: {}",this,s);
        }else{
            logger.warn("Received an unknown message type from coinbase: {}",type);
        }
    }

    private Collection<BookUpdate> getForSnapshot(JsonObject o){
        String product=o.get("product_id").getAsString();
        List<BookUpdate> out=new LinkedList<>();
        for(JsonElement e: o.get("bids").getAsJsonArray()){
            BookUpdate b=new BookUpdate(product,EXCHANGE);
            b.setSide(Side.BUY);
            b.setPrice(e.getAsJsonArray().get(0).getAsBigDecimal());
            b.setQuantity(e.getAsJsonArray().get(1).getAsBigDecimal());
            out.add(b);
        }
        for(JsonElement e: o.get("asks").getAsJsonArray()){
            BookUpdate b=new BookUpdate(product,EXCHANGE);
            b.setSide(Side.SELL);
            b.setPrice(e.getAsJsonArray().get(0).getAsBigDecimal());
            b.setQuantity(e.getAsJsonArray().get(1).getAsBigDecimal());
            out.add(b);
        }
        return out;
    }

    private Collection<BookUpdate> getForL2Update(JsonObject o){
        String product=o.get("product_id").getAsString();
        List<BookUpdate> out=new LinkedList<>();
        for(JsonElement e: o.get("changes").getAsJsonArray()){
            BookUpdate b=new BookUpdate(product,EXCHANGE);
            String sideString=e.getAsJsonArray().get(0).getAsString();
            b.setSide(sideString.equals("buy")?Side.BUY:Side.SELL);
            b.setPrice(e.getAsJsonArray().get(1).getAsBigDecimal());
            b.setQuantity(e.getAsJsonArray().get(2).getAsBigDecimal());
            out.add(b);
        }
        return out;
    }

    public void onClose(int i, String s, boolean b) {
        logger.info("Connection closed for {} with this message: {}",this,s);
    }

    public void onError(Exception e) {
        logger.error("Received websockets exception for {}",this,e);
    }

    @Override
    public String toString() {
        return "CoinbaseWebsocket{" +
                "product='" + product + '\'' +
                '}';
    }
}
