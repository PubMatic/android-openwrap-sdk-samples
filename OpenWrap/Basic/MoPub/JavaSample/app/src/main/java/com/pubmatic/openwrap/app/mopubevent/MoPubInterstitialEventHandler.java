/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL
 * Unpublished Copyright (c) 2006-2020 PubMatic, All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of PubMatic. The intellectual and technical concepts contained
 * herein are proprietary to PubMatic and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from PubMatic.  Access to the source code contained herein is hereby forbidden to anyone except current PubMatic employees, managers or contractors who have executed
 * Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure  of  this source code, which includes
 * information that is confidential and/or proprietary, and is a trade secret, of  PubMatic.   ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC  PERFORMANCE,
 * OR PUBLIC DISPLAY OF OR THROUGH USE  OF THIS  SOURCE CODE  WITHOUT  THE EXPRESS WRITTEN CONSENT OF PubMatic IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE
 * LAWS AND INTERNATIONAL TREATIES.  THE RECEIPT OR POSSESSION OF  THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS
 * TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT  MAY DESCRIBE, IN WHOLE OR IN PART.
 */

package com.pubmatic.openwrap.app.mopubevent;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.pubmatic.sdk.common.POBError;
import com.pubmatic.sdk.common.log.PMLog;
import com.pubmatic.sdk.common.ui.POBInterstitialRendering;
import com.pubmatic.sdk.openwrap.core.POBBid;
import com.pubmatic.sdk.openwrap.interstitial.POBInterstitialEvent;
import com.pubmatic.sdk.openwrap.interstitial.POBInterstitialEventListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import androidx.core.content.ContextCompat;

/**
 * This class is compatible with OpenWrap SDK v1.5.0.
 * This class implements the communication between the OpenWrap SDK and the MoPub SDK for a given ad
 * unit. It implements the PubMatic's Wrapper interface. PM SDK notifies (using wrapper interface)
 * to make a request to MoPub SDK and pass the targeting parameters. This class also creates the MoPub's
 * MoPubInterstitial, initialize it and listen for the callback methods. And pass the MoPub ad event to
 * OpenWrap SDK via POBInterstitialEventListener.
 */
public class MoPubInterstitialEventHandler implements POBInterstitialEvent, MoPubInterstitial.InterstitialAdListener {

    private static final String TAG = "MoPubInterstitialEvent";
    /**
     * Config listener to check if publisher want to config properties in MoPub ad
     */
    private MoPubConfigListener mopubConfigListener;
    /**
     * Interface to pass the MoPub ad event to OpenWrap SDK
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
            PMLog.error(TAG, error.toString());
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

        /// NOTE: Please do not remove this code. Need to reset MoPub interstitial listener to
        // MoPubInterstitialEventHandler as these are used by MoPubInterstitialEventHandler internally.
        if (moPubInterstitial.getInterstitialAdListener() != this) {
            moPubInterstitial.setInterstitialAdListener(this);
            PMLog.warn(TAG, "Resetting MoPub interstitial listener to MoPubInterstitialEventHandler" +
                    " as these are used by MoPubInterstitialEventHandler internally.");
        }

        if (null != bid) {
            // Logging details of bid objects for debug purpose.
            PMLog.debug(TAG, bid.toString());
            targetingParams = generateTargeting(bid);

            // No need to set localExtras when status is 0, as PubMatic line item will not get
            // picked up
            if (bid.getStatus() == 1) {
                Map<String, Object> localExtra = new HashMap<>();
                localExtra.put(POBInterstitialCustomEvent.BID_KEY, bid);

                // Check if any local extra is configured by publisher, append it
                Map<String, Object> publisherLocalExtra = moPubInterstitial.getLocalExtras();
                if (publisherLocalExtra != null && !publisherLocalExtra.isEmpty()) {
                    localExtra.putAll(publisherLocalExtra);
                }
                moPubInterstitial.setLocalExtras(localExtra);
            }
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
                targetingParams.append(entry.getKey()).append(":").append(entry.getValue());
                if (iterator.hasNext()) {
                    targetingParams.append(",");
                }
                PMLog.debug(TAG, "Targeting param [" + entry.getKey() + "] = " + entry.getValue());
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
            PMLog.error(TAG,errMsg);
        }
    }

    @Override
    public void destroy() {
        destroyMoPubAd();
    }

    //<editor-fold desc="InterstitialAdListener overridden methods">
    @Override
    public void onInterstitialLoaded(MoPubInterstitial interstitial) {
        PMLog.info(TAG, "onInterstitialLoaded");
        POBBid pubmaticBid = (POBBid) interstitial.getLocalExtras().get(POBInterstitialCustomEvent.BID_KEY);
        if (pubmaticBid != null && eventListener != null) {
            if (pubmaticBid.hasWon()) {
                eventListener.onOpenWrapPartnerWin();
            } else {
                eventListener.onAdServerWin();
            }
        }else {
            onInterstitialFailed(interstitial, MoPubErrorCode.NETWORK_NO_FILL);
        }
    }
    //</editor-fold>

    @Override
    public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
        PMLog.info(TAG, "onInterstitialFailed");
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
            PMLog.error(TAG, "Can not call failure callback, POBInterstitialEventListener reference null. MoPub error:"+errorCode.toString());
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
