package com.erik.coinbase;

import com.erik.bookManagement.Book;
import com.erik.bookManagement.BookUpdate;
import com.erik.bookManagement.Side;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class Booktest {

    public static final JsonParser parser = new JsonParser();
    public static final String TEST_PRODUCT = "ETH-USD";
    public static final String TEST_EXCHANGE = "coinbase";

    @Test
    public void emptyBookTest() throws InterruptedException {
        LinkedBlockingQueue<String> emitedStrings=new LinkedBlockingQueue<>();
        Consumer<String> consumer=(s)->emitedStrings.add(s);
        Book bm=new Book(TEST_PRODUCT, TEST_EXCHANGE,consumer);
        bm.emitSnapshot();

        String firstSnapshot=emitedStrings.take();
        System.out.println(firstSnapshot);
        JsonObject o=parser.parse(firstSnapshot).getAsJsonObject();

        Assert.assertEquals("snapshot",o.get("type").getAsString());
        Assert.assertEquals(TEST_PRODUCT,o.get("product").getAsString());
        Assert.assertEquals(TEST_EXCHANGE,o.get("exchange").getAsString());
        Assert.assertEquals(0,o.get("sequenceNumber").getAsInt());
        for(Map.Entry<String, JsonElement> element:o.get("data").getAsJsonObject().entrySet())
            Assert.assertTrue(element.getValue().getAsJsonObject().entrySet().size()==0);

    }

    @Test
    public void sequenceNumberTest() throws InterruptedException {
        LinkedBlockingQueue<String> emitedStrings=new LinkedBlockingQueue<>();
        Consumer<String> consumer=(s)->emitedStrings.add(s);
        Book bm=new Book(TEST_PRODUCT, TEST_EXCHANGE,consumer);
        bm.emitSnapshot();
        bm.emitSnapshot();

        JsonObject firstSnapshot=parser.parse(emitedStrings.take()).getAsJsonObject();
        JsonObject secondSnapshot=parser.parse(emitedStrings.take()).getAsJsonObject();

        Assert.assertEquals(0,firstSnapshot.get("sequenceNumber").getAsInt());
        Assert.assertEquals(1,secondSnapshot.get("sequenceNumber").getAsInt());
    }
    
    @Test
    public void l2AfterSnapshotEmptyTest() throws InterruptedException {
        LinkedBlockingQueue<String> emitedStrings=new LinkedBlockingQueue<>();
        Consumer<String> consumer=(s)->emitedStrings.add(s);
        Book bm=new Book(TEST_PRODUCT, TEST_EXCHANGE,consumer);
        bm.emitSnapshot();
        BookUpdate bu=new BookUpdate(TEST_PRODUCT,TEST_EXCHANGE);
        bu.setPrice(new BigDecimal(123));
        bu.setQuantity(BigDecimal.TEN);
        bu.setSide(Side.BUY);
        bm.receiveBookUpdate(Collections.singletonList(bu));
        bm.emitSnapshot();
        bm.emitL2Update();

        JsonObject firstSnapshot=parser.parse(emitedStrings.take()).getAsJsonObject();
        JsonObject secondSnapshot=parser.parse(emitedStrings.take()).getAsJsonObject();
        JsonObject firstL2Update=parser.parse(emitedStrings.take()).getAsJsonObject();
        for(Map.Entry<String, JsonElement> element:firstSnapshot.get("data").getAsJsonObject().entrySet())
            Assert.assertTrue(element.getValue().getAsJsonObject().entrySet().size()==0);

        Assert.assertTrue(secondSnapshot.get("data").getAsJsonObject()
                .getAsJsonObject("BUY").entrySet().size()==1);
        Assert.assertTrue(secondSnapshot.get("data").getAsJsonObject()
                .getAsJsonObject("SELL").entrySet().size()==0);

        for(Map.Entry<String, JsonElement> element:firstL2Update.get("data").getAsJsonObject().entrySet())
            Assert.assertTrue(element.getValue().getAsJsonObject().entrySet().size()==0);

    }

}
