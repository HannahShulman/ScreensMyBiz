package com.cliqdbase.app.chats_stuff;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Yuval on 10/05/2015.
 *
 * @author Yuval Siev
 */
public class ChatsSQLiteHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "chat_db";
    public static final int DATABASE_VERSION = 1;


    public static final String CHATS_TABLE = "T0501_user_cliqs_chat_device";
    public static final String CHAT_DRAFTS_TABLE = "T0501_user_chat_drafts";
    public static final String USERS_TABLE = "T0100_device_msg_users";


    public static final String COLUMN_CHATS_MESSAGE_ID = "_id";               // The message id.
    public static final String COLUMN_CHATS_MESSAGE_TEXT = "msg_text";           // The text of the message.
    public static final String COLUMN_CHATS_MESSAGE_TIME = "msg_local_date";           // The time the massage was sent. - Number of milliseconds
    public static final String COLUMN_CHATS_MESSAGE_STATUS = "msg_status";       // 'R' = received, 'S' - Sent, 'C' = Called, 'E' = Error
    public static final String COLUMN_CHATS_MESSAGE_SIDE = "msg_side";           // 1 - if I sent the message, 2 - if the other side sent it.
    public static final String COLUMN_CHATS_MESSAGE_USER_ID = "msg_otherSide_userId";         // The user id of the other side of the conversation.



    public static final String COLUMN_CHAT_DRAFT_RECIPIENT_USER_ID = "recipient_user_id";
    public static final String COLUMN_CHAT_DRAFT_TEXT = "draft_message_text";
    public static final String COLUMN_CHAT_DRAFT_TIMESTAMP = "draft_timestamp";


    public static final String COLUMN_USERS_USER_ID = "userID";
    public static final String COLUMN_USERS_FIRST_NAME = "user_FirstName";     // The user name of the other side of the conversation.
    public static final String COLUMN_USERS_LAST_NAME = "user_LastName";     // The user name of the other side of the conversation.


    private static final String CREATE_CHATS_TABLE = "CREATE TABLE " + CHATS_TABLE + " (" +
            COLUMN_CHATS_MESSAGE_ID + " INTEGER PRIMARY KEY, " +
            COLUMN_CHATS_MESSAGE_TEXT + " TEXT NOT NULL, " +
            COLUMN_CHATS_MESSAGE_TIME + " INTEGER NOT NULL, " +
            COLUMN_CHATS_MESSAGE_STATUS + " TEXT NOT NULL, " +
            COLUMN_CHATS_MESSAGE_USER_ID + " INTEGER NOT NULL, " +
            COLUMN_CHATS_MESSAGE_SIDE + " INTEGER NOT NULL)";


    private static final String CREATE_CHAT_DRAFTS_TABLE = "CREATE TABLE " + CHAT_DRAFTS_TABLE + " (" +
            COLUMN_CHAT_DRAFT_RECIPIENT_USER_ID + " INTEGER PRIMARY KEY, " +
            COLUMN_CHAT_DRAFT_TEXT              + " TEXT NOT NULL, " +
            COLUMN_CHAT_DRAFT_TIMESTAMP         + " INTEGER NOT NULL)";


    private static final String CREATE_USERS_TABLE = "CREATE TABLE " + USERS_TABLE + " (" +
            COLUMN_USERS_USER_ID + " INTEGER PRIMARY KEY, " +
            COLUMN_USERS_FIRST_NAME + " TEXT NOT NULL, " +
            COLUMN_USERS_LAST_NAME + " TEXT NOT NULL)";



    public static final String SELECT_CHAT_CONVERSATIONS_LIST = "SELECT T.*, T2.user_LastName, T2.user_FirstName" +
            " FROM T0501_user_cliqs_chat_device T" +
            " JOIN" +
            " (SELECT" +
            " m.msg_otherSide_userId," +
            " u.user_LastName," +
            " u.user_FirstName," +
            " MAX(m.msg_local_date) msg_local_date" +
            " FROM T0501_user_cliqs_chat_device m," +
            " T0100_device_msg_users u" +
            " where m.msg_otherSide_userId = u.userID" +
            " GROUP BY m.msg_otherSide_userId,u.user_LastName, u.user_FirstName" +
            " ) T2" +
            " ON T.msg_otherSide_userId = T2.msg_otherSide_userId" +
            " AND T.msg_local_date = T2.msg_local_date" +
            " ORDER BY T.msg_local_date DESC";



    public static final String FIND_DRAFT_CHAT_MESSAGE = "SELECT * FROM " + CHAT_DRAFTS_TABLE +
            " WHERE " + COLUMN_CHAT_DRAFT_RECIPIENT_USER_ID + " = ?" ;




    /**
     * This query will return the cursor to populate a specific conversation based on the given user id.
     * This query have one ? in it - the user id of the user on the other side of the conversation we want to view.
     */
    public static final String SELECT_CONVERSATION_MESSAGES_LIST = "SELECT T1.*, T3." + COLUMN_USERS_LAST_NAME + ", T3." + COLUMN_USERS_FIRST_NAME +
            " FROM " + CHATS_TABLE + " T1, " +
            USERS_TABLE + " T3" +
            " WHERE T1." + COLUMN_CHATS_MESSAGE_USER_ID + " = T3." + COLUMN_USERS_USER_ID +
            " AND T1." + COLUMN_CHATS_MESSAGE_USER_ID + " = ?" +
            " ORDER BY T1." + COLUMN_CHATS_MESSAGE_TIME + " ASC";


    /**
     * This is a where clause to find the row with a specific message id.
     * This is used in {@link SQLiteDatabase#update(String, ContentValues, String, String[])} as the where clause string.
     * There is one ? in this string - the id of the message.
     */
    public static final String WHERE_CLAUSE_MESSAGE_ID = COLUMN_CHATS_MESSAGE_ID + " = ? ";

    //private Context context;

    public ChatsSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //this.context = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CHATS_TABLE);
        db.execSQL(CREATE_CHAT_DRAFTS_TABLE);
        db.execSQL(CREATE_USERS_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TODO write something here...
        /*db.execSQL("DROP TABLE IF EXISTS " + CHATS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE);

        SharedPreferences prefs = context.getSharedPreferences(SharedPreferencesConstants.GLOBAL_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_MAX_MESSAGE_ID_SENDER)
                .remove(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_MAX_MESSAGE_ID_RECIPIENT)
                .apply();

        onCreate(db);*/
        db.execSQL("DROP TABLE " + CHATS_TABLE);
        db.execSQL("DROP TABLE " + CHAT_DRAFTS_TABLE);
        db.execSQL("DROP TABLE " + USERS_TABLE);

        onCreate(db);
    }


    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
