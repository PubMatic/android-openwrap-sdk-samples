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

package com.pubmatic.openwrap.gamnextgen.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.pubmatic.openwrap.gamnextgen.app.databinding.ActivityHomeBinding
import com.pubmatic.sdk.common.OpenWrapSDK
import com.pubmatic.sdk.common.models.POBDSAComplianceStatus

/**
 * Host activity that lists all GAM Next-Gen ad format demos and initializes the Mobile Ads SDK.
 */
class MainActivity : BaseSampleActivity() {

    private lateinit var itemList: List<AdType>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyEdgeToEdge(binding.root, binding.includeToolbar.root, binding.recyclerView, applyHorizontalPadding = true)
        setSupportActionBar(binding.includeToolbar.toolbar)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)

        itemList = AdType.entries

        val adapter = AdListAdapter(itemList) { position ->
            itemList.getOrNull(position)?.activity?.let { activityClass ->
                startActivity(Intent(this, activityClass))
            }
        }
        binding.recyclerView.adapter = adapter

        // Set DSA compliance status for OpenWrap SDK
        OpenWrapSDK.setDSAComplianceStatus(POBDSAComplianceStatus.REQUIRED)

        // Initialize GAM Next-Gen Mobile Ads SDK
        initializeMobileAdsSdk()
    }

    private fun initializeMobileAdsSdk() {
        // Initialize on a background thread as recommended by the GAM Next-Gen SDK
        Thread {
            MobileAds.initialize(
                this@MainActivity,
                InitializationConfig.Builder(Constants.GAM_APP_ID).build(),
            ) {
                Log.i(TAG, "Google Mobile Ads Next-Gen SDK initialized.")
            }
        }.start()
    }

    enum class AdType(val activity: Class<*>?, val displayName: String) {
        BANNER(NextGenBannerActivity::class.java, "Banner"),
        INTERSTITIAL(NextGenInterstitialActivity::class.java, "Interstitial Display"),
        VIDEO_INTERSTITIAL(NextGenVideoInterstitialActivity::class.java, "Interstitial Video"),
        MREC_DISPLAY(NextGenMRECDisplayActivity::class.java, "MREC Display"),
        MREC_VIDEO(NextGenMRECVideoActivity::class.java, "MREC Video"),
        REWARDED_AD(NextGenRewardedActivity::class.java, "Rewarded Ad"),
        NATIVE_BANNER(NextGenNativeBannerActivity::class.java, "Native+Banner Ad"),
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
