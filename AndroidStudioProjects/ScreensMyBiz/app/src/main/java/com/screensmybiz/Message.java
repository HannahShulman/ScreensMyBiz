package com.screensmybiz;
//

import java.io.Serializable;

//
///**
// * Created by hannashulmah on 26/09/2016.
// */
public class Message implements Serializable {
    private String mText;
    private String sender_name;
    private String sender_uid;
    private String receiver_uid;
    private String receiver_name;
    private long timeStamp;
    private String key;




    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    public String getmText() {
        return mText;
    }

    public void setmText(String mText) {
        this.mText = mText;
    }

    public String getReceiverName() {
        return receiver_name;
    }

    public void setReceiverName(String receiver) {
        this.receiver_name = receiver;
    }

    public String getReceiverUid() {
        return receiver_uid;
    }

    public void setReceiverUid(String receiverUid) {
        this.receiver_uid = receiverUid;
    }

    public int getSeen() {
        return seen;
    }

    public void setSeen(int seen) {
        this.seen = seen;
    }

    public String getSender_uid() {
        return sender_uid;
    }

    public void setSender_uid(String sender_uid) {
        this.sender_uid = sender_uid;
    }

    public String getSenderName() {
        return sender_name;
    }

    public void setSenderName(String senderName) {
        this.sender_name = senderName;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    private int seen = 0;

    public Message() {
    }

    public Message(String senderName, String sender_uid, String mReceiver, String receiverUid, String message, int seen, long timeStamp) {
        this.sender_uid = sender_uid;
        this.receiver_name = mReceiver;
        this.receiver_uid = receiverUid;
        this.sender_name = senderName;
        this.mText = message;
        this.seen = seen;
        this.timeStamp = timeStamp;
    }

}
