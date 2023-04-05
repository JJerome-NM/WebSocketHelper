package com.jjerome.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionMessage {
    CLASS_DONT_HAVE_FILTERING_ORDER("class does not have a FilteringOrder annotation, " +
            "which can have a bad effect on filtering connections");

    private final String message;
}
