package com.erik.bookManagement;

import com.erik.bookManagement.Events.*;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class Book implements Runnable {
    private static final Logger logger = LogManager.getLogger(Book.class);
    private final String product;
    private final String exchange;
    private final Gson gson = new Gson();
    private final LinkedBlockingQueue<BookEvent> incomingBookEvents;
    private final Consumer<String> bookUpdateDestination;
    private boolean mustSendSnapshotNext = true;
    private long sequenceNumber = 0;
    /**
     * The full book state is used for emiting
     */
    private BookState fullState = new BookState();
    private BookState incrementalSate = new BookState();

    public Book(String product, String exchange, Consumer<String> bookUpdateDestination) {
        this.product = product;
        this.exchange = exchange;
        incomingBookEvents = new LinkedBlockingQueue<>();
        this.bookUpdateDestination = bookUpdateDestination;

        Thread t = new Thread(this);
        t.setName("BookStateWorker-" + exchange + "-" + product);
        t.setDaemon(true);
        t.start();
    }

    public void receiveSnapshot(Collection<BookUpdate> updateSet) {
        try {
            incomingBookEvents.put(new BookUpdateEvent(updateSet,true));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void receiveL2Update(Collection<BookUpdate> updateSet){
        try {
            incomingBookEvents.put(new BookUpdateEvent(updateSet,false));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void emitSnapshot() {
        try {
            incomingBookEvents.put(EmitSnapshotEvent.INSTANCE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void clearBook() {
        try {
            incomingBookEvents.put(ClearBookEvent.INSTANCE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * This may result in emiting a full snapshot if there was a clear in the underlying full book.
     */
    public void emitL2Update() {
        try {
            incomingBookEvents.put(EmitL2UpdateEvent.INSTANCE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void processBookUpdate(BookUpdateEvent updates) {
        if(updates.isSnapshot()){
            this.clearBook();
        }
        for (BookUpdate u : updates.getUpdates()) {
            if (!(u.getExchange().equals(this.exchange) &&
                    u.getProduct().equals(this.product))) {
                logger.warn("Book {} received an order from the wrong product or exchange.  Dropping: {}", this, u);
                continue;
            }
            fullState.processBookUpdate(u);
            incrementalSate.processBookUpdate(u);
        }
    }


    private String seralize(boolean forceSnapshot) {
        String type = null;
        JsonElement data = null;
        boolean takingSnapshot = forceSnapshot || this.mustSendSnapshotNext;
        if (takingSnapshot) {
            type = "snapshot";
            data = gson.toJsonTree(fullState.getBook());
            mustSendSnapshotNext = false;
        } else {
            type = "incremental";
            data = gson.toJsonTree(incrementalSate.getBook());
        }

        JsonObject output = new JsonObject();
        output.addProperty("type", type);
        output.addProperty("product", this.product);
        output.addProperty("exchange", this.exchange);
        output.addProperty("sequenceNumber", this.sequenceNumber++);
        output.addProperty("time", System.currentTimeMillis());
        BigDecimal bd = BigDecimal.ONE;
        output.add("data", data);


        String jsonString = output.toString();
        //Lets clear the incremental update after we have already serialized it and its ready to send.
        incrementalSate.clear();
        return jsonString;
    }

    @Override
    public String toString() {
        return "Book{" +
                "product='" + product + '\'' +
                ", exchange='" + exchange + '\'' +
                '}';
    }

    @Override
    public void run() {
        while (true) {
            BookEvent e = null;
            try {
                e = this.incomingBookEvents.take();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            if (e == null) continue;
            if (e instanceof BookUpdateEvent) {
                processBookUpdate((BookUpdateEvent) e);
            } else if (e instanceof EmitL2UpdateEvent) {
                // only send an incremental update if there is something to update
                if (incrementalSate.calculateBookSize() > 0) {
                    String output = seralize(false);
                    bookUpdateDestination.accept(output);
                }
            } else if (e instanceof EmitSnapshotEvent) {
                String output = seralize(true);
                bookUpdateDestination.accept(output);
            } else if (e instanceof ClearBookEvent) {
                this.fullState.clear();
                this.incrementalSate.clear();
            }
        }
    }
}
