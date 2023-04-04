package com.jjerome.annotations;

import com.jjerome.filters.SocketMethodFilter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SocketMappingFilters {

    Class<? extends SocketMethodFilter>[] filters();
}
