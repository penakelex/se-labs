package com.example.core;

public class RxException extends Exception {
    public RxException(String message) { super(message); }
    public RxException(String message, Throwable cause) { super(message, cause); }
}
