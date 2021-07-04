package com.udemy.curso.libraryapi.service;

import java.util.List;

public interface EmailService {
    public void sendEmails(String message, List<String> emails);
}
