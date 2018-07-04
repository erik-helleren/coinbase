package com.erik.coinbase;

import com.erik.bookManagement.BookManager;
import org.junit.Assert;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class BasicTest {

    @Test
    public void baseTest() throws URISyntaxException, InterruptedException {
        ConcurrentLinkedQueue<String> emitedStrings=new ConcurrentLinkedQueue<>();
        Consumer<String> consumer=(s)->emitedStrings.add(s);
        consumer=consumer.andThen((s)->System.out.println(s));
        BookManager bm=new BookManager("ETH-USD","coinbase",consumer);
        CoinbaseWebsocket cw=new CoinbaseWebsocket(bm);
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


        Assert.assertTrue(emitedStrings.poll().contains("snapshot"));
        Assert.assertTrue(emitedStrings.poll().contains("incremental"));
        Assert.assertTrue(emitedStrings.poll().contains("incremental"));
        Assert.assertTrue(emitedStrings.poll().contains("snapshot"));

        int a=1+1;
    }
}
