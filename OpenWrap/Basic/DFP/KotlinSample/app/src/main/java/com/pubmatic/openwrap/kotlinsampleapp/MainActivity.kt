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
package com.pubmatic.openwrap.kotlinsampleapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pubmatic.sdk.common.OpenWrapSDK
import com.pubmatic.sdk.common.models.POBDSAComplianceStatus

/**
 * This class host the list view to show the entry for the Ad types for demonstrating the respective
 * features.
 */
class MainActivity : AppCompatActivity() {

    var recycler: RecyclerView? = null
    var list: ArrayList<AdType>? = null

    companion object {

        private fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissions.forEach {
                    if (ActivityCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED) {
                        return false
                    }
                }
            }
            return true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        recycler = findViewById(R.id.recycler_view)
        recycler?.layoutManager = LinearLayoutManager(this)
        recycler?.setHasFixedSize(true)

        list = java.util.ArrayList()
        list?.addAll(AdType.values())

        val recyclerAdapter = RecyclerAdapter(list, RecyclerItemListener())
        recycler?.adapter = recyclerAdapter

        // Ask permission from user for location and write external storage
        val permissionList: MutableList<String> = ArrayList()
        permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        // Access READ_PHONE_STATE permission if api level 30 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE)
        }

        val PERMISSIONS = permissionList.toTypedArray()

        if (!hasPermissions(this@MainActivity, PERMISSIONS)) {
            val MULTIPLE_PERMISSIONS_REQUEST_CODE = 123
            ActivityCompat.requestPermissions(this@MainActivity, PERMISSIONS, MULTIPLE_PERMISSIONS_REQUEST_CODE)
        }

        OpenWrapSDK.setDSAComplianceStatus(POBDSAComplianceStatus.REQUIRED)
    }

    /**
     * Navigates respective Activity from list info
     */
    fun displayActivity(position: Int) {
        if (null != list?.get(position)?.activity) {
            val intent = Intent(this, list?.get(position)?.activity)
            startActivity(intent)
        }
    }

    /**
     * Handling the list item click
     */
    inner class RecyclerItemListener : RecyclerAdapter.OnItemClickListener {
        override fun onItemClick(position: Int) {
            displayActivity(position)
        }

    }

    /**
     * Constant to represents AdType
     */
    enum class AdType constructor(val activity: Class<*>?, val displayName: String) {
        BANNER(DFPBannerActivity::class.java, "Banner"),
        INTERSTITIAL(DFPInterstitialActivity::class.java, "Interstitial Display"),
        VIDEO_INTERSTITIAL(VideoInterstitialActivity::class.java, "Interstitial Video"),
        MREC_DISPLAY(DFPMRECDisplayActivity::class.java, "MREC Display"),
        MREC_VIDEO(DFPMRECVideoActivity::class.java, "MREC Video"),
        REWARDED_AD(DFPRewardedActivity::class.java,"Rewarded Ad"),
        NATIVE_BANNER(GAMNativeBannerActivity::class.java,"Native+Banner Ad"),
        NATIVE_STANDARD(GAMNativeStandardTemplateActivity::class.java,"Native Ad - Standard Template"),
        NATIVE_CUSTOM(GAMNativeCustomizedTemplateActivity::class.java,"Native Ad - Customized Template");
    }
}
