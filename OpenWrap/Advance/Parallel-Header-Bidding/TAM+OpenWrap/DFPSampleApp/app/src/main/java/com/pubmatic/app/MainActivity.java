/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL
 * Unpublished Copyright (c) 2006-2023 PubMatic, All Rights Reserved.
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

package com.pubmatic.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.amazon.device.ads.AdRegistration;
import com.amazon.device.ads.MRAIDPolicy;
import com.pubmatic.appapp.R;
import com.pubmatic.sdk.common.BuildConfig;
import com.pubmatic.sdk.common.OpenWrapSDK;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    public static final String APP_KEY = "a9_onboarding_app_id";
    private static final String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void initListView() {

        RecyclerView adList = findViewById(R.id.ad_list);
        adList.setHasFixedSize(true);
        adList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        final List<String> itemList = new ArrayList<>();
        itemList.add("Banner Ad");
        itemList.add("Interstitial Ad");

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        adList.setLayoutManager(layoutManager);

        AdListAdapter adListAdapter = new AdListAdapter(itemList);
        adListAdapter.setItemClickListener((view, position) -> loadAdScreen(itemList.get(position)));
        adList.setAdapter(adListAdapter);
    }

    private void registerTAMAds() {
        AdRegistration.getInstance(APP_KEY, this);
        //Please remove enableLogging and enableTesting in production mode
        AdRegistration.enableLogging(true);
        AdRegistration.enableTesting(true);
        //Optional. Highly recommended for Transparent Ad Marketplace users
        AdRegistration.useGeoLocation(true);
        // If you are using Google Play Services 15.0.0 and plus, please call the following
        AdRegistration.setMRAIDSupportedVersions(new String[]{"1.0", "2.0", "3.0"});
        AdRegistration.setMRAIDPolicy(MRAIDPolicy.CUSTOM);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (BuildConfig.DEBUG) {
            OpenWrapSDK.setLogLevel(OpenWrapSDK.LogLevel.Debug);
        }

        //Change the Status Bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorStatusBar));
        }

        // Ask permission from user for location and write external storage
        if (!hasPermissions(MainActivity.this, PERMISSIONS)) {
            int MULTIPLE_PERMISSIONS_REQUEST_CODE = 123;
            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, MULTIPLE_PERMISSIONS_REQUEST_CODE);
        }

        initListView();
        registerTAMAds();
    }

    public void loadAdScreen(String adType) {
        Intent intent;
        if (adType.equals("Banner Ad")) {
            intent = new Intent(MainActivity.this, DFPBannerActivity.class);
        } else {
            intent = new Intent(MainActivity.this, DFPInterstitialActivity.class);
        }
        startActivity(intent);
    }
}
