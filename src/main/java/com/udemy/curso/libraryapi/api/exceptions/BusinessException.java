package com.udemy.curso.libraryapi.api.exceptions;

public class BusinessException extends RuntimeException {
    public BusinessException(String s) {
        super(s);
    }
}
