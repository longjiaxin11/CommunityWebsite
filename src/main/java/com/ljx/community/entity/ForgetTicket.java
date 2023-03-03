package com.ljx.community.entity;

public class ForgetTicket {
    String email;
    String kaptcha;


    public String getEmail() {
        return email;
    }

    public ForgetTicket setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getKaptcha() {
        return kaptcha;
    }

    public ForgetTicket setKaptcha(String kaptcha) {
        this.kaptcha = kaptcha;
        return this;
    }
}
