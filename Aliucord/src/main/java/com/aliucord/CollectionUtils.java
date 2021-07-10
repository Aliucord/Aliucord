/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.*;

import kotlin.jvm.functions.Function1;

@SuppressWarnings("unused")
public class CollectionUtils {
    /**
     * Check whether any element of the collection passes the filter
     * @return True if condition is true for any element in the collection
     */
    public static <E> boolean some(@NonNull Collection<E> collection, @NonNull Function1<E, Boolean> filter) {
        return find(collection, filter) != null;
    }

    /**
     * Check whether all elements of the collection pass the filter
     * @return True if condition is true for all elements in the collection
     */
    public static <E> boolean every(@NonNull Collection<E> collection, @NonNull Function1<E, Boolean> filter) {
        for (E e : collection) if (!filter.invoke(e)) return false;
        return true;
    }

    /**
     * Find the first element which passes the filter
     * @return Element if found, otherwise null
     */
    @Nullable
    public static <E> E find(@NonNull Collection<E> collection, @NonNull Function1<E, Boolean> filter) {
        for (E e : collection) if (filter.invoke(e)) return e;
        return null;
    }

    /**
     * Find the last element which passes the filter
     * @return Element if found, otherwise null
     */
    @Nullable
    public static <E> E findLast(@NonNull Collection<E> collection, @NonNull Function1<E, Boolean> filter) {
        E last = null;
        for(E e : collection) if (filter.invoke(e)) last = e;
        return last;
    }

    /**
     * Find the index of the first element which passes the filter
     * @return Index if found, otherwise -1
     */
    public static <E> int findIndex(@NonNull List<E> list, @NonNull Function1<E, Boolean> filter) {
        int i = 0;
        for(E e : list){
            if(filter.invoke(e)) return i;
            i++;
        }
        return -1;
    }

    /**
     * Find the index of the last element which passes the filter
     * @return Index if found, otherwise -1
     */
    public static <E> int findLastIndex(@NonNull List<E> list, @NonNull Function1<E, Boolean> filter) {
        Iterator<E> iterator = list.iterator();
        int last = -1;
        int i = 0;
        while(iterator.hasNext()){
            if(filter.invoke(iterator.next())) last = i;
            i++;
        }
        return last;
    }

    /**
     * Returns a new Array containing only the elements which passed the filter
     * @return Filtered Collection
     */
    @NonNull
    public static <E> List<E> filter(@NonNull Collection<E> collection, @NonNull Function1<E, Boolean> filter) {
        List<E> ret = new ArrayList<>();
        for (E e : collection) if (filter.invoke(e)) ret.add(e);
        return ret;
    }

    /**
     * Returns a new Array containing the results of the transform function for all elements
     * @return Filtered Collection
     */
    @NonNull
    public static <E, R> List<R> map(@NonNull Collection<E> collection, @NonNull Function1<E, R> transform) {
        List<R> ret = new ArrayList<>(collection.size());
        for (E e : collection) ret.add(transform.invoke(e));
        return ret;
    }

    /**
     * Removes all elements from the collection which pass the filter
     * @return Whether an element was removed
     */
    public static <E> boolean removeIf(@NonNull Collection<E> collection, @NonNull Function1<E, Boolean> filter) {
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

    /**
     * Partition the collection into two Arrays. The first array has all elements which passed the filter, the second one has the rest
     * @return A {@link Pair} containing the two arrays
     */
    public static <E> Pair<List<E>, List<E>> partition(@NonNull Collection<E> collection, @NonNull Function1<E, Boolean> filter) {
        List<E> passed = new ArrayList<>();
        List<E> failed = new ArrayList<>();
        for (E e : collection) {
            if (filter.invoke(e)) passed.add(e);
            else failed.add(e);
        }
        return new Pair<>(passed, failed);
    }


    /**
     * Removes all elements after the specified start index
     * @return The removed elements
     */
    public static <E> List<E> splice(List<E> list, int start) {
        return splice(list, start, list.size() - start);
    }

    /**
     * Removes the specified amount of elements after the specified start index and inserts the specified items
     * @param list The list of splice
     * @param start The start index
     * @param deleteCount The amount of items to remove
     * @param items The items to insert
     * @return The removed elements
     */
    @SafeVarargs
    public static <E> List<E> splice(List<E> list, int start, int deleteCount, E... items) {
        List<E> ret = new ArrayList<>(deleteCount);
        for (int i = 0; i < deleteCount; i++) ret.add(list.remove(start + i));
        list.addAll(start, Arrays.asList(items));
        return ret;
    }
}
