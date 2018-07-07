package com.erik.bookManagement.Events;

public class EmitL2UpdateEvent implements BookEvent{
    public static final EmitL2UpdateEvent INSTANCE=new EmitL2UpdateEvent();

    private EmitL2UpdateEvent() {
    }
}
