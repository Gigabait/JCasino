package com.github.wyozi.jtexas.commonsg.net;

public interface Filter<T> {
    public boolean accept(T element);
}
