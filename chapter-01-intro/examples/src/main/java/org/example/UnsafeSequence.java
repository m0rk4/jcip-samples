package org.example;

public class UnsafeSequence {
    private int value;

    public int getNext() {
        return value++;
    }
}
