package com.screensmybiz;

/**
 * Created by hannashulmah on 09/11/2016.
 */
public class LastMessage {
    String contact;
    String message;
    String time;
    String category;
    int profile;
    boolean my_msg;

    public LastMessage(String category, String contact, String message, int profile, String time, boolean my_msg) {
        this.category = category;
        this.contact = contact;
        this.message = message;
        this.profile = profile;
        this.time = time;
        this.my_msg = my_msg;
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getProfile() {
        return profile;
    }

    public void setProfile(int profile) {
        this.profile = profile;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isMy_msg() {
        return my_msg;
    }

    public void setMy_msg(boolean my_msg) {
        this.my_msg = my_msg;
    }
}
