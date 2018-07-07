package com.erik.bookManagement.Events;

public class ClearBookEvent implements BookEvent {
    public static final ClearBookEvent INSTANCE=new ClearBookEvent();
    private ClearBookEvent() {
    }
}
