/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL
 * Unpublished Copyright (c) 2006-2026 PubMatic, All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of PubMatic. The intellectual and technical concepts contained
 * herein are proprietary to PubMatic and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from PubMatic.  Access to the source code contained herein is hereby forbidden to anyone except current PubMatic employees, managers or contractors who have executed
 * Confidentiality and Non-disclosure agreements explicitly covering such access or to such other persons whom are directly authorized by PubMatic to access the source code and are subject to confidentiality and nondisclosure obligations with respect to the source code.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure  of  this source code, which includes
 * information that is confidential and/or proprietary, and is a trade secret, of  PubMatic.   ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC  PERFORMANCE,
 * OR PUBLIC DISPLAY OF OR THROUGH USE  OF THIS  SOURCE CODE  WITHOUT  THE EXPRESS WRITTEN CONSENT OF PUBMATIC IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE
 * LAWS AND INTERNATIONAL TREATIES.  THE RECEIPT OR POSSESSION OF  THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS
 * TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT  MAY DESCRIBE, IN WHOLE OR IN PART.
 */
package com.pubmatic.openwrap.app;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.pubmatic.sdk.common.OpenWrapSDK;
import com.pubmatic.sdk.common.POBAdSize;
import com.pubmatic.sdk.common.POBError;
import com.pubmatic.sdk.common.models.POBApplicationInfo;
import com.pubmatic.sdk.openwrap.banner.POBBannerView;

import java.net.MalformedURLException;
import java.net.URL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MRECVideoActivity extends AppCompatActivity implements LoggerListener {

    private static final String OPENWRAP_AD_UNIT_ID = "OpenWrapBannerAdUnit";

    private POBBannerView banner;
    private TextView logTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mrec_video);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.mrec_video_title);

        logTextView = findViewById(R.id.ad_event_logs);
        Logger.registerLoggerListener(this);
        Logger.log(Logger.LogType.INFO, Constants.TAG_OWSDK, Constants.OWSDK_VERSION_LOG + OpenWrapSDK.getVersion());

        // A valid Play Store Url of an Android application is required.
        POBApplicationInfo appInfo = new POBApplicationInfo();
        try {
            appInfo.setStoreURL(new URL("https://play.google.com/store/apps/details?id=com.example.android&hl=en"));
        } catch (MalformedURLException e) {
            Logger.log(Logger.LogType.ERROR, Constants.TAG_OWSDK, "Failed to set Store URL, "+ e.getMessage());
        }

        // This app information is a global configuration & you
        // Need not set this for every ad request(of any ad type)
        OpenWrapSDK.setApplicationInfo(appInfo);

        // Initialise banner view with MRECVideo size i.e 300x250
        banner = findViewById(R.id.banner);
        banner.init(Constants.PUB_ID,
                Constants.PROFILE_ID_FOR_VIDEO,
                OPENWRAP_AD_UNIT_ID, POBAdSize.BANNER_SIZE_300x250);

        //optional listener to listen banner events
        banner.setListener(new POBBannerViewListener());

        // Call loadAd() on banner instance
        banner.loadAd();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Destroy banner before destroying activity
        if (null != banner) {
            banner.destroy();
        }
        Logger.removeLoggerListener(this);
    }


    class POBBannerViewListener extends POBBannerView.POBBannerViewListener {
        private final String TAG = "POBBannerViewListener";

        // Callback method Notifies that an ad has been successfully loaded and rendered.
        @Override
        public void onAdReceived(@NonNull POBBannerView view) {
            Logger.log(Logger.LogType.DEBUG, TAG, "Ad Received");
        }

        // Callback method Notifies an error encountered while loading or rendering an ad.
        @Override
        public void onAdFailed(@NonNull POBBannerView view, @NonNull POBError error) {
            Logger.log(Logger.LogType.ERROR, TAG, error.toString());
        }

        // Callback method Notifies that the banner ad view will launch a dialog on top of the current view
        @Override
        public void onAdOpened(@NonNull POBBannerView view) {
            Logger.log(Logger.LogType.DEBUG, TAG, "Ad Opened");
        }

        // Callback method Notifies that the banner ad view is clicked.
        @Override
        public void onAdClicked(@NonNull POBBannerView view) {
            Logger.log(Logger.LogType.DEBUG, TAG, "Ad Clicked");
        }

        // Callback method Notifies that the banner ad view has dismissed the modal on top of the current view
        @Override
        public void onAdClosed(@NonNull POBBannerView view) {
            Logger.log(Logger.LogType.DEBUG, TAG, "Ad Closed");
        }

        @Override
        public void onAppLeaving(@NonNull POBBannerView view) {
            // Implement your custom logic
            Logger.log(Logger.LogType.DEBUG, TAG, "Banner : App Leaving");
        }

        @Override
        public void onAdImpression(@NonNull POBBannerView view) {
            // Implement your custom logic
            Logger.log(Logger.LogType.DEBUG, TAG, "Ad Impression");
        }

    }

    // Appends a log message to the log TextView.
    @Override
    public void log(@NonNull String message) {
        logTextView.append(message + "\n");
    }
}
