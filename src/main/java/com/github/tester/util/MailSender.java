package com.github.tester.util;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MailSender {
    private String smtpHost;
    private String smtpPort;
    private String authUserName;
    private String authPassword;

    public MailSender(String smtpHost, String smtpPort, String authUserName, String authPassword) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.authUserName = authUserName;
        this.authPassword = authPassword;
    }
    
    public void send (String subject, String body, String from, String... to) {
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);

        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(authUserName, authPassword);
            }
        };
        
        Session mailSession = Session.getDefaultInstance(props, auth);
        Message simpleMessage = new MimeMessage(mailSession);

        InternetAddress fromAddress = null;
        
        List<InternetAddress> addrList = new ArrayList<InternetAddress>();
        try {
            fromAddress = new InternetAddress(from);
            for (String temp : to) {
                InternetAddress t = new InternetAddress(temp);
                addrList.add(t);
            }
        } catch (AddressException e) {
            e.printStackTrace();
        }
        InternetAddress[] addrArray = new InternetAddress[addrList.size()];
        addrList.toArray(addrArray);
        
        try {
            simpleMessage.setFrom(fromAddress);
            simpleMessage.setRecipients(Message.RecipientType.TO, addrArray);
            simpleMessage.setSubject(subject);
            simpleMessage.setText(body);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        try {
            Transport.send(simpleMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        
        
    }
}
