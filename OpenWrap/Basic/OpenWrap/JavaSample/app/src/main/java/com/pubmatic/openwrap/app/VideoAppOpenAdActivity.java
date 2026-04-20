
/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL
 * Unpublished Copyright (c) 2006-2025 PubMatic, All Rights Reserved.
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
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.pubmatic.sdk.appopenad.POBAppOpenAd;
import com.pubmatic.sdk.common.OpenWrapSDK;
import com.pubmatic.sdk.common.POBError;
import com.pubmatic.sdk.common.models.POBApplicationInfo;

import java.net.MalformedURLException;
import java.net.URL;

public class VideoAppOpenAdActivity extends AppCompatActivity implements LoggerListener {
    private static final String OPENWRAP_AD_UNIT_ID = "OpenWrapAppOpenAdUnit";
    @Nullable
    private POBAppOpenAd appOpenAd;
    @Nullable
    private TextView logTextView;
    @Nullable
    private Button showAdBtn;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appopenad);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_open_ad);

        logTextView = findViewById(R.id.ad_event_logs);
        showAdBtn = findViewById(R.id.show_ad);
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

        appOpenAd = new POBAppOpenAd(this, Constants.PUB_ID, Constants.PROFILE_ID_FOR_VIDEO, OPENWRAP_AD_UNIT_ID);

        appOpenAd.setListener(new POBAppOpenAdVideoListener());


        // Load Ad button
        findViewById(R.id.load_ad).setOnClickListener(v -> {
            if (showAdBtn != null) {
                showAdBtn.setEnabled(false);
            }
            appOpenAd.loadAd();
        });

        // Show button
        showAdBtn.setOnClickListener(v -> showAppOpenAd());

    }

    /**
     * To show App Open Ad ad call this method
     **/
    private void showAppOpenAd() {
        if (null != appOpenAd && appOpenAd.isReady()) {
            // Call show on app open ad
            appOpenAd.showAd();
        }
    }

    private void destroyAd() {
        if (appOpenAd != null) {
            appOpenAd.destroy();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyAd();
        Logger.removeLoggerListener(this);
    }

    private class POBAppOpenAdVideoListener extends POBAppOpenAd.POBAppOpenAdListener {

        private final String TAG = "POBAppOpenAdListener";

        @Override
        public void onAdClicked(@NonNull POBAppOpenAd ad) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdClicked");

        }

        @Override
        public void onAdReceived(@NonNull POBAppOpenAd ad) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdReceived");
            //Method gets called when ad gets loaded in container
            //Here, you can show app open ad to user
            if (showAdBtn != null) {
                showAdBtn.setEnabled(true);
            }
        }

        @Override
        public void onAdFailedToLoad(@NonNull POBAppOpenAd ad, @NonNull POBError error) {
            Logger.log(Logger.LogType.ERROR, TAG, "onAdFailedToLoad : Ad failed to load with error -" + error.toString());
        }

        @Override
        public void onAdFailedToShow(@NonNull POBAppOpenAd ad, @NonNull POBError error) {
            Logger.log(Logger.LogType.ERROR, TAG, "onAdFailedToLoad : Ad failed to show with error -" + error.toString());
        }

        @Override
        public void onAdImpression(@NonNull POBAppOpenAd ad) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdImpression");
        }

        @Override
        public void onAdOpened(@NonNull POBAppOpenAd ad) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdOpened");
        }

        @Override
        public void onAdExpired(@NonNull POBAppOpenAd ad) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdExpired");
        }

        @Override
        public void onAppLeaving(@NonNull POBAppOpenAd ad) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAppLeaving");
        }

        @Override
        public void onAdClosed(@NonNull POBAppOpenAd ad) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdClosed");
        }


    }

    @Override
    public void log(@NonNull String message) {
        logTextView.append(message + "\n");
    }
}



