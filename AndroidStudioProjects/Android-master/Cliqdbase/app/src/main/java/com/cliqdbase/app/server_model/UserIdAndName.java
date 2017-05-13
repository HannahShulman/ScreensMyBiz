package com.cliqdbase.app.server_model;

import android.content.ContentValues;

import com.cliqdbase.app.chats_stuff.ChatsSQLiteHelper;

/**
 * Created by Yuval on 11/06/2015.
 *
 * @author Yuval Siev
 */
public class UserIdAndName {
    private long userId;
    private String firstName;
    private String lastName;

    public UserIdAndName(long userId, String firstName, String lastName) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public long getUserId() {
        return userId;
    }

    /**
     * @return A ContentValues object to insert to the users table.
     * @see ChatsSQLiteHelper#USERS_TABLE
     */
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();

        values.put(ChatsSQLiteHelper.COLUMN_USERS_USER_ID, this.userId);
        values.put(ChatsSQLiteHelper.COLUMN_USERS_FIRST_NAME, this.firstName);
        values.put(ChatsSQLiteHelper.COLUMN_USERS_LAST_NAME, this.lastName);

        return values;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UserIdAndName))
            return false;
        else if (this == obj)
            return true;

        UserIdAndName otherUser = (UserIdAndName) obj;
        return  this.userId == otherUser.getUserId();
    }
}
