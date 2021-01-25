package com.masranber.bikecomputer;

public class Event<T> {

    private T content;
    private boolean hasBeenHandled;

    public Event(T content) {
        this.content = content;
        this.hasBeenHandled = false;
    }

    public T getContent() {
        if(hasBeenHandled) return null;
        else {
            hasBeenHandled = true;
            return content;
        }
    }

    public T peekContent() {
        return content;
    }

    public boolean hasBeenHandled() {
        return hasBeenHandled;
    }

}
