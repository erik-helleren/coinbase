# Coinbase and generic book management

So, what is this thing?  Despite being called coinbase, its really mostly a generic book management system that
is designed to work like an actor.  Messages are passed to the bookManager to update its state, clear itself, or publish snapshots and incremental updates to a consumer.  Incremental updates are batched between calls.

Example message flow:
Snapshot
incremental
incremental
...
Snapshot
incremental
...

The messages are sent via a message broker (Just a linked list for now) to enable downstream components to have a real time book state.  By embedding snapshots regularly in the outbound, we enable easy recovery for downstream components.  By batching incremental updates, we save stress on downstream components while sacrafacing some latency as book updates are pooled.

The Manager takes a configuration object and a Consumer<String> to send all configured streams to the provided consumer. 

This design also enables very modular testing.  The book management component is fully isolated from the connectors to
market data from various exchanges.  So each exchange connections can individually be tested separate from book management.  

This is far from production ready, and their remains a lot of extra configuration and hardening work that must be done.  
