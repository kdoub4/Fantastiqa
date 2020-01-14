package com.example.fantastiqa.GameState;

import androidx.annotation.NonNull;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

public class LoopingLinkedList<T> extends LinkedList<T> {
    @Override
    public ListIterator<T> listIterator() {
        return new LoopingListIterator<>(super.listIterator());
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return new LoopingListIterator<>(super.listIterator(index));
    }
}
