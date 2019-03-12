/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL
 * Unpublished Copyright (c) 2006-2019 PubMatic, All Rights Reserved.
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

package com.pubmatic.openbid.app.mopubevent;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;
import com.pubmatic.sdk.common.POBAdSize;
import com.pubmatic.sdk.common.POBError;
import com.pubmatic.sdk.openbid.banner.POBBannerEvent;
import com.pubmatic.sdk.openbid.banner.POBBannerEventListener;
import com.pubmatic.sdk.openbid.core.POBBid;
import com.pubmatic.sdk.webrendering.ui.POBBannerRendering;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class implements the communication between the OpenBid SDK and the MoPub SDK for a given ad
 * unit. It implements the PubMatic's OpenBid interface. POB SDK notifies (using OpenBid interface)
 * to make a request to MoPub SDK and pass the targeting parameters. This class also creates the MoPub's
 * MoPubView, initialize it and listen for the callback methods. And pass the MoPub ad event to
 * OpenBid SDK via POBBannerEventListener.
 */
public class MoPubBannerEventHandler implements POBBannerEvent, MoPubView.BannerAdListener {

    private static final String TAG = "MoPubBannerEvent";
    /**
     * Config listener to check if publisher want to config properties in MoPub ad
     */
    private MoPubConfigListener mopubConfigListener;
    /**
     * Ad size to request PubMatic banner
     */
    private POBAdSize adSize;
    /**
     * MoPub Banner ad view
     */
    private MoPubView moPubView;
    /**
     * Interface to pass the MoPub ad event to OpenBid SDK
     */
    private POBBannerEventListener eventListener;

    /**
     * Android context
     */
    private Context context;

    /**
     * Constructor
     *
     * @param context  value of context
     * @param adUnitId SDP ad unit ID
     */
    public MoPubBannerEventHandler(Context context, String adUnitId, POBAdSize size) {
        this.context = context;
        adSize = size;
        moPubView = new MoPubView(context);
        moPubView.setAdUnitId(adUnitId);

        // DO NOT REMOVE/OVERRIDE BELOW LISTENER/PROPERTY
        moPubView.setBannerAdListener(this);
        moPubView.setAutorefreshEnabled(false);
    }

    /**
     * Sets the Data listener object. Publisher should implement the MoPubConfigListener and
     * override its method only when publisher needs to set the targeting parameters over MoPub
     * banner ad view.
     *
     * @param listener MoPub config listener
     */
    public void setConfigListener(MoPubConfigListener listener) {
        mopubConfigListener = listener;
    }

    // ------- Overridden methods from POBBannerEvent -------
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
        moPubView.setKeywords(null);
        // Check if publisher want to set any targeting data
        if (mopubConfigListener != null) {
            mopubConfigListener.configure(moPubView);
        }

        if (moPubView.getBannerAdListener() != this) {
            Log.w(TAG, "Do not set MoPub listener. This is used by MoPubBannerEventHandler internally.");
        }

        if (null != bid) {
            // Logging details of bid objects for debug purpose.
            Log.d(TAG, bid.toString());
            StringBuilder keywords = new StringBuilder();
            Map<String, String> targeting = bid.getTargetingInfo();
            if (targeting != null && !targeting.isEmpty()) {
                // using for-each loop for iteration over Map.entrySet()
                Iterator<Map.Entry<String, String>> targetingSet = targeting.entrySet().iterator();
                while (targetingSet.hasNext()) {
                    Map.Entry<String, String> entry = targetingSet.next();
                    keywords.append(entry.getKey()).append(":").append(entry.getValue());
                    if (targetingSet.hasNext()) {
                        keywords.append(",");
                    }
                    Log.d(TAG, "Targeting param [" + entry.getKey() + "] = " + entry.getValue());
                }

                // Check if keywords is configured by publisher, append it
                String publisherKeywords = moPubView.getKeywords();
                if (publisherKeywords != null && !"".equalsIgnoreCase(publisherKeywords)) {
                    keywords.append(",");
                    keywords.append(publisherKeywords);
                }
                moPubView.setKeywords(keywords.toString());

                // No need to set localExtras when status is 0, as Pubmatic line item will not get
                // picked up
                if (bid.getStatus() == 1) {
                    Map<String, Object> localMap = new HashMap<>();
                    localMap.put(POBBannerCustomEvent.BID_KEY, bid);

                    // Check if any local extra is configured by publisher, append it
                    Map<String, Object> publisherLocalExtra = moPubView.getLocalExtras();
                    if (publisherLocalExtra != null && !publisherLocalExtra.isEmpty()) {
                        localMap.putAll(publisherLocalExtra);
                    }
                    moPubView.setLocalExtras(localMap);
                }
            }

        }
        // Load MoPub ad request
        moPubView.loadAd();
    }

    @Override
    public void setEventListener(POBBannerEventListener listener) {
        eventListener = listener;
    }

    @Override
    public POBBannerRendering getRenderer(String partnerName) {
        return null;
    }

    @Override
    public POBAdSize getAdSize() {
        return new POBAdSize(moPubView.getAdWidth(), moPubView.getAdHeight());
    }

    @Override
    public POBAdSize[] requestedAdSizes() {
        POBAdSize[] pobAdSizes = new POBAdSize[1];
        pobAdSizes[0] = adSize;
        return pobAdSizes;
    }

    @Override
    public void destroy() {
        if (null != moPubView) {
            moPubView.destroy();
            moPubView = null;
        }
        if (null != eventListener) {
            eventListener = null;
        }
        context = null;
    }

    @Override
    public void onBannerLoaded(MoPubView banner) {
        Log.d(TAG, "onBannerLoaded");
        eventListener.onAdServerWin(banner);
    }

    @Override
    public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
        Log.d(TAG, "onBannerFailed");
        POBError error;
        if (eventListener != null) {
            switch (errorCode) {
                case NO_FILL:
                case NETWORK_NO_FILL:
                    error = new POBError(POBError.NO_ADS_AVAILABLE, errorCode.toString());
                    break;
                case NO_CONNECTION:
                case NETWORK_TIMEOUT:
                    error = new POBError(POBError.NETWORK_ERROR, errorCode.toString());
                    break;
                case SERVER_ERROR:
                    error = new POBError(POBError.SERVER_ERROR, errorCode.toString());
                    break;
                case CANCELLED:
                    error = new POBError(POBError.REQUEST_CANCELLED, errorCode.toString());
                    break;
                case NETWORK_INVALID_STATE:
                    error = new POBError(POBError.INVALID_REQUEST, errorCode.toString());
                    break;
                default:
                    error = new POBError(POBError.INTERNAL_ERROR, errorCode.toString());
                    break;
            }
            eventListener.onFailed(error);
        } else {
            Log.e(TAG, "Can not call failure callback, POBBannerEventListener reference null. MoPub error:"+errorCode.toString());
        }

    }

    @Override
    public void onBannerClicked(MoPubView banner) {
        if (eventListener != null) {
            eventListener.onAdClick();
        }
    }

    @Override
    public void onBannerExpanded(MoPubView banner) {
        if (eventListener != null) {
            eventListener.onAdOpened();
        }

    }

    @Override
    public void onBannerCollapsed(MoPubView banner) {
        if (eventListener != null) {
            eventListener.onAdClosed();
        }
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
     * Interface to get the MoPub Banner ad view object, to configure the properties.
     */
    public interface MoPubConfigListener {
        /**
         * This method is called before event handler makes an ad request call to MoPub SDK. It
         * passes MoPub ad view which will be used to make an ad request. Publisher can configure
         * the ad request properties on the provided object.
         *
         * @param adView MoPub Banner ad view
         */
        void configure(MoPubView adView);

    }

}
