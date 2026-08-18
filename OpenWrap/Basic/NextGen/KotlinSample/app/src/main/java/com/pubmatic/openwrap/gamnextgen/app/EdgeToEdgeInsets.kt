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

import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Applies edge-to-edge layout for screens that use transparent system bars, mirroring NextGenTestApp.
 */
internal object EdgeToEdgeInsets {

    fun apply(
        activity: AppCompatActivity,
        root: View,
        topTarget: View,
        bottomTarget: View,
        applyHorizontalPadding: Boolean,
    ) {
        activity.enableEdgeToEdge()

        val topInitialLeft = topTarget.paddingLeft
        val topInitialTop = topTarget.paddingTop
        val topInitialRight = topTarget.paddingRight
        val topInitialBottom = topTarget.paddingBottom

        val bottomInitialLeft = bottomTarget.paddingLeft
        val bottomInitialTop = bottomTarget.paddingTop
        val bottomInitialRight = bottomTarget.paddingRight
        val bottomInitialBottom = bottomTarget.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            topTarget.setPadding(
                if (applyHorizontalPadding) topInitialLeft + systemBars.left else topInitialLeft,
                topInitialTop + systemBars.top,
                if (applyHorizontalPadding) topInitialRight + systemBars.right else topInitialRight,
                topInitialBottom,
            )
            bottomTarget.setPadding(
                if (applyHorizontalPadding) bottomInitialLeft + systemBars.left else bottomInitialLeft,
                bottomInitialTop,
                if (applyHorizontalPadding) bottomInitialRight + systemBars.right else bottomInitialRight,
                bottomInitialBottom + systemBars.bottom,
            )
            insets
        }
        ViewCompat.requestApplyInsets(root)
    }
}
