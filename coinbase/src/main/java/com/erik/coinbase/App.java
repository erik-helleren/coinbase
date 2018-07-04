package com.erik.coinbase;

import com.erik.bookManagement.BookManager;

import java.net.URISyntaxException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws URISyntaxException, InterruptedException {
        BookManager bm=new BookManager("ETH-USD","coinbase");
        CoinbaseWebsocket cw=new CoinbaseWebsocket(bm);
        cw.establishConnection();
        Thread.sleep(30000);
    }
}
