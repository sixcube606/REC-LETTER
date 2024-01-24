package com.sixcube.recletter.email.service;

import org.springframework.mail.SimpleMailMessage;


public interface EmailService {

    public void sendEmail(String toEmail, String title, String text) throws Exception;

    public SimpleMailMessage createEmailForm(String toEmail, String title, String text);

}
