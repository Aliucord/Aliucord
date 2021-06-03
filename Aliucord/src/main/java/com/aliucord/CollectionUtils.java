/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord;

import java.util.*;

import kotlin.jvm.functions.Function1;

@SuppressWarnings("unused")
public class CollectionUtils {
    public static <E> E find(Collection<E> collection, Function1<E, Boolean> filter) {
        for (E e : collection) if (filter.invoke(e)) return e;
        return null;
    }

    public static <E> E findLast(Collection<E> collection, Function1<E, Boolean> filter) {
        E last = null;
        for(E e : collection) if (filter.invoke(e)) last = e;
        return last;
    }

    public static <E> int findIndex(List<E> list, Function1<E, Boolean> filter) {
        int i = 0;
        for(E e : list){
            if(filter.invoke(e)) return i;
            i++;
        }
        return -1;
    }

    public static <E> int findLastIndex(List<E> list, Function1<E, Boolean> filter) {
        Iterator<E> iterator = list.iterator();
        int last = -1;
        int i = 0;
        while(iterator.hasNext()){
            if(filter.invoke(iterator.next())) last = i;
            i++;
        }
        return last;
    }

    public static <E> List<E> filter(Collection<E> collection, Function1<E, Boolean> filter) {
        List<E> ret = new ArrayList<>();
        for (E e : collection) if (filter.invoke(e)) ret.add(e);
        return ret;
    }

    public static <E, R> List<R> map(Collection<E> collection, Function1<E, R> transform) {
        List<R> ret = new ArrayList<>(collection.size());
        for (E e : collection) ret.add(transform.invoke(e));
        return ret;
    }

    public static <E> boolean removeIf(Collection<E> collection, Function1<E, Boolean> filter) {
        Objects.requireNonNull(filter);
        boolean removed = false;
        final Iterator<E> iterator = collection.iterator();
        while (iterator.hasNext()) {
            if (filter.invoke(iterator.next())) {
                iterator.remove();
                removed = true;
            }
        }
        return removed;
    }

    public static <E> List<E> splice(List<E> list, int start) {
        return splice(list, start, list.size() - start);
    }

    @SafeVarargs
    public static <E> List<E> splice(List<E> list, int start, int deleteCount, E... items) {
        List<E> ret = new ArrayList<>();
        for (int i = 0; i < deleteCount; i++) ret.add(list.remove(start + i));
        list.addAll(start, Arrays.asList(items));
        return ret;
    }
}
