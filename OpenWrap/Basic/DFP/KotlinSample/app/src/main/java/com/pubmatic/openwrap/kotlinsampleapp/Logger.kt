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
package com.pubmatic.openwrap.kotlinsampleapp

import android.util.Log

/**
 * Logger class to handle logging messages to both Logcat and a LoggerListener.
 */
object Logger {

    private val loggerListeners: MutableSet<LoggerListener> = HashSet()

    /**
     * Sets the LoggerListener where log messages will be sent.
     *
     * @param listener The LoggerListener to receive log messages.
     */
    fun registerLoggerListener(listener: LoggerListener) = loggerListeners.add(listener)

    /**
     * Removes the LoggerListener.
     *
     * @param listener The LoggerListener to remove.
     */
    fun removeLoggerListener(listener: LoggerListener) = loggerListeners.remove(listener)


    /**
     * Logs a message to Logcat and sends it to the LoggerListener.
     *
     * @param logType The type of log message.
     * @param tag     The tag for the log message.
     * @param message The log message.
     */
    fun log(logType: LogType, tag: String, message: String) {
        when (logType) {
            LogType.DEBUG -> Log.d(tag, message)
            LogType.INFO -> Log.i(tag, message)
            LogType.ERROR -> Log.e(tag, message)
            LogType.WARN -> Log.w(tag, message)
            LogType.VERBOSE -> Log.v(tag, message)
        }

        for (listener in loggerListeners) {
            listener.log(message + "\n")
        }
    }

    /**
     * Enum class representing different types of log levels.
     */
    enum class LogType {
        /** Debug level log */
        DEBUG,

        /** Info level log */
        INFO,

        /** Error level log */
        ERROR,

        /** Warning level log */
        WARN,

        /** Verbose level log */
        VERBOSE
    }

}