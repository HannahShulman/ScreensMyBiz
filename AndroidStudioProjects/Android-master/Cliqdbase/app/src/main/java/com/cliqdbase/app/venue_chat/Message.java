package com.cliqdbase.app.venue_chat;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.Date;

/**
 * Created by Yuval on 27/07/2015.
 *
 * @author Yuval Siev
 */
@SuppressWarnings("unused")         // We are using them in Gson
public class Message implements Parcelable{
    private String text;
    private Date date;
    private String userName;
    private long userId;

    public Message(String text, Date date, String userName, long userId) {
        this.text = text;
        this.date = date;
        this.userName = userName;
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public Date getDate() {
        return date;
    }

    public long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public Message(Parcel parcel) {
        this.text = parcel.readString();
        this.userName = parcel.readString();
        this.userId = parcel.readLong();
        this.date = new Date(parcel.readLong());
    }


    public static Message getMessageFromJson(String json) {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);

        Type messageType = new TypeToken<Message>() {}.getType();

        return gson.fromJson(reader, messageType);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.text);
        dest.writeString(this.userName);
        dest.writeLong(this.userId);
        dest.writeLong(this.date.getTime());
    }

    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
}
