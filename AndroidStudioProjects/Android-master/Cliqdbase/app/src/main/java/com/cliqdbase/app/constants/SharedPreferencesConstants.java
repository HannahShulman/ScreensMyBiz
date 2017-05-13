package com.cliqdbase.app.constants;

/**
 * Created by Yuval on 26/05/2015.
 *
 * @author Yuval Siev
 */
public interface SharedPreferencesConstants {

    /**
     * The global shared preferences file name
     */
    String GLOBAL_SHARED_PREFERENCES_FILE_NAME = "cliqdbase_sp";

    /**
     * The key to the value of the cookie that contain the sessionId in the shared preferences
     */
    String SHARED_PREFERENCES_KEY_COOKIE_SESSION_ID = "sessionId";

    /**
     * The key to the value of the user's id.
     */
    String SHARED_PREFERENCES_KEY_LOGGED_IN_USER_ID = "logged_in_user_id";

    /**
     * The key in the shared preferences for the profile image path.
     * This will reduce traffic between the user and the server, because we won't need to download the image each and every time we enter our profile.
     */
    String SHARED_PREFERENCES_KEY_PROFILE_IMAGE_PATH = "profile_image_uri";

    /**
     * This key will hold a boolean value - whether the user logged in using facebook or not.
     */
    String SHARED_PREFERENCES_KEY_FACEBOOK_LOGIN = "facebookLogin";

    /**
     * This key will hold the value of the play services registration key.
     * This key is used in the GCM.
     */
    String SHARED_PREFERENCES_KEY_REG_ID = "registration_id";

    /**
     * This key will hold the app version.
     * Changes in app version can cause different registration ids in the CGM.
     * Therefore, we must keep the app version, and if different than the current one installed, we will re-register the device to get the updated registration id.
     */
    String SHARED_PREFERENCES_KEY_APP_VERSION = "appVersion";

    /**
     * This key will hold the temp message id.
     * After a user is sending a message, the message needs to have an id. This id will be the temp id until the message will be sent to the server and receive it's correct id.
     */
    String SHARED_PREFERENCES_KEY_TEMP_MESSAGE_ID = "tempMessageId";

    /**
     * This key will hold the maximum message id exists in our local database, when the user is the sender of the message.
     * When we will ask the server for new messages, we will add this number, in order to receive only new messages, and not all of them.
     */
    String SHARED_PREFERENCES_KEY_MAX_MESSAGE_ID_SENDER = "maxMessageIdSender";

    /**
     * This key will hold the maximum message id exists in our local database, when the user is the recipient of the message.
     * When we will ask the server for new messages, we will add this number, in order to receive only new messages, and not all of them.
     */
    String SHARED_PREFERENCES_KEY_MAX_MESSAGE_ID_RECIPIENT = "maxMessageIdRecipient";


    String SHARED_PREFERENCES_KEY_USER_DATA_EMPTY = "user_data_empty";

    /**
     * This boolean key will hold true if the user prefers metric units (cm), and false if the user prefers imperial units (inches + feet)
     */
    String SHARED_PREFERENCES_KEY_PREFERRED_UNITS_METRIC = "user_preferred_units_metric";

    /**
     * This boolean value will hold true if the user currently logged in is a guest user, and false if the user has logged in normally.
     */
    String SHARED_PREFERENCES_KEY_LOGGED_IN_AS_GUEST = "user_logged_in_as_guest";

}
