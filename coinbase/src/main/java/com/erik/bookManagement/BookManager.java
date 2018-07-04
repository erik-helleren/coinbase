package com.erik.bookManagement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

public class BookManager {
    private static final Logger logger=LogManager.getLogger(BookManager.class);
    private final String product;
    private final String exchange;

    /**
     * The full book state is used for emiting
     */
    private BookState fullState=new BookState();
    private BookState incrementalSate=new BookState();

    private final LinkedBlockingQueue<Collection<BookUpdate>> incomingBookUpdates;


    public BookManager(String product, String exchange) {
        this.product = product;
        this.exchange = exchange;
        incomingBookUpdates =new LinkedBlockingQueue<>();
    }

    public void receiveBookUpdate(Collection<BookUpdate> updateSet){
        processBookUpdate(updateSet);
    }

    public void processBookUpdate(Collection<BookUpdate> updates){
        for(BookUpdate u:updates){
            if(!(u.getExchange().equals(this.exchange)&&
                u.getProduct().equals(this.product))){
                logger.warn("Book {} received a order from the wrong product or exchange.  Dropping: {}",this,u);
                continue;
            }
            fullState.processBookUpdate(u);
            incrementalSate.processBookUpdate(u);
        }
    }


    public String seralize(boolean snapshot){
        if(snapshot){

        }
    }

    @Override
    public String toString() {
        return "BookManager{" +
                "product='" + product + '\'' +
                ", exchange='" + exchange + '\'' +
                ", book=" + book +
                '}';
    }
}
