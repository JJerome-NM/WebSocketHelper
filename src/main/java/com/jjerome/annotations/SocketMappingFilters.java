package com.jjerome.annotations;

import com.jjerome.filters.SocketMethodFilter;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SocketMappingFilters {

    Class<? extends SocketMethodFilter>[] filters();
}
