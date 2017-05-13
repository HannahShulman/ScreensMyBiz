package com.cliqdbase.app.venue_chat;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.cliqdbase.app.general.Common;
import com.cliqdbase.app.constants.IntentConstants;
import com.cliqdbase.app.constants.ServerUrlConstants;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by Yuval on 27/07/2015.
 *
 * @author Yuval Siev
 */
public class SocketThread extends Thread {

    private static final String CLOSE_CONNECTION_COMMAND = "command:close_connection";


    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;

    private String venueName;

    private Context context;

    public SocketThread(Context context, String venueName) {
        this.context = context;
        this.venueName = venueName;
    }


    public void run() {
        try {
            this.socket = new Socket(ServerUrlConstants.SOCKET_SERVER_HOST, ServerUrlConstants.SOCKET_SERVER_PORT);
            this.output = new PrintWriter(socket.getOutputStream(), true);
            this.input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

            this.output.println(createFirstMessage());

            Log.d("yuval", "Connected to socket server");

            String inputLine;
            Intent intent;
            while(!Thread.interrupted()) {
                try {
                    if ((inputLine = this.input.readLine()) != null) {
                        Message message = Message.getMessageFromJson(inputLine);
                        intent = new Intent(IntentConstants.INTENT_FILTER_VENUE_CHAT_SOCKET_BROADCAST);
                        intent.putExtra(IntentConstants.INTENT_EXTRA_VENUE_CHAT_SOCKET_MESSAGE_KEY, message);
                        sendBroadcastToUiThread(intent);
                    }
                } catch (SocketException e) { break; }
            }

        } catch (IOException e) {
            e.printStackTrace();

            // Sending a broadcast to the UI thread without the message extra - Therefore this will be null.
            Intent intent = new Intent(IntentConstants.INTENT_FILTER_VENUE_CHAT_SOCKET_BROADCAST);
            sendBroadcastToUiThread(intent);

            closeConnection();
        }
    }


    private void sendBroadcastToUiThread(Intent intent) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void sendMessageToServer(String messageText) {
        if (this.output != null)
            this.output.println("input:" + messageText);
    }


    private String createFirstMessage() {
        String message = "command:";

        JsonObject json = new JsonObject();
        json.addProperty("sessionId", Common.getSessionId(this.context));
        json.addProperty("venue", this.venueName);

        return message + json.toString();
    }


    public void closeConnection() {
        Log.d("yuval", "Connection to socket server closed.");
        interrupt();
        this.output.println(CLOSE_CONNECTION_COMMAND);
        this.output.close();
        try { this.input.close(); } catch(Exception e1) {/* Ignored */}
        try { this.socket.close(); } catch(Exception e1) {/* Ignored */}
    }

}
