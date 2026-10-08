package com.indra.notifications.service;

public interface EmailSender {
    void send(String to, String subject, String body);
}

