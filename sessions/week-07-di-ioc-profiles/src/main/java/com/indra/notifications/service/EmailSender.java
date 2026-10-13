package com.indra.notifications.service;

@FunctionalInterface
public interface EmailSender {

    void send(String to, String subject, String body);
}
