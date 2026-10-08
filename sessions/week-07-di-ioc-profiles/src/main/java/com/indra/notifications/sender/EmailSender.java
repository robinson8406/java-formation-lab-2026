package com.indra.notifications.sender;

public interface EmailSender {

    public void send(String to, String subject, String body);
}