package com.jjerome.annotations;

import com.jjerome.models.SocketMethodFilter;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SocketMappingFilter {

    Class<? extends SocketMethodFilter> filter();
}
