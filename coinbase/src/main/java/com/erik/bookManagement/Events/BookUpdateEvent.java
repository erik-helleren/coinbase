package com.erik.bookManagement.Events;

import com.erik.bookManagement.BookUpdate;

import java.util.Collection;

public class BookUpdateEvent implements BookEvent {
    private final Collection<BookUpdate> updates;
    private final boolean snapshot;

    public BookUpdateEvent(Collection<BookUpdate> updates,boolean snapshot) {
        this.snapshot=snapshot;
        this.updates = updates;
    }

    public boolean isSnapshot() {
        return snapshot;
    }

    public Collection<BookUpdate> getUpdates() {
        return updates;
    }
}
