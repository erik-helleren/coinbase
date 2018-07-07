package com.erik.bookManagement.Events;

public class EmitSnapshotEvent implements BookEvent {
    public static final EmitSnapshotEvent INSTANCE=new EmitSnapshotEvent();

    private EmitSnapshotEvent() {
    }
}
