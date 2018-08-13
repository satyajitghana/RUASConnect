package com.debug.shadowleaf.ruasconnect.models;

import java.util.Map;

public class User {
    public String uid;
    public String regNo;
    public String name;
    public String email;

    public User() {

    }

    public User(String uid, String regNo, String name, String email) {
        this.uid = uid;
        this.regNo = regNo;
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return this.name;
    }

    public String getRegNo() {
        return this.regNo;
    }

    public String getEmail() {
        return email;
    }

    public String getUid() {
        return uid;
    }

    @Override
    public String toString() {
        return uid + " " + regNo + " " + name + " " + email;
    }
}
