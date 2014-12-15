package com.github.wyozi.jtexas.commons.util;

public interface Filter<T> {
    public boolean accept(T element);
}
