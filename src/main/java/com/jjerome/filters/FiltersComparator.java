package com.jjerome.filters;

import com.jjerome.annotations.FilteringOrder;


import java.util.Comparator;

public class FiltersComparator<F> implements Comparator<F> {
    @Override
    public int compare(F filter1, F filter2) {
        if (!filter1.getClass().isAnnotationPresent(FilteringOrder.class)) {
            return 1;
        } else if (!filter2.getClass().isAnnotationPresent(FilteringOrder.class)) {
            return -1;
        } else if (filter1 == filter2){
            return 0;
        }

        int firstFilterOrder = filter1.getClass().getDeclaredAnnotation(FilteringOrder.class).order();
        int secondFilterOrder = filter2.getClass().getDeclaredAnnotation(FilteringOrder.class).order();

        int result = firstFilterOrder - secondFilterOrder;

        return result != 0 ? result : -1;
    }
}
