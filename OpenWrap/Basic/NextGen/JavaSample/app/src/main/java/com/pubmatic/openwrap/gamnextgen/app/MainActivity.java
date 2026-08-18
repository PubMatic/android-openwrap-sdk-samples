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

package com.pubmatic.openwrap.gamnextgen.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.libraries.ads.mobile.sdk.MobileAds;
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig;
import com.pubmatic.sdk.common.OpenWrapSDK;
import com.pubmatic.sdk.common.models.POBDSAComplianceStatus;
import com.pubmatic.openwrap.gamnextgen.app.databinding.ActivityHomeBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Host activity that lists all GAM Next-Gen ad format demos and initializes the Mobile Ads SDK.
 */
public class MainActivity extends BaseSampleActivity {

    private static final String TAG = "MainActivity";

    private List<AD_TYPE> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityHomeBinding binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyEdgeToEdge(binding.getRoot(), binding.includeToolbar.getRoot(), binding.recyclerView, true);
        setSupportActionBar(binding.includeToolbar.toolbar);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setHasFixedSize(true);

        itemList = new ArrayList<>(Arrays.asList(AD_TYPE.values()));

        AdListAdapter adListAdapter = new AdListAdapter(itemList);
        adListAdapter.setItemClickListener((view, position) -> {
            Class<?> activityClass = itemList.get(position).getActivity();
            if (activityClass != null) {
                startActivity(new Intent(MainActivity.this, activityClass));
            }
        });
        binding.recyclerView.setAdapter(adListAdapter);

        // Set DSA compliance status for OpenWrap SDK
        OpenWrapSDK.setDSAComplianceStatus(POBDSAComplianceStatus.REQUIRED);

        // Initialize GAM Next-Gen Mobile Ads SDK
        initializeMobileAdsSdk();
    }

    private void initializeMobileAdsSdk() {
        // Initialize on a background thread as recommended by the GAM Next-Gen SDK
        new Thread(() -> MobileAds.initialize(
                MainActivity.this,
                new InitializationConfig.Builder(Constants.GAM_APP_ID).build(),
                status -> Log.i(TAG, "Google Mobile Ads Next-Gen SDK initialized.")
        )).start();
    }

    enum AD_TYPE {
        BANNER(NextGenBannerActivity.class, "Banner"),
        INTERSTITIAL(NextGenInterstitialActivity.class, "Interstitial Display"),
        VIDEO_INTERSTITIAL(NextGenVideoInterstitialActivity.class, "Interstitial Video"),
        MREC_DISPLAY(NextGenMRECDisplayActivity.class, "MREC Display"),
        MREC_VIDEO(NextGenMRECVideoActivity.class, "MREC Video"),
        REWARDED_AD(NextGenRewardedActivity.class, "Rewarded Ad"),
        NATIVE_BANNER(NextGenNativeBannerActivity.class, "Native+Banner Ad");

        private final Class<?> activity;
        private final String displayText;

        AD_TYPE(@Nullable Class<?> activity, @NonNull String text) {
            this.activity = activity;
            this.displayText = text;
        }

        @Nullable
        public Class<?> getActivity() {
            return activity;
        }

        @NonNull
        public String getDisplayText() {
            return displayText;
        }
    }
}
