package org.example;

public class Sequence {
    private int nextValue;

    public synchronized int getNext() {
        return nextValue++;
    }
}
