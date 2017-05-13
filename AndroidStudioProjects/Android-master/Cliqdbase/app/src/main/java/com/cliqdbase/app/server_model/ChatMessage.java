package com.cliqdbase.app.server_model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.cliqdbase.app.chats_stuff.ChatsSQLiteHelper;

/**
 * @author Yuval
 *
 */
public class ChatMessage {

    /**
     * Removes a chat message from the LOCAL database (not from the server)
     * @param context      The application's context.
     * @param messageIds   List of the id of the messages we are about to delete.
     */
    public static void deleteChatMessages(Context context, List<Long> messageIds) {
        ChatsSQLiteHelper helper = new ChatsSQLiteHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        for (long messageId : messageIds)
            db.delete(ChatsSQLiteHelper.CHATS_TABLE, ChatsSQLiteHelper.COLUMN_CHATS_MESSAGE_ID + "=?", new String[] {String.valueOf(messageId)});

        db.close();
        helper.close();
    }


    /**
     * Removes a whole conversation from the Local database. All the messages with the given user id will be deleted.
     * @param context    The application's context.
     * @param userIds    A list of the user ids whose conversations we will be deleting
     */
    public static void deleteChatConversations(Context context, List<Long> userIds) {
        ChatsSQLiteHelper helper = new ChatsSQLiteHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        for (long userId : userIds)
            db.delete(ChatsSQLiteHelper.CHATS_TABLE, ChatsSQLiteHelper.COLUMN_CHATS_MESSAGE_USER_ID + "=?", new String[] {String.valueOf(userId)});

        db.close();
        helper.close();
    }





    /**
     * Inserts a new draft message to the CHAT_DRAFTS_TABLE in our local database.
     * If there is already a message to the given id, we will overwrite that message.
     * @param context            The application's context.
     * @param recipientUserId    The user that this draft is intended to.
     * @param text               The draft text.
     */
    public static void insertNewDraftToDatabase(Context context, long recipientUserId, String text) {
        ChatsSQLiteHelper helper = new ChatsSQLiteHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ChatsSQLiteHelper.COLUMN_CHAT_DRAFT_RECIPIENT_USER_ID, recipientUserId);
        values.put(ChatsSQLiteHelper.COLUMN_CHAT_DRAFT_TEXT, text);
        values.put(ChatsSQLiteHelper.COLUMN_CHAT_DRAFT_TIMESTAMP, System.currentTimeMillis());

        db.insertWithOnConflict(ChatsSQLiteHelper.CHAT_DRAFTS_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);        // Inserting the new message, and overwriting existing one.

        db.close();
        helper.close();
    }

    /**
     * Finds and returns the draft message that we intended to send to the given user.
     * @param context            The application's context
     * @param recipientUserId    The user id of the recipient.
     * @return The draft message's text, or an empty string if no draft message is in the database. This is never null.
     */
    public static String findDraftMessage(Context context, long recipientUserId) {
        String draft = "";

        ChatsSQLiteHelper helper = new ChatsSQLiteHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(ChatsSQLiteHelper.FIND_DRAFT_CHAT_MESSAGE, new String[] {String.valueOf(recipientUserId)});

        if (cursor.moveToFirst())
            draft = cursor.getString(cursor.getColumnIndex(ChatsSQLiteHelper.COLUMN_CHAT_DRAFT_TEXT));

        cursor.close();
        db.close();
        helper.close();

        return draft;
    }


    /**
     * Deletes a draft message from the CHAT_DRAFTS_TABLE in our local database.
     * @param context            The application's context.
     * @param recipientUserId    The user that this draft is intended to.
     */
    public static void deleteDraftFromDatabase(Context context, long recipientUserId) {
        ChatsSQLiteHelper helper = new ChatsSQLiteHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        db.delete(ChatsSQLiteHelper.CHAT_DRAFTS_TABLE, ChatsSQLiteHelper.COLUMN_CHAT_DRAFT_RECIPIENT_USER_ID + "=?", new String[]{String.valueOf(recipientUserId)});

        db.close();
        helper.close();
    }



    /**
     * Inserts a new message to the LOCAL database.
     * This function is called after we received a data from the server.
     * This function receives the database connection in order to improve the run-time for many inserts.
     *
	 * @param db 	                The connection to the local database.
     * @param messageId             The message id to insert to the database. Either a temp message id (negative number, saved in the shared preferences to avoid duplicates {@link com.cliqdbase.app.constants.SharedPreferencesConstants#SHARED_PREFERENCES_KEY_TEMP_MESSAGE_ID}, or a valid id received from our server
     * @param text                  The message text.
     * @param dateMillis            The message time. For outgoing messages this will be the current time on the device that the message was sent (System.currentTimeMillis()), For messages downloaded from the server, this will be the time that the message was inserted to the server's database.
     * @param status                The message status.
     * @param side                  The message side. 1 - I sent, 2 - I received.
     * @param otherSideUserId       The user id of the user in the side of the conversation.
     */
    public static void insertMessageToDatabase(SQLiteDatabase db, long messageId, String text, long dateMillis, char status, int side, long otherSideUserId) {
        ContentValues values = new ContentValues();
        values.put(ChatsSQLiteHelper.COLUMN_CHATS_MESSAGE_ID, messageId);
        values.put(ChatsSQLiteHelper.COLUMN_CHATS_MESSAGE_TEXT, text);
        values.put(ChatsSQLiteHelper.COLUMN_CHATS_MESSAGE_TIME, dateMillis);
        values.put(ChatsSQLiteHelper.COLUMN_CHATS_MESSAGE_STATUS, String.valueOf(status));
        values.put(ChatsSQLiteHelper.COLUMN_CHATS_MESSAGE_SIDE, side);
        values.put(ChatsSQLiteHelper.COLUMN_CHATS_MESSAGE_USER_ID, otherSideUserId);

        db.insert(ChatsSQLiteHelper.CHATS_TABLE, null, values);
    }

    /**
     * Inserts a new message to the LOCAL database.
     * This function is called after we sent a message to another user.
     * This function creates a database connection - not good for many inserts.
     *
     * @param context            The application's context - needed to create a ChatsSQLiteHelper object.
     * @param messageId          The message id.
     * @param text               The message text.
     * @param dateMillis         The UTC time the message was sent.
     * @param status             The message's status.
     * @param side               The message side. 1 - I sent, 2 - I received.
     * @param otherSideUserId    The user id of the user in the side of the conversation.
     */
    public static void insertMessageToDatabase(Context context, long messageId, String text, long dateMillis, char status, int side, long otherSideUserId) {
        ChatsSQLiteHelper helper = new ChatsSQLiteHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ChatsSQLiteHelper.COLUMN_CHATS_MESSAGE_ID, messageId);
        values.put(ChatsSQLiteHelper.COLUMN_CHATS_MESSAGE_TEXT, text);
        values.put(ChatsSQLiteHelper.COLUMN_CHATS_MESSAGE_TIME, dateMillis);
        values.put(ChatsSQLiteHelper.COLUMN_CHATS_MESSAGE_STATUS, String.valueOf(status));
        values.put(ChatsSQLiteHelper.COLUMN_CHATS_MESSAGE_SIDE, side);
        values.put(ChatsSQLiteHelper.COLUMN_CHATS_MESSAGE_USER_ID, otherSideUserId);

        db.insert(ChatsSQLiteHelper.CHATS_TABLE, null, values);


        db.close();
        helper.close();
    }


    /**
     * Inserts all the messages in the given ChatMessage.MessagesListToPhone to the messages table in the local database.
     * In edition, inserts the name of the user to the local users table
     * @param context     The context of the application.
     * @param messages    The data received from the server
     * @return An ArrayList of the unique user ids (needed in order to update the activity if necessary).
     */
    public static ArrayList<Long> insertMessagesAndUserDataToDatabase(Context context, ChatMessage.MessagesListToPhone messages) {
        HashSet<UserIdAndName> fromUsers = new HashSet<>();
        ArrayList<Long> userIds = new ArrayList<>();

        ChatsSQLiteHelper helper = new ChatsSQLiteHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        for (ChatMessage.ToPhone m : messages.getMessages()) {
            String firstName = m.getMessageOSUserFirstName();
            String lastName = m.getMessageOSUserLastName();
            long userId = m.getMessageOSUserId();

            fromUsers.add(new UserIdAndName(userId, firstName, lastName));

            //insertMessageToDatabase(db, m.getMessageId(), m.getMessageText(), convertUtcToLocal(m.getMessageUtcDate()), m.getMessageStatus(), m.getMessageSide(), m.getMessageOSUserId());
            insertMessageToDatabase(db, m.getMessageId(), m.getMessageText(), m.getMessageUtcDate(), m.getMessageStatus(), m.getMessageSide(), m.getMessageOSUserId());
        }

        for (UserIdAndName user : fromUsers) {
            userIds.add(user.getUserId());
            db.insertWithOnConflict(ChatsSQLiteHelper.USERS_TABLE, null, user.toContentValues(), SQLiteDatabase.CONFLICT_REPLACE);
        }

        db.close();
        helper.close();

        return userIds;
    }


    /**
     * Converts a time from UTC representation to match the devices current time zone.
     * @param utcDateMillis    The number of milliseconds since January 1st 1970, in a UTC timezone.
     * @return The number of milliseconds since January 1st 1970, in the device's current timezone.
     */
    /*private static long convertUtcToLocal(long utcDateMillis) {
        //Calendar calendar = Calendar.getInstance();
        //calendar.get(Calendar.DST_OFFSET);
        TimeZone tz = TimeZone.getDefault();
        return utcDateMillis + tz.getOffset(System.currentTimeMillis());
    }*/


    /**
     * This function is called after we finished sending the message to our server, and received the new id of the message (before it was a negative temporary value, see {@link #insertMessageToDatabase(SQLiteDatabase, long, String, long, char, int, long)})
     * @param context          The context from which this method was called.
     * @param tempMessageId    The temporary message id. This is the current id of the message. If there was an error while sending, and the message should keep it's negative id, pass -1 in this field.
     * @param newMessageId     The new id - the correct id. This is the id we received from the server.
     * @param status           The new status - 'E' if there was an error sending the message, or 'S' if the message had successfully sent to the server
     */
    public static void updateIdOfSentMessage(Context context, long tempMessageId, long newMessageId, char status) {
        ChatsSQLiteHelper helper = new ChatsSQLiteHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
		if (newMessageId != -1)
        	values.put(ChatsSQLiteHelper.COLUMN_CHATS_MESSAGE_ID, newMessageId);
        values.put(ChatsSQLiteHelper.COLUMN_CHATS_MESSAGE_STATUS, String.valueOf(status));

        String[] args = {String.valueOf(tempMessageId)};

        db.update(ChatsSQLiteHelper.CHATS_TABLE, values, ChatsSQLiteHelper.WHERE_CLAUSE_MESSAGE_ID, args);

        db.close();
    }


    /**
	 * This class represents a ChatMessage object that is being sent to a user's phone.
	 * 
	 * This class will be used when the user's phone receives a GCM notification of a new message (That he is the recipient of) in the database.
	 * After the GCM is received by the phone, the phone will start a service to download the messages.
	 * 
	 * see server project at servlets.GetMessages
	 * 
	 * @author Yuval
	 *
	 */
	public class ToPhone {
		private long messageId;
		private String messageText;
		private long messageUtcDate;
		private char messageStatus;
		private int messageSide;
		private long messageOSUserId;				// The user id of the other user. (OS = other side)
		private String messageOSUserFirstName;		// The user on the other side's first name
		private String messageOSUserLastName;		// The user on the other side's last name
		
		/**
		 * @param messageId						The message id.
		 * @param messageText					The message text. This is UTF-8 text.
		 * @param messageUtcDate				The message UTC date of the time the message was received by the server.
		 * @param messageStatus					The status of the message.
		 * @param messageSide					The side of the message. 1 - I sent, 2 - I received.
		 * @param messageOSUserId				The userId of the other side of the conversation.
		 * @param messageOSUserFirstName		The user's first name of the other side of the conversation.
		 * @param messageOSUserLastName			The user's last name of the other side of the conversation.
		 */
		public ToPhone(long messageId, String messageText, long messageUtcDate,
				char messageStatus, int messageSide, long messageOSUserId,
				String messageOSUserFirstName, String messageOSUserLastName) {
			this.messageId = messageId;
			this.messageText = messageText;
			this.messageUtcDate = messageUtcDate;
			this.messageStatus = messageStatus;
			this.messageSide = messageSide;
			this.messageOSUserId = messageOSUserId;
			this.messageOSUserFirstName = messageOSUserFirstName;
			this.messageOSUserLastName = messageOSUserLastName;
		}

		public long getMessageId() {
			return messageId;
		}

		public String getMessageText() {
			return messageText;
		}

		public long getMessageUtcDate() {
			return messageUtcDate;
		}

		public char getMessageStatus() {
			return messageStatus;
		}

		public int getMessageSide() {
			return messageSide;
		}

		public long getMessageOSUserId() {
			return messageOSUserId;
		}

		public String getMessageOSUserFirstName() {
			return messageOSUserFirstName;
		}

		public String getMessageOSUserLastName() {
			return messageOSUserLastName;
		}

	}
	

	/**
	 * This class represents a ChatMessage Object that being sent from the user's phone.
	 * 
	 * This class will be used when a user is sending a new chat message.
	 * see the server project at servlets.SendMessage
	 * 
	 * @author Yuval
	 */
	public final static class FromPhone {
        @SuppressWarnings("unused")
		private long recipientUserId;		// The userId of the recipient.		We don't need the sender user id, because we will receive that from the Session Cookie.
		private String messageText;			// The text of the new message. Can't be more than 250 characters (the phone verifies this). This message needs to be encrypted.

		
		/**
		 * @param recipientUserId		The user ID of the recipient.
		 * @param messageText			The text of the new message. This message needs to be encrypted in the server
		 */
		public FromPhone(long recipientUserId, String messageText) {
			this.recipientUserId = recipientUserId;
			this.messageText = messageText;
		}
		
		public String getMessageText() {
			return messageText;
		}
	}


	public static class MessagesListToPhone {
		private ArrayList<ToPhone> messages;
		private long newMaxMessageIdRecipient;
		private long newMaxMessageIdSender;

		public MessagesListToPhone(ArrayList<ToPhone> messages,
								   long newMaxMessageIdRecipient, long newMaxMessageIdSender) {
			this.messages = messages;
			this.newMaxMessageIdRecipient = newMaxMessageIdRecipient;
			this.newMaxMessageIdSender = newMaxMessageIdSender;
		}

		public ArrayList<ToPhone> getMessages() {
			return messages;
		}

		public long getNewMaxMessageIdRecipient() {
			return newMaxMessageIdRecipient;
		}

		public long getNewMaxMessageIdSender() {
			return newMaxMessageIdSender;
		}
	}

}