package com.erik.Server.configuration;

import java.util.List;

public class Configuration {
    public static class Coinbase{
        public List<String> products;
    }

    /**
     * How frequently should an incremental update be published per book
     */
    public int incrementalFrequencyMs=10;
    /**
     * How frequently should a snapshot be published per book
     */
    public int snapshotFrequencyMs=2000;
    /**
     * The configuration for coinbase
     */
    public Coinbase coinbase;
}
