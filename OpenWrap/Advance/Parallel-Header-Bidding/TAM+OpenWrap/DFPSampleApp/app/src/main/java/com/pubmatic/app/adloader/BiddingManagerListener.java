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

package com.pubmatic.app.adloader;

import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;

/**
 * Interface to be implemented by a class/activity to get the notification
 * after the responses are received from all the bidders.
 */
public interface BiddingManagerListener {

    /**
     * Manager class uses this method to notify once response is received from all the
     * registered bidders of different partners and at least one of them is a
     * success response with non-null response body.
     *
     * @param response  Targeting information map. This map is in the form of
     *                  {partnerName1: response1. partnerName2: response2, ...}
     *                  response1, response2 etc. are key-value pairs of targeting information.
     */
    void onResponseReceived(@Nullable Map<String, Map<String, List<String>>> response);

    /**
     * Manager class uses this method to notify once response is received from all the
     * registered bidders of different partners and all of them have failed with error.
     * This callback method will also be used in cases, where bidders have responded with
     * success but no single non-null response body is available.
     *
     * @param error     Error object
     */
    void onResponseFailed(@Nullable Object error);
}
