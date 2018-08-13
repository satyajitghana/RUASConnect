package com.debug.shadowleaf.ruasconnect.models;

import com.google.firebase.Timestamp;

import java.util.Date;

public class Message {
    public String senderID;
    public String senderName;
    public String content;
    //public FieldValue createdAt;
    //public Timestamp timestamp;
    public Date createdAt;

    public Message() {

    }

    public Message(String senderID, String senderName, String content) {
        this.senderID = senderID;
        this.senderName = senderName;
        this.content = content;
        //this.timestamp = new Timestamp(new Date());
        //this.createdAt = FieldValue.serverTimestamp();
        this.createdAt = new Date();
    }
}
