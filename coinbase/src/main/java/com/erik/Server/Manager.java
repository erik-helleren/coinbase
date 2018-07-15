package com.erik.Server;

import com.erik.Server.configuration.Configuration;
import com.erik.bookManagement.Book;
import com.erik.coinbase.CoinbaseWebsocket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Manager {
    private static final Logger logger = LogManager.getLogger(Manager.class);
    private final Consumer<String> destination;
    private final List<Book> books = new ArrayList<>();
    private final long snapshotFrequency;
    private final ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();
    private long lastSnapshotTime = 0;

    public Manager(Configuration configuration, Consumer<String> destination) {
        this.destination = destination;
        this.snapshotFrequency = configuration.snapshotFrequencyMs;
        this.coinbase(configuration.coinbase);
        ex.scheduleAtFixedRate(() -> this.handleTimer(), 0, configuration.incrementalFrequencyMs, TimeUnit.MILLISECONDS);
    }

    public void coinbase(Configuration.Coinbase coinbase) {
        if (coinbase == null) return;
        for (String product : coinbase.products) {
            Book b = new Book(product, "coinbase", this.destination);
            try {
                CoinbaseWebsocket websocket = new CoinbaseWebsocket(b, product);
                websocket.establishConnection();
                books.add(b);
            } catch (URISyntaxException| InterruptedException e) {
                logger.error("Failed to startup op coinbase's websocket for product {}", product, e);
            }
        }
    }

    public void handleTimer() {
        long currentTime = System.currentTimeMillis();
        if (currentTime > lastSnapshotTime + snapshotFrequency) {
            for (Book b : books) {
                b.emitSnapshot();
            }
            lastSnapshotTime=currentTime;
        } else {
            for (Book b : books) {
                b.emitL2Update();
            }
        }
    }


}
