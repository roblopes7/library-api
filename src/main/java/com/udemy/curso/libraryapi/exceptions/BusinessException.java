package com.udemy.curso.libraryapi.exceptions;

public class BusinessException extends RuntimeException {
    public BusinessException(String s) {
        super(s);
    }
}
