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

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDexApplication;

import com.pubmatic.sdk.common.OpenWrapSDK;
import com.pubmatic.sdk.common.OpenWrapSDKConfig;
import com.pubmatic.sdk.common.OpenWrapSDKInitializer;
import com.pubmatic.sdk.common.POBError;

import java.util.Collections;

/**
 * Application class that initializes OpenWrap SDK on app startup for the GAM Next-Gen sample app.
 */
public class OpenWrapSampleApp extends MultiDexApplication {

    private static final String TAG = "OpenWrap";

    @Override
    public void onCreate() {
        super.onCreate();
        // Enable OpenWrap SDK logs for debugging
        OpenWrapSDK.setLogLevel(OpenWrapSDK.LogLevel.All);

        // A valid Play Store URL of an Android application is required.
        // This app information is a global configuration and you need not set this for every ad request.
        OpenWrapSDK.setApplicationInfo(ApplicationInfoUtil.createApplicationInfo());

        // Call the initialize method to warm up the OpenWrap SDK.
        OpenWrapSDK.initialize(
                getApplicationContext(),
                new OpenWrapSDKConfig.Builder(Constants.PUB_ID, Collections.singletonList(Constants.PROFILE_ID)).build(),
                new OpenWrapSDKInitializer.Listener() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, Constants.OWSDK_INITIALIZATION_SUCCESSFUL);
                    }

                    @Override
                    public void onFailure(@NonNull POBError error) {
                        Log.e(TAG, Constants.OWSDK_INITIALIZATION_FAILED + error.toString());
                    }
                }
        );
    }
}
