package com.cliqdbase.app.constants;

import com.cliqdbase.app._fragments.ProfileFragment;
import com.cliqdbase.app._activities.UpdateProfileActivity;

/**
 * Created by Yuval on 26/05/2015.
 *
 * @author Yuval Siev
 */
public interface IntentConstants {

    String INTENT_EXTRA_PREFIX = "com.cliqdbase.app.";



    /**
     * The key in the intent extras for sign up. (used in the ChangeProfilePic activity)
     */
    String INTENT_EXTRA_SIGNUP = INTENT_EXTRA_PREFIX + "signUp";

    /**
     * The key in the intent extras for the user id. Used when attempting to view a user's profile.
     */
    String INTENT_EXTRA_USER_ID = INTENT_EXTRA_PREFIX + "userId";

    /**
     * The key in the intent extras. This key holds true if the user updated his profile, and false otherwise.
     * used in ProfileActivity and UpdateProfileActivity as the intent result.
     * @see ProfileFragment
     * @see UpdateProfileActivity
     */
    String INTENT_EXTRA_PROFILE_UPDATED = INTENT_EXTRA_PREFIX + "profileUpdated";


    /**
     * The key in the intent extras that will hold the chat id.
     * Used upon click on a conversation item in the ChatMainActivity's list, in order to see what conversation is being clicked
     */
    String INTENT_EXTRA_CHAT_USER_ID = INTENT_EXTRA_PREFIX + "chatUserId";

    /**
     * This intent filter is used for the locally-registered broadcast receivers in ChatMainActivity and ChatConversationActivity.
     * These receivers are needed to communicate between the GcmIntentService and the running activities.
     * Without them, we won't be able to refresh the adapter in the aforementioned activities.
     */
    String INTENT_FILTER_MESSAGES_DATABASE_UPDATED = INTENT_EXTRA_PREFIX + "MESSAGES_IN_DATABASE_UPDATED";

    /**
     * This extra will hold an array of long values of user ids.
     * An ID will be on this array if and only if we downloaded a new message with this ID.
     *
     * @see com.cliqdbase.app.chats_stuff.GcmIntentService
     */
    String INTENT_EXTRA_USER_IDS_OF_NEW_MESSAGES = INTENT_EXTRA_PREFIX + "user_ids_list";


    /**
     * This extra will hold the user id of the recipient of an outgoing chat message.
     * This is needed because the data of the outgoing message is sent to an intent service.
     */
    String INTENT_EXTRA_OUTGOING_CHAT_MESSAGE_USER_ID = INTENT_EXTRA_PREFIX + "outgoing_chat_message_recipient_user_id";

    /**
     * This extra will hold the text of a message of an outgoing chat message.
     * This is needed because the data of the outgoing message is sent to an intent service.
     */
    String INTENT_EXTRA_OUTGOING_CHAT_MESSAGE_TEXT = INTENT_EXTRA_PREFIX + "outgoing_chat_message_text";


    /**
     * The intent filter for the locally-registered broadcast receiver.
     * When a new line is received from the server, we will notify the UI thread by a broadcast receiver with this intent filter.
     */
    String INTENT_FILTER_VENUE_CHAT_SOCKET_BROADCAST = INTENT_EXTRA_PREFIX + "venue_chat_socket_broadcast";

    /**
     * This key will hold a Message object that we will send to the UI thread to display to the user.
     * If this is null, the connection to the socket server has been lost.
     */
    String INTENT_EXTRA_VENUE_CHAT_SOCKET_MESSAGE_KEY = INTENT_EXTRA_PREFIX + "venue_chat_socket_message";

    /**
     * This key in the intent extra will hold the venue name to which we want to connect in the venue chat.
     */
    String INTENT_EXTRA_VENUE_CHAT_NAME_KEY = INTENT_EXTRA_PREFIX + "venue_chat_name";

    /**
     * This boolean key in the intent will indicate that a guest user wants to sign up.
     */
    String INTENT_EXTRA_GUEST_WANT_TO_SIGN_UP = INTENT_EXTRA_PREFIX + "guest_sign_up";
}
