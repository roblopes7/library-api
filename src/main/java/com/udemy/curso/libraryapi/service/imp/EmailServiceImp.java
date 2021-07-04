package com.udemy.curso.libraryapi.service.imp;

import com.udemy.curso.libraryapi.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailServiceImp implements EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${application.mail.lateloans.defaultRemetent}")
    private String remetent;

    @Override
    public void sendEmails(String message, List<String> emails) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(remetent);
        mailMessage.setSubject("Livro Empréstimo Atrasado");
        mailMessage.setText(message);
        mailMessage.setTo(emails.toArray(new String[emails.size()]));
        javaMailSender.send(mailMessage);
    }
}
