package com.jjerome.annotations;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FilteringOrder {

    int order();

    int defaultOrder = 1000;
}
