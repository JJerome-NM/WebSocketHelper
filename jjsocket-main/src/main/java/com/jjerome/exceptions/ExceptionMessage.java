package com.jjerome.exceptions;

public enum ExceptionMessage {

    CLASS_DONT_HAVE_FILTERING_ORDER("class does not have a FilteringOrder annotation, " +
            "which can have a bad effect on filtering connections");

    private final String message;

    ExceptionMessage(String message){
        this.message = message;
    }

    public String get(){
        return this.message;
    }
}
