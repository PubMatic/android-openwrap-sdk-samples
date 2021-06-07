package com.pubmatic.openwrap.listapp.adloader

/**
 * Interface to be implemented by a class/activity to get the notification
 * after the responses are received from all the bidders.
 */
interface BiddingManagerListener {
    /**
     * Manager class uses this method to notify once response is received from all the
     * registered bidders of different partners and at least one of them is a
     * success response with non-null response body.
     *
     * @param response  Targeting information map. This map is in the form of
     * {partnerName1: response1. partnerName2: response2, ...}
     * response1, response2 etc. are key-value pairs of targeting information.
     */
    fun onResponseReceived(response: MutableMap<String?, Map<String?, List<String?>?>?>?)

    /**
     * Manager class uses this method to notify once response is received from all the
     * registered bidders of different partners and all of them have failed with error.
     * This callback method will also be used in cases, where bidders have responded with
     * success but no single non-null response body is available.
     *
     * @param error     Error object
     */
    fun onResponseFailed(error: Any?)
}