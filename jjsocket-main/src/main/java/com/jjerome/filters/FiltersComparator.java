package com.jjerome.filters;

import com.jjerome.annotations.FilteringOrder;
import lombok.NonNull;

import java.util.Comparator;

public class FiltersComparator<F> implements Comparator<F> {
    @Override
    public int compare(@NonNull F f1, @NonNull F f2) {
        if (!f1.getClass().isAnnotationPresent(FilteringOrder.class)) return 1;
        if (!f2.getClass().isAnnotationPresent(FilteringOrder.class)) return -1;

        if (f1 == f2) return 0;

        int firstFilterOrder = f1.getClass().getDeclaredAnnotation(FilteringOrder.class).order();
        int secondFilterOrder = f2.getClass().getDeclaredAnnotation(FilteringOrder.class).order();

        int result = firstFilterOrder - secondFilterOrder;

        return result != 0 ? result : -1;
    }
}
