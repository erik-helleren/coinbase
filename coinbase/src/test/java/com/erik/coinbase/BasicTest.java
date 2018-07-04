package com.erik.coinbase;

import com.erik.bookManagement.BookManager;
import org.junit.Assert;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class BasicTest {

    @Test
    public void baseTest() throws URISyntaxException, InterruptedException {
        LinkedBlockingQueue<String> emitedStrings=new LinkedBlockingQueue<>();
        Consumer<String> consumer=(s)->emitedStrings.add(s);
        consumer=consumer.andThen((s)->System.out.println(s));
        BookManager bm=new BookManager("ETH-USD","coinbase",consumer);
        CoinbaseWebsocket cw=new CoinbaseWebsocket(bm,"ETH-USD");
        cw.establishConnection();
        Thread.sleep(1000);
        bm.emitSnapshot();
        Thread.sleep(1000);
        bm.emitL2Update();
        Thread.sleep(1000);
        bm.emitL2Update();
        Thread.sleep(1000);
        bm.emitSnapshot();
        Thread.sleep(1000);


        Assert.assertTrue(emitedStrings.take().contains("snapshot"));
        Assert.assertTrue(emitedStrings.take().contains("incremental"));
        Assert.assertTrue(emitedStrings.take().contains("incremental"));
        Assert.assertTrue(emitedStrings.take().contains("snapshot"));

        int a=1+1;
    }
}
