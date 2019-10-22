package com.pubmatic.openbid.kotlinsampleapp.mopubevent

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.support.v4.content.ContextCompat
import android.util.Log
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubInterstitial
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.common.ui.POBInterstitialRendering
import com.pubmatic.sdk.openbid.core.POBBid
import com.pubmatic.sdk.openbid.interstitial.POBInterstitialEvent
import com.pubmatic.sdk.openbid.interstitial.POBInterstitialEventListener

/**
 * This class implements the communication between the OpenBid SDK and the MoPub SDK for a given ad
 * unit. It implements the PubMatic's Wrapper interface. PM SDK notifies (using wrapper interface)
 * to make a request to MoPub SDK and pass the targeting parameters. This class also creates the MoPub's
 * MoPubInterstitial, initialize it and listen for the callback methods. And pass the MoPub ad event to
 * OpenBid SDK via POBInterstitialEventListener.
 */
open class MoPubInterstitialEventHandler(
        /**
         * Activity context on which interstitial Ad will get displayed.
         */
        private val context: Activity,
        /**
         * MoPub Interstitial Ad unit id.
         */
        private val mopubAdUnitId: String) : POBInterstitialEvent, MoPubInterstitial.InterstitialAdListener {

    /**
     * Interface to get the MoPub Interstitial ad object, to configure the properties.
     */
    interface MoPubConfigListener {
        /**
         * This method is called before event handler makes ad request call to MoPub SDK. It passes
         * MoPub ad object which will be used to make an ad request. Publisher can configure the ad
         * request properties on the provided objects.
         * @param ad MoPub Interstitial ad
         */
        fun configure(ad: MoPubInterstitial)
    }

    /**
     * Interface to pass the MoPub ad event to OpenBid SDK
     */
    private var eventListener: POBInterstitialEventListener? = null

    /**
     * Config listener to check if publisher want to config properties in MoPub ad
     */
    private var mopubConfigListener: MoPubConfigListener? = null

    /**
     * MoPub Interstitial Ad instance
     */
    private var moPubInterstitial: MoPubInterstitial? = null

    private fun initializeMoPubAd() {
        destroyMoPubAd()
        moPubInterstitial = MoPubInterstitial(context, mopubAdUnitId)

        // DO NOT REMOVE/OVERRIDE BELOW LISTENER
        moPubInterstitial?.setInterstitialAdListener(this)
    }

    /**
     * Sets the Data listener object. Publisher should implement the MoPubConfigListener and
     * override its method only when publisher needs to set the targeting parameters over MoPub
     * interstitial ad view.
     *
     * @param listener MoPub config listener
     */
    fun setConfigListener(listener: MoPubConfigListener) {
        mopubConfigListener = listener
    }

    private fun destroyMoPubAd() {
        if (moPubInterstitial != null) {
            moPubInterstitial?.destroy()
            moPubInterstitial = null
        }
    }

    //<editor-fold desc="POBInterstitialEvent overridden methods">
    override fun requestAd(bid: POBBid?) {

        // If network is not available, SDK is not getting error callbacks from MoPub
        // For that we are checking the network initially and throwing error callback.
        if (!isConnected(context)) {
            val error = POBError(POBError.NETWORK_ERROR, "Network not available!")
            Log.e(MoPubInterstitialEventHandler.TAG, error.toString())
            eventListener?.onFailed(error)
            return
        }

        initializeMoPubAd()
        var targetingParams: StringBuilder? = null

        // Check if publisher want to set any targeting data
        moPubInterstitial?.let { mopubConfigListener?.configure(it) }

        // NOTE: Please do not remove this code. Need to reset MoPub interstitial listener to
        // MoPubInterstitialEventHandler as these are used by MoPubInterstitialEventHandler internally.
        if (moPubInterstitial?.interstitialAdListener != this) {
            moPubInterstitial?.setInterstitialAdListener(this)
            Log.w(TAG, "Resetting MoPub interstitial interstitial to MoPubInterstitialEventHandler" +
                    " as these are used by MoPubInterstitialEventHandler internally.")
        }

        if (null != bid) {
            // Logging details of bid objects for debug purpose.
            Log.d(TAG, bid.toString())
            targetingParams = getTargeting(bid)

            //Pass bid object to MoPub custom event for rendering PubMatic Ad
            val localExtra = HashMap<String, Any>()
            localExtra.put(PUBMATIC_BID_KEY, bid)
            eventListener?.customData?.let {
                localExtra.putAll(it)
            }

            // Check if any local extra is configured by publisher, append it
            val publisherLocalExtra = moPubInterstitial?.getLocalExtras()
            if (publisherLocalExtra != null && !publisherLocalExtra.isEmpty()) {
                localExtra.putAll(publisherLocalExtra)
            }
            moPubInterstitial?.setLocalExtras(localExtra)
        }
        //Add custom targeting parameters to MoPub Ad request
        if (targetingParams != null) {

            // Check if keywords is configured by publisher, append it
            val publisherKeywords = moPubInterstitial?.getKeywords()
            if (publisherKeywords != null && !"".equals(publisherKeywords, ignoreCase = true)) {
                targetingParams.append(",")
                targetingParams.append(publisherKeywords)
            }
            moPubInterstitial?.setKeywords(targetingParams.toString())
        }
        // Load MoPub ad request
        moPubInterstitial?.load()
    }

    private fun getTargeting(bid: POBBid): StringBuilder{
        val targetingParams = StringBuilder()
        val targeting = bid.targetingInfo
        if (targeting != null && !targeting.isEmpty()) {
            // using iterator for iteration over Map.entrySet()
            val iterator = targeting.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                targetingParams.append(entry.key + ":" + entry.value)
                if (iterator.hasNext()) {
                    targetingParams.append(",")
                }
            }
        }
        return targetingParams
    }

    override fun setEventListener(listener: POBInterstitialEventListener) {
        this.eventListener = listener
    }

    override fun getRenderer(partnerName: String): POBInterstitialRendering? {
        return null
    }

    override fun show() {
        if (moPubInterstitial != null && moPubInterstitial?.isReady() == true) {
            moPubInterstitial?.show()
        }else{
            val errMsg = "MoPub SDK is not ready to show Interstitial Ad."
            eventListener?.onFailed(POBError(POBError.INTERSTITIAL_NOT_READY, errMsg))
            Log.e(TAG, errMsg)
        }
    }

    override fun destroy() {
        destroyMoPubAd()
    }
    //</editor-fold>

    //<editor-fold desc="InterstitialAdListener overridden methods">
    override fun onInterstitialLoaded(interstitial: MoPubInterstitial) {
        eventListener?.onAdServerWin()
    }

    override fun onInterstitialFailed(interstitial: MoPubInterstitial, errorCode: MoPubErrorCode) {
        if (null != eventListener) {
            when (errorCode) {
                MoPubErrorCode.NO_FILL, MoPubErrorCode.NETWORK_NO_FILL -> eventListener?.onFailed(POBError(POBError.NO_ADS_AVAILABLE, errorCode.toString()))
                MoPubErrorCode.NO_CONNECTION, MoPubErrorCode.NETWORK_TIMEOUT -> eventListener?.onFailed(POBError(POBError.NETWORK_ERROR, errorCode.toString()))
                MoPubErrorCode.SERVER_ERROR -> eventListener?.onFailed(POBError(POBError.SERVER_ERROR, errorCode.toString()))
                MoPubErrorCode.CANCELLED -> eventListener?.onFailed(POBError(POBError.REQUEST_CANCELLED, errorCode.toString()))
                else -> eventListener?.onFailed(POBError(POBError.INTERNAL_ERROR, errorCode.toString()))
            }
        }else{
            Log.e(TAG, "Can not call failure callback, POBInterstitialEventListener reference null. MoPub error:$errorCode")
        }
    }

    override fun onInterstitialShown(interstitial: MoPubInterstitial) {
        eventListener?.onAdOpened()
    }

    override fun onInterstitialClicked(interstitial: MoPubInterstitial) {
        eventListener?.onAdClick()
    }

    override fun onInterstitialDismissed(interstitial: MoPubInterstitial) {
        eventListener?.onAdClosed()
        destroy()
    }

    /**
     * Method to check network connection available
     * @param context android context
     * @return true if network is available else returns false
     */
    private fun isConnected(context: Context): Boolean {
        val cm = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {
            val activeNetwork = cm?.activeNetworkInfo
            return activeNetwork != null && activeNetwork.isConnected
        }
        return false
    }

    companion object {

        private val TAG = "MoPubInterstitialEvent"

        /**
         * Key to pass the PubMatic bid instance to CustomEventInterstitial
         */
        var PUBMATIC_BID_KEY = "POBBid"
    }
    //</editor-fold>

}
