package com.erik.coinbase;

import com.erik.bookManagement.BookManager;
import org.junit.Test;

import java.net.URISyntaxException;

public class BasicTest {

    @Test
    public void baseTest() throws URISyntaxException, InterruptedException {
        BookManager bm=new BookManager("ETH-USD","coinbase");
        CoinbaseWebsocket cw=new CoinbaseWebsocket(bm);
        cw.establishConnection();
    }
}
