package com.cliqdbase.app.async.server;

/**
 * Created by Yuval on 11/03/2015.
 *
 * @author Yuval Siev
 */
public interface AsyncResponse_Server {
    // Callback after the server finished the request.
    void onServerResponse(long taskCode, int httpResultCode, String data);
}
