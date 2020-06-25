package com.kobrakid.retroachievements

/**
 * Must be implemented by any class that intends on accessing the RetroAchievements API.
 */
@Deprecated("Phasing out")
interface RAAPICallback {
    /**
     * The callback function to be run upon response from the RA API.
     *
     * @param responseCode The corresponding response code, which informs a callback on what kind of
     * API call was made.
     * @param response     The raw String response that was retrieved from the API call.
     */
    fun callback(responseCode: Int, response: String)
}