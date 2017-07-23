package me.cnlm.mail.service;


import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * Created by zhuxl@paxsz.com on 2016/8/19.
 */
public class EmailAuthenticator extends Authenticator {
    private String username;
    private String password;

    public EmailAuthenticator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username,password);
    }
}
