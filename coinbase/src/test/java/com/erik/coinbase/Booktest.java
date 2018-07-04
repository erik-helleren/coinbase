package com.erik.coinbase;

import com.erik.bookManagement.BookManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class Booktest {

    public static final JsonParser parser = new JsonParser();

    @Test
    public void emptyBookTest() throws InterruptedException {
        LinkedBlockingQueue<String> emitedStrings=new LinkedBlockingQueue<>();
        Consumer<String> consumer=(s)->emitedStrings.add(s);
        BookManager bm=new BookManager("ETH-USD","coinbase",consumer);
        bm.emitSnapshot();

        String firstSnapshot=emitedStrings.take();
        System.out.println(firstSnapshot);
        JsonObject o=parser.parse(firstSnapshot).getAsJsonObject();

        Assert.assertEquals("snapshot",o.get("type").getAsString());
        Assert.assertEquals("ETH-USD",o.get("product").getAsString());
        Assert.assertEquals("coinbase",o.get("exchange").getAsString());
        Assert.assertEquals(0,o.get("sequenceNumber").getAsInt());
        Assert.assertTrue(o.get("data").getAsJsonObject().entrySet().size()==0);

    }

    @Test
    public void sequenceNumberTest() throws InterruptedException {
        LinkedBlockingQueue<String> emitedStrings=new LinkedBlockingQueue<>();
        Consumer<String> consumer=(s)->emitedStrings.add(s);
        BookManager bm=new BookManager("ETH-USD","coinbase",consumer);
        bm.emitSnapshot();
        bm.emitSnapshot();

        JsonObject firstSnapshot=parser.parse(emitedStrings.take()).getAsJsonObject();
        JsonObject secondSnapshot=parser.parse(emitedStrings.take()).getAsJsonObject();

        Assert.assertEquals(0,firstSnapshot.get("sequenceNumber").getAsInt());
        Assert.assertEquals(1,secondSnapshot.get("sequenceNumber").getAsInt());
    }

}
