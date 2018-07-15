package com.erik.coinbase;

import com.erik.Server.Manager;
import com.erik.Server.configuration.Configuration;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class ManagerTest {

    @Test
    public void basicManagerTest() throws InterruptedException {
        LinkedBlockingQueue<String> emitedStrings=new LinkedBlockingQueue<>();
        Consumer<String> consumer=(s)->emitedStrings.add(s);
        consumer=consumer.andThen((s)->System.out.println(s));
        Configuration c= new Configuration();
        c.coinbase=new Configuration.Coinbase();
        c.coinbase.products= Arrays.asList("ETH-USD","BTC-USD");

        Manager m=new Manager(c,consumer);
        Thread.sleep(1000);
    }
}
