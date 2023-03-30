package com.jjerome.filters;

import com.jjerome.annotations.FilteringOrder;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class ConnectFilterComparator implements Comparator<SocketConnectionFilter> {

    @Override
    public int compare(@NotNull SocketConnectionFilter f1, @NotNull SocketConnectionFilter f2) {
        if (!f1.getClass().isAnnotationPresent(FilteringOrder.class)) return 1;
        if (!f2.getClass().isAnnotationPresent(FilteringOrder.class)) return -1;

        int result = f1.getClass().getDeclaredAnnotation(FilteringOrder.class).order() -
                f2.getClass().getDeclaredAnnotation(FilteringOrder.class).order();

        return result != 0 ? result : f1 == f2 ? 0 : -1;
    }
}
