package com.cliqdbase.app.constants;

/**
 * Created by Yuval on 26/05/2015.
 *
 * @author Yuval Siev
 */
public interface ServerUrlConstants {

    /**
     * The server's host
    */
    //String SERVER_HOST = "http://10.0.0.2:8080/CliqdbaseServer";
    String SERVER_HOST = "http://52.88.81.89:8080/CliqdbaseServer";      // Shlomi
    //String SERVER_HOST = "http://52.25.213.172";      // yuval

    /**
     * The host of the socket server for venue-chat.
     */
    String SOCKET_SERVER_HOST = "52.88.81.89:8080";        // shlomi
    //String SOCKET_SERVER_HOST = "52.25.213.172";      // yuval

    /**
     * The port of the venue-chat socket server
     */
    int SOCKET_SERVER_PORT = 4444;


    /**
     * The URL of the regular login servlet
     */
    String CLIQDBASE_LOGIN_URL = "/Login/Cliqdbase";
    /**
     * The URL of the regular SignUp (register) servlet
     */
    String CLIQDBASE_SIGN_UP_URL = "/Register/Cliqdbase";

    /**
     * The URL of the facebook login servlet
     */
    String FACEBOOK_LOGIN_URL = "/Login/Facebook";

    /**
     * The URL of the facebook login servlet
     */
    String FACEBOOK_SIGN_UP_URL = "/Register/Facebook";

    /**
     * The URL of the logout servlet
     */
    String LOGOUT_URL = "/Logout";

    /**
     * The URL of the servlet that supply the cities list.
     */
    String GET_CITIES_LIST = "/GetCitiesList";

    /**
     * The URL of the upload image servlet
     */
    String UPLOAD_PROFILE_IMAGE = "/ChangeProfilePic";

    String REMOVE_PROFILE_IMAGE = "/RemoveProfilePic";

    /**
     * The URL of the servlet that will return the data of my (my = the logged in user's) profile
     */
    String GET_MY_USER_PROFILE = "/GetProfile";

    /**
     * The URL of the servlet that will return the data of a user's profile based on the given id.
     */
    String GET_USER_PROFILE = "/GetProfile/user/";

    /**
     * The URL that downloads the full (not thumbnail) profile picture of a given user.
     */
    String GET_USER_PROFILE_PICTURE = "/GetProfilePhoto";

    /**
     * The URL of the servlet in which the user will be able to update his profile data.
     */
    String UPDATE_USER_PROFILE = "/UpdateProfile";

    /**
     * The URL of the servlet that receives the messages from the server.
     */
    String GET_CHAT_MESSAGES = "/GetMessages";

    /**
     * The URL of the servlet that sends a new chat message.
     */
    String SEND_CHAT_MESSAGE = "/SendMessage";

    /**
     * The URL of the servlet that confirms the user's email.
     * Before sending, you must append this url with the confirmation code.
     */
    String CONFIRM_EMAIL = "/confirm/";

    /**
     * The URL of the servlet that send a 'reset-password' email to the user.
     */
    String FORGOT_PASS_SERVLET = "/ForgotPasswordServlet";


    /**
     * The URL of the servlet that resets the password after a user clicks on the reset-password link he received via mail.
     */
    String RESET_PASS_SERVLET = "/ResetPasswordServlet";

    /**
     * The URL of the servlet that allows the user to change his password manually.
     */
    String CHANGE_PASS_SERVLET = "/ChangePasswordServlet";

    /**
     * The URL of the servlet that returns a filter with the user's appearance data.
     */
    String GET_SEARCH_FILTER_INITIAL_DATA = "/SearchFilterGetInitialDataServlet";

    /**
     * The url for searching cliqs
     */
    String SEARCH_CLIQ_SERVLET = "/SearchCliqServlet";

    /**
     * The url of the servlet that manages the guest user registration.
     */
    String GUEST_USER_MANAGER = "/GuestRegistrationManager";

    /**
     * The url of the servlet that will return the data to display on the sign up activity, when a guest is trying to sign up.
     */
    String GET_GUEST_INFO_FOR_SIGN_UP = "/GetGuestInfoForSignUp";
}
