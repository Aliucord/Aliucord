package com.aliucord;

import java.util.*;

import kotlin.jvm.functions.Function1;

@SuppressWarnings("unused")
public class CollectionUtils {
    public static <E> E find(Collection<E> collection, Function1<E, Boolean> filter) {
        for (E e : collection) if (filter.invoke(e)) return e;
        return null;
    }

    public static <E> int findIndex(List<E> list, Function1<E, Boolean> filter) {
        int j = list.size();
        for (int i = 0; i < j; i++) if (filter.invoke(list.get(i))) return i;
        return -1;
    }

    public static <E> int findLastIndex(List<E> list, Function1<E, Boolean> filter) {
        for (int i = list.size() - 1; i >= 0; i--) if (filter.invoke(list.get(i))) return i;
        return -1;
    }

    public static <E> List<E> filter(Collection<E> collection, Function1<E, Boolean> filter) {
        List<E> ret = new ArrayList<>();
        for (E e : collection) if (filter.invoke(e)) ret.add(e);
        return ret;
    }

    public static <E, R> List<R> map(Collection<E> collection, Function1<E, R> transform) {
        List<R> ret = new ArrayList<>();
        for (E e : collection) ret.add(transform.invoke(e));
        return ret;
    }

    public static <E> boolean removeIf(Collection<E> collection, Function1<E, Boolean> filter) {
        Objects.requireNonNull(filter);
        boolean removed = false;
        final Iterator<E> each = collection.iterator();
        while (each.hasNext()) {
            if (filter.invoke(each.next())) {
                each.remove();
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
        for (int i = items.length - 1; i >= 0; i--) list.add(start, items[i]);
        return ret;
    }
}
