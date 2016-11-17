package com.screensmybiz;

import java.io.Serializable;

/**
 * Created by hannashulmah on 15/11/2016.
 */
public class SMessage implements Serializable {
    String _sender;
    String _reciever;
    String _message;
    int _time;

    public SMessage(String message, String reciever, String sender, int time) {
        this._message = message;
        this._reciever = reciever;
        this._sender = sender;
        this._time = time;
    }


    public String getMessage() {
        return _message;
    }

    public void setMessage(String message) {
        this._message = message;
    }

    public String getReciever() {
        return _reciever;
    }

    public void setReciever(String reciever) {
        this._reciever = reciever;
    }

    public String getSender() {
        return _sender;
    }

    public void setSender(String sender) {
        this._sender = sender;
    }

    public int getTime() {
        return _time;
    }

    public void setTime(int time) {
        this._time = time;
    }
}
