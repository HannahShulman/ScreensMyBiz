package com.cliqdbase.app.async.google_maps;

import java.util.List;

/**
 * Created by Yuval on 13/07/2015.
 *
 * @author Yuval Siev
 */
public interface GetLocationListAsyncCallback {
    void callback(List<String> addresses);
}
