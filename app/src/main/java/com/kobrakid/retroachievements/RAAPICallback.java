package com.kobrakid.retroachievements;

public interface RAAPICallback {

    /**
     * The callback function to be run upon response from the RA API.
     *
     * @param responseCode The corresponding response code, which informs a callback on what kind of
     *                     API call was made.
     * @param response     The raw String response that was retrieved from the API call.
     */
    void callback(int responseCode, String response);
}
