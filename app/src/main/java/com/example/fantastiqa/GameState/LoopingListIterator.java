package com.example.fantastiqa.GameState;

import java.util.ListIterator;

public class LoopingListIterator<T> implements ListIterator<T> {

    private ListIterator<T> underlying;
    public LoopingListIterator(ListIterator<T> underlying) {
        this.underlying = underlying;
    }

    @Override public boolean hasNext() {return underlying.hasNext();}
    @Override public boolean hasPrevious() {return underlying.hasPrevious();}
    @Override public int nextIndex() {return underlying.nextIndex();}
    @Override public int previousIndex() {return underlying.previousIndex();}
    @Override public void remove() { underlying.remove();}

    @Override
    public void set(T t) {
        underlying.set(t);
    }

    @Override
    public void add(T t) {
        underlying.set(t);
    }

    @Override public T next() {
        if (hasNext()) {
            return underlying.next();
        } else {
            while (previousIndex() > 0) {
                previous();
            }
            return underlying.previous();
        }
    }

    @Override public T previous() {
        if (hasPrevious()) {
            return underlying.previous();
        }
        else {
            while (hasNext()) {
                next();
            }
            previous();
            return underlying.next();
        }
    }
}