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

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pubmatic.sdk.common.OpenWrapSDK;
import com.pubmatic.sdk.common.models.POBDSAComplianceStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class host the list view to show the entry for the Ad types for demonstrating the respective features.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

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

        final List<AD_TYPE> itemList = new ArrayList<>(Arrays.asList(AD_TYPE.values()));

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        adList.setLayoutManager(layoutManager);

        AdListAdapter adListAdapter = new AdListAdapter(itemList);
        adListAdapter.setItemClickListener(new AdListAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                Class activityClass = itemList.get(position).getActivity();
                if (activityClass != null) {
                    loadAdScreen(activityClass);
                }
            }
        });
        adList.setAdapter(adListAdapter);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (BuildConfig.DEBUG) {
            OpenWrapSDK.setLogLevel(OpenWrapSDK.LogLevel.Debug);
        }

        //Change the Status Bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorStatusBar));
        }

        // Runtime optional permission list
        List<String> permissionList = new ArrayList<>();
        permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // Access READ_PHONE_STATE permission if api level 30 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }

        final String[] PERMISSIONS = new String[permissionList.size()];
        permissionList.toArray(PERMISSIONS);
        // Ask permission from user for location and write external storage
        if (!hasPermissions(MainActivity.this, PERMISSIONS)) {
            int MULTIPLE_PERMISSIONS_REQUEST_CODE = 123;
            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, MULTIPLE_PERMISSIONS_REQUEST_CODE);
        }

        initListView();
        OpenWrapSDK.setDSAComplianceStatus(POBDSAComplianceStatus.REQUIRED);
    }

    public void loadAdScreen(@NonNull Class activity) {
        Intent intent = new Intent(MainActivity.this, activity);
        startActivity(intent);
    }

    enum AD_TYPE {
        BANNER(DFPBannerActivity.class, "Banner"),
        INTERSTITIAL(DFPInterstitialActivity.class, "Interstitial Display"),
        VIDEO_INTERSTITIAL(DFPVideoInterstitialActivity.class, "Interstitial Video"),
        MREC_DISPLAY(DFPMRECDisplayActivity.class, "MREC Display"),
        MREC_VIDEO(DFPMRECVideoActivity.class, "MREC Video"),
        REWARDED_AD(DFPRewardedActivity.class,"Rewarded Ad"),
        NATIVE_BANNER(GAMNativeBannerActivity.class, "Native+Banner Ad"),
        NATIVE_STANDARD(GAMNativeStandardTemplateActivity.class, "Native Ad - Standard Template"),
        NATIVE_CUSTOM(GAMNativeCustomizedTemplateActivity.class, "Native Ad - Customized Template");

        private final Class activity;
        private final String displayText;

        AD_TYPE(@Nullable Class activity, @NonNull String text) {
            this.activity = activity;
            this.displayText = text;
        }

        public Class getActivity() {
            return activity;
        }

        public String getDisplayText() {
            return displayText;
        }
    }
}
