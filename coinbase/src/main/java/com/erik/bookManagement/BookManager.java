package com.erik.bookManagement;

import com.erik.bookManagement.Events.BookEvent;
import com.erik.bookManagement.Events.BookUpdateEvent;
import com.erik.bookManagement.Events.EmitL2UpdateEvent;
import com.erik.bookManagement.Events.EmitSnapshotEvent;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class BookManager implements Runnable{
    private static final Logger logger=LogManager.getLogger(BookManager.class);
    private final String product;
    private final String exchange;
    private final Gson gson=new Gson();

    /**
     * The full book state is used for emiting
     */
    private BookState fullState=new BookState();
    private BookState incrementalSate=new BookState();

    private final LinkedBlockingQueue<BookEvent> incomingBookEvents;
    private final Consumer<String>  bookUpdateDestination;

    public BookManager(String product, String exchange, Consumer<String>bookUpdateDestination) {
        this.product = product;
        this.exchange = exchange;
        incomingBookEvents =new LinkedBlockingQueue<>();
        this.bookUpdateDestination=bookUpdateDestination;

        Thread t=new Thread(this);
        t.setName("BookStateWorker-"+exchange+"-"+product);
        t.setDaemon(true);
        t.start();
    }

    public void receiveBookUpdate(Collection<BookUpdate> updateSet){
        try {
            incomingBookEvents.put(new BookUpdateEvent(updateSet));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.trace("Received a Book Update");
    }

    public void emitSnapshot(){
        try {
            incomingBookEvents.put(new EmitSnapshotEvent());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void emitL2Update(){
        try {
            incomingBookEvents.put(new EmitL2UpdateEvent());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        String type=null;
        JsonElement data=null;
        if(snapshot){
            type="snapshot";
            data=gson.toJsonTree(fullState);
        }else{
            type="incremental";
            data=gson.toJsonTree(incrementalSate);
        }

        JsonObject output=new JsonObject();
        output.addProperty("type",type);
        output.add("data",data);
        output.addProperty("time",System.currentTimeMillis());

        String jsonString = output.toString();
        //Lets clear the incremental update after we have made it ready to send.
        if(!snapshot){
            incrementalSate.clear();
        }
        return jsonString;
    }

    @Override
    public String toString() {
        return "BookManager{" +
                "product='" + product + '\'' +
                ", exchange='" + exchange + '\'' +
                ", book=" + fullState +
                '}';
    }

    @Override
    public void run() {
        while(true){
            BookEvent e=this.incomingBookEvents.poll();
            if(e instanceof BookUpdateEvent){
                processBookUpdate(((BookUpdateEvent)e).getUpdates());
            }else if(e instanceof EmitL2UpdateEvent){
                String output=seralize(false);
                bookUpdateDestination.accept(output);
            }else if(e instanceof EmitSnapshotEvent){
                String output=seralize(true);
                bookUpdateDestination.accept(output);
            }
        }
    }
}
