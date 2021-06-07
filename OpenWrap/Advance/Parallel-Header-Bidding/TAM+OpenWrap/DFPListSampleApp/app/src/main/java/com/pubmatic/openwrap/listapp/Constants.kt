/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL
 * Unpublished Copyright (c) 2006-2021 PubMatic, All Rights Reserved.
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
package com.pubmatic.openwrap.listapp

import com.google.android.gms.ads.AdSize

/**
 * Constants class to maintain ad unit and config data
 */
class Constants {
    companion object{

        // TAM App Key
        const val APP_KEY = "a9_onboarding_app_id"

        // OpenWrap Ad Units
        const val OPENWRAP_AD_UNIT_ID = "/15671365/pm_sdk/PMSDK-Demo-App-Banner"
        const val PUB_ID = "156276"

        // Note: To enable/show display banner ads, you can use display banner profile ,
        // for In-Banner Video use In-Banner Video Profile id

        // Profile Id for Display Banner ads
        // const val PROFILE_ID = 1165

        // Profile Id for In-Banner Video ad
        const val PROFILE_ID = 1757

        // Ad size for for In-Banner Video ad
        val AD_SIZE = AdSize(300, 250)

        // DFP Ad unit id
        const val DFP_AD_UNIT_ID = "/15671365/pm_sdk/A9_Demo"
        // A9 TAM slot id
        const val SLOT_ID = "54fb2d08-c222-40b1-8bbe-4879322dc04b"

        // Feed interval after which a single banner ad is served.
        const val BANNER_INTERVAL = 10

        // Max no. of feeds can be displayed displayed in a feed view.
        const val MAX_FEEDS = 100
    }
}