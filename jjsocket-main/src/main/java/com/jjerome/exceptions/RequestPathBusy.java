package com.jjerome.exceptions;

public class RequestPathBusy extends RuntimeException{
    public RequestPathBusy(String message){
        super(message);
    }
}
