package com.erik.bookManagement.Events;

import com.erik.bookManagement.BookUpdate;

import java.util.Collection;

public class BookUpdateEvent implements BookEvent {
    private final Collection<BookUpdate> updates;

    public BookUpdateEvent(Collection<BookUpdate> updates) {
        this.updates = updates;
    }

    public Collection<BookUpdate> getUpdates() {
        return updates;
    }
}
