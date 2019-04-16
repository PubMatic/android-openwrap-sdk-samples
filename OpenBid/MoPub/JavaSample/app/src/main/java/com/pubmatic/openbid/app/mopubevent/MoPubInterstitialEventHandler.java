package com.pubmatic.openbid.app.mopubevent;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.pubmatic.sdk.common.POBError;
import com.pubmatic.sdk.openbid.core.POBBid;
import com.pubmatic.sdk.openbid.interstitial.POBInterstitialEvent;
import com.pubmatic.sdk.openbid.interstitial.POBInterstitialEventListener;
import com.pubmatic.sdk.webrendering.ui.POBInterstitialRendering;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class implements the communication between the OpenBid SDK and the MoPub SDK for a given ad
 * unit. It implements the PubMatic's Wrapper interface. PM SDK notifies (using wrapper interface)
 * to make a request to MoPub SDK and pass the targeting parameters. This class also creates the MoPub's
 * MoPubInterstitial, initialize it and listen for the callback methods. And pass the MoPub ad event to
 * OpenBid SDK via POBInterstitialEventListener.
 */
public class MoPubInterstitialEventHandler implements POBInterstitialEvent, MoPubInterstitial.InterstitialAdListener {

    private static final String TAG = "MoPubInterstitialEvent";
    /**
     * Key to pass the PubMatic bid instance to CustomEventInterstitial
     */
    public static String PUBMATIC_BID_KEY = "POBBid";
    /**
     * Config listener to check if publisher want to config properties in MoPub ad
     */
    private MoPubConfigListener mopubConfigListener;
    /**
     * Interface to pass the MoPub ad event to OpenBid SDK
     */
    private POBInterstitialEventListener eventListener;
    /**
     * MoPub Interstitial Ad instance
     */
    private MoPubInterstitial moPubInterstitial;
    /**
     * MoPub Interstitial Ad unit id.
     */
    private String mopubAdUnitId;
    /**
     * Activity context on which interstitial Ad will get displayed.
     */
    private Activity context;

    public MoPubInterstitialEventHandler(Activity context, String adUnitId) {
        this.context = context;
        this.mopubAdUnitId = adUnitId;

    }

    /**
     * Sets the Data listener object. Publisher should implement the MoPubConfigListener and
     * override its method only when publisher needs to set the targeting parameters over MoPub
     * interstitial ad view.
     *
     * @param listener MoPub config listener
     */
    public void setConfigListener(MoPubConfigListener listener) {
        mopubConfigListener = listener;
    }

    private void initializeMoPubAd() {
        destroyMoPubAd();
        moPubInterstitial = new MoPubInterstitial(context, mopubAdUnitId);

        // DO NOT REMOVE/OVERRIDE BELOW LISTENER
        moPubInterstitial.setInterstitialAdListener(this);
    }

    private void destroyMoPubAd() {
        if (moPubInterstitial != null) {
            moPubInterstitial.destroy();
            moPubInterstitial = null;
        }
    }

    //<editor-fold desc="POBInterstitialEvent overridden methods">
    @Override
    public void requestAd(POBBid bid) {

        // If network is not available, SDK is not getting error callbacks from MoPub
        // For that we are checking the network initially and throwing error callback.
        if(!isConnected(context)){
            POBError error = new POBError(POBError.NETWORK_ERROR, "Network not available!");
            Log.e(TAG, error.toString());
            if(null != eventListener){
                eventListener.onFailed(error);
            }
            return;
        }

        initializeMoPubAd();
        StringBuilder targetingParams = null;

        // Check if publisher want to set any targeting data
        if (mopubConfigListener != null) {
            mopubConfigListener.configure(moPubInterstitial);
        }

        if (moPubInterstitial.getInterstitialAdListener() != this) {
            Log.w(TAG, "Do not set MoPub listener. This is used by MoPubInterstitialEventHandler internally.");
        }

        if (null != bid) {
            // Logging details of bid objects for debug purpose.
            Log.d(TAG, bid.toString());
            targetingParams = generateTargeting(bid);
            // Pass bid object to MoPub custom event for rendering PubMatic Ad
            Map<String, Object> localExtra = new HashMap<>();
            localExtra.put(PUBMATIC_BID_KEY, bid);
            // put customData to localExtra

            if(null != eventListener && null != eventListener.getCustomData()){
                localExtra.putAll(eventListener.getCustomData());
            }

            // Check if any local extra is configured by publisher, append it
            Map<String, Object> publisherLocalExtra = moPubInterstitial.getLocalExtras();
            if (publisherLocalExtra != null && !publisherLocalExtra.isEmpty()) {
                localExtra.putAll(publisherLocalExtra);
            }
            moPubInterstitial.setLocalExtras(localExtra);
        }
        //Add custom targeting parameters to MoPub Ad request
        if (targetingParams != null) {

            // Check if keywords is configured by publisher, append it
            String publisherKeywords = moPubInterstitial.getKeywords();
            if (publisherKeywords != null && !"".equalsIgnoreCase(publisherKeywords)) {
                targetingParams.append(",");
                targetingParams.append(publisherKeywords);
            }
            moPubInterstitial.setKeywords(targetingParams.toString());
        }
        // Load MoPub ad request
        moPubInterstitial.load();
    }

    private StringBuilder generateTargeting(POBBid bid){
        StringBuilder targetingParams = new StringBuilder();
        Map<String, String> targeting = bid.getTargetingInfo();
        if (targeting != null && !targeting.isEmpty()) {
            // using iterator for iteration over Map.entrySet()
            Iterator<Map.Entry<String, String>> iterator = targeting.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                targetingParams.append(entry.getKey() + ":" + entry.getValue());
                if (iterator.hasNext()) {
                    targetingParams.append(",");
                }
                Log.d(TAG, "Targeting param [" + entry.getKey() + "] = " + entry.getValue());
            }
        }
        return targetingParams;
    }

    @Override
    public void setEventListener(POBInterstitialEventListener listener) {
        this.eventListener = listener;
    }

    @Override
    public POBInterstitialRendering getRenderer(String partnerName) {
        return null;
    }

    @Override
    public void show() {
        if (moPubInterstitial != null && moPubInterstitial.isReady()) {
            moPubInterstitial.show();
        }else {
            String errMsg = "MoPub SDK is not ready to show Interstitial Ad.";
            if (null != eventListener) {
                eventListener.onFailed(new POBError(POBError.INTERSTITIAL_NOT_READY, errMsg));
            }
            Log.e(TAG,errMsg);
        }
    }

    @Override
    public void destroy() {
        destroyMoPubAd();
    }

    //<editor-fold desc="InterstitialAdListener overridden methods">
    @Override
    public void onInterstitialLoaded(MoPubInterstitial interstitial) {
        if (null != eventListener) {
            eventListener.onAdServerWin();
        }
    }
    //</editor-fold>

    @Override
    public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {

        if (null != eventListener) {
            switch (errorCode) {
                case NO_FILL:
                case NETWORK_NO_FILL:
                    eventListener.onFailed(new POBError(POBError.NO_ADS_AVAILABLE, errorCode.toString()));
                    break;
                case NO_CONNECTION:
                case NETWORK_TIMEOUT:
                    eventListener.onFailed(new POBError(POBError.NETWORK_ERROR, errorCode.toString()));
                    break;
                case SERVER_ERROR:
                    eventListener.onFailed(new POBError(POBError.SERVER_ERROR, errorCode.toString()));
                    break;
                case CANCELLED:
                    eventListener.onFailed(new POBError(POBError.REQUEST_CANCELLED, errorCode.toString()));
                    break;
                default:
                    eventListener.onFailed(new POBError(POBError.INTERNAL_ERROR, errorCode.toString()));
                    break;
            }
        } else {
            Log.e(TAG, "Can not call failure callback, POBInterstitialEventListener reference null. MoPub error:"+errorCode.toString());
        }
    }

    @Override
    public void onInterstitialShown(MoPubInterstitial interstitial) {
        if (null != eventListener) {
            eventListener.onAdOpened();
        }
    }

    @Override
    public void onInterstitialClicked(MoPubInterstitial interstitial) {
        if (null != eventListener) {
            eventListener.onAdClick();
        }
    }

    @Override
    public void onInterstitialDismissed(MoPubInterstitial interstitial) {
        if (null != eventListener) {
            eventListener.onAdClosed();
        }
        destroy();
    }

    /**
     * Method to check network connection available
     * @param context android context
     * @return true if network is available else returns false
     */
    private static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null &&
                    activeNetwork.isConnected();
        }
        return false;
    }
    /**
     * Interface to get the MoPub Interstitial ad object, to configure the properties.
     */
    public interface MoPubConfigListener {
        /**
         * This method is called before event handler makes an ad request call to MoPub SDK. It passes
         * MoPub ad object which will be used to make an ad request. Publisher can configure the ad
         * request properties on the provided object.
         *
         * @param ad MoPub Interstitial ad
         */
        void configure(MoPubInterstitial ad);
    }
    //</editor-fold>

}
