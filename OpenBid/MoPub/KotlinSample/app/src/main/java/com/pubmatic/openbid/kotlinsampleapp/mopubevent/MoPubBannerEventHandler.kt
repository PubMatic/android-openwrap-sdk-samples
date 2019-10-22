package com.pubmatic.openbid.kotlinsampleapp.mopubevent

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.support.v4.content.ContextCompat
import android.util.Log
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubView
import com.pubmatic.sdk.common.POBAdSize
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.common.ui.POBBannerRendering
import com.pubmatic.sdk.openbid.banner.POBBannerEvent
import com.pubmatic.sdk.openbid.banner.POBBannerEventListener
import com.pubmatic.sdk.openbid.core.POBBid
import java.util.*

/**
 * This class implements the communication between the OpenBid SDK and the MoPub SDK for a given ad
 * unit. It implements the PubMatic's OpenBid interface. POB SDK notifies (using OpenBid interface)
 * to make a request to MoPub SDK and pass the targeting parameters. This class also creates the MoPub's
 * MoPubView, initialize it and listen for the callback methods. And pass the MoPub ad event to
 * OpenBid SDK via POBBannerEventListener.
 */
class MoPubBannerEventHandler
/**
 * Constructor
 *
 * @param context  value of contextø
 * @param adUnitId SDP ad unit ID
 * @param pobAdSize Ad size for requesting Banner Ad
 */
(val context: Context, adUnitId: String, val pobAdSize: POBAdSize) : POBBannerEvent, MoPubView.BannerAdListener {

    /**
     * Interface to get the MoPub Banner ad view object, to configure the properties.
     */
    interface MoPubConfigListener {
        /**
         * This method is called before event handler makes ad request call to MoPub SDK. It passes
         * MoPub ad view which will be used to make an ad request. Publisher can configure the ad
         * request properties on the provided objects.
         * @param adView MoPub Banner ad view
         */
        fun configure(adView: MoPubView)
    }

    /**
     * MoPub Banner ad view
     */
    private var moPubView: MoPubView? = null

    /**
     * Config listener to check if publisher want to config properties in MoPub ad
     */
    private var mopubConfigListener: MoPubConfigListener? = null

    /**
     * Interface to pass the MoPub ad event to OpenBid SDK
     */
    private var eventListener: POBBannerEventListener? = null


    init {
        moPubView = MoPubView(context)
        moPubView?.setAdUnitId(adUnitId)

        // DO NOT REMOVE/OVERRIDE BELOW LISTENER/PROPERTY
        moPubView?.setBannerAdListener(this)
        moPubView?.setAutorefreshEnabled(false)
    }

    /**
     * Sets the Data listener object. Publisher should implement the MoPubConfigListener and
     * override its method only when publisher needs to set the targeting parameters over MoPub
     * banner ad view.
     *
     * @param listener MoPub config listener
     */
    fun setConfigListener(listener: MoPubConfigListener) {
        mopubConfigListener = listener
    }

    // ------- Overridden methods from POBBannerEvent -------
    override fun requestAd(bid: POBBid?) {

        // If network is not available, SDK is not getting error callbacks from MoPub
        // For that we are checking the network initially and throwing error callback.
        if (!isConnected(context)) {
            val error = POBError(POBError.NETWORK_ERROR, "Network not available!")
            Log.e(TAG, error.toString())
            eventListener?.onFailed(error)
            return
        }

        // Check if publisher want to set any targeting data
        moPubView?.let { mopubConfigListener?.configure(it) }

        // NOTE: Please do not remove this code. Need to reset MoPub banner listener to
        // MoPubBannerEventHandler as these are used by MoPubBannerEventHandler internally.
        // Changing the mopub delegate to other instance may break the callback mechanism and may
        // hamper the banner refresh mechanism.
        if (moPubView?.bannerAdListener != this) {
            moPubView?.setBannerAdListener(this)
            Log.w(TAG, "Resetting MoPub banner listener to MoPubBannerEventHandler as these are" +
                    " used by MoPubBannerEventHandler internally.")
        }
        
        if (null != bid) {
            // Logging details of bid objects for debug purpose.
            Log.d(TAG, bid.toString())
            val keywords = StringBuilder()
            val targeting = bid.targetingInfo
            if (targeting != null && !targeting.isEmpty()) {
                // using for-each loop for iteration over Map.entrySet()
                val targetingSet = targeting.entries.iterator()
                while (targetingSet.hasNext()) {
                    val entry = targetingSet.next()
                    keywords.append(entry.key).append(":").append(entry.value)
                    if (targetingSet.hasNext()) {
                        keywords.append(",")
                    }
                }

                // Check if keywords is configured by publisher, append it
                val publisherKeywords = moPubView?.getKeywords()
                if (publisherKeywords != null && !"".equals(publisherKeywords, ignoreCase = true)) {
                    keywords.append(",")
                    keywords.append(publisherKeywords)
                }
                Log.d(TAG, "MoPub requestBuilder :" + keywords.toString())
                moPubView?.setKeywords(keywords.toString())

                // No need to set localExtras when status is 0, as Pubmatic line item will not get
                // picked up
                if (bid.status == 1) {
                    val localMap = HashMap<String, Any>()
                    localMap[POBBannerCustomEvent.BID_KEY] = bid

                    // Check if any local extra is configured by publisher, append it
                    val publisherLocalExtra = moPubView?.getLocalExtras()
                    if (publisherLocalExtra != null && !publisherLocalExtra.isEmpty()) {
                        localMap.putAll(publisherLocalExtra)
                    }
                    moPubView?.setLocalExtras(localMap)
                }
            }

        }
        // Load MoPub ad request
        moPubView?.loadAd()
    }

    override fun setEventListener(listener: POBBannerEventListener) {
        eventListener = listener
    }

    override fun getRenderer(partnerName: String): POBBannerRendering? {
        return null
    }

    override fun getAdSize(): POBAdSize {
        return POBAdSize(moPubView!!.getAdWidth(), moPubView!!.getAdHeight())
    }

    override fun requestedAdSizes(): Array<POBAdSize?>? {
        val pobAdSizes = arrayOfNulls<POBAdSize>(1)
        pobAdSizes[0] = pobAdSize
        return pobAdSizes
    }

    override fun destroy() {
        moPubView?.destroy()
        moPubView = null
        eventListener = null

    }

    override fun onBannerLoaded(banner: MoPubView) {
        Log.d(TAG, "onBannerLoaded")
        eventListener?.onAdServerWin(banner)
    }

    override fun onBannerFailed(banner: MoPubView, errorCode: MoPubErrorCode) {
        Log.d(TAG, "onBannerFailed")
        val error: POBError
        if (eventListener != null) {
            when (errorCode) {
                MoPubErrorCode.NO_FILL, MoPubErrorCode.NETWORK_NO_FILL -> error = POBError(POBError.NO_ADS_AVAILABLE, errorCode.toString())
                MoPubErrorCode.NO_CONNECTION, MoPubErrorCode.NETWORK_TIMEOUT -> error = POBError(POBError.NETWORK_ERROR, errorCode.toString())
                MoPubErrorCode.SERVER_ERROR -> error = POBError(POBError.SERVER_ERROR, errorCode.toString())
                MoPubErrorCode.CANCELLED -> error = POBError(POBError.REQUEST_CANCELLED, errorCode.toString())
                MoPubErrorCode.NETWORK_INVALID_STATE -> error = POBError(POBError.INVALID_REQUEST, errorCode.toString())
                else -> error = POBError(POBError.INTERNAL_ERROR, errorCode.toString())
            }
            eventListener?.onFailed(error)
        } else {
            Log.e(TAG, "Can not call failure callback, POBBannerEventListener reference null. MoPub error:$errorCode")
        }

    }


    override fun onBannerClicked(banner: MoPubView) {
        eventListener?.onAdClick()
    }

    override fun onBannerExpanded(banner: MoPubView) {
        eventListener?.onAdOpened()

    }

    override fun onBannerCollapsed(banner: MoPubView) {
        eventListener?.onAdClosed()
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

        private val TAG = "MoPubBannerEvent"
    }

}
