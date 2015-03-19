package com.github.tester.util;

import org.apache.commons.lang3.StringUtils;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

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
    
    public void send (String subject, String body, String from, String to, String cc) {
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
        
        List<InternetAddress> toList = new ArrayList<InternetAddress>();
        List<InternetAddress> ccList = new ArrayList<InternetAddress>();
        try {
            fromAddress = getAddress(from);
            StringTokenizer st = new StringTokenizer(to, ",");
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                InternetAddress addr = getAddress(token);
                toList.add(addr);
            }
            StringTokenizer st2 = new StringTokenizer(cc, ",");
            while (st2.hasMoreTokens()) {
                String token = st2.nextToken();
                InternetAddress addr = getAddress(token);
                ccList.add(addr);
            }
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        InternetAddress[] toArray = new InternetAddress[toList.size()];
        toList.toArray(toArray);
        InternetAddress[] ccArray = new InternetAddress[ccList.size()];
        ccList.toArray(ccArray);
        
        try {
            simpleMessage.setFrom(fromAddress);
            simpleMessage.setRecipients(Message.RecipientType.TO, toArray);
            simpleMessage.setRecipients(Message.RecipientType.CC, ccArray);
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
    
    private InternetAddress getAddress(String address) throws AddressException, UnsupportedEncodingException {
        if (StringUtils.contains(address, ":")) {
            String[] temp = StringUtils.split(address, ":");
            return new InternetAddress(temp[0],temp[1]);
        } else {
            return new InternetAddress(address);
        }
    }
}
