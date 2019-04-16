package com.pubmatic.openbid.kotlinsampleapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_home.*
import java.util.*

/**
 * Main Activity to show Multiple Ad Type Implementation
 */
class MainActivity : AppCompatActivity()  {

    private var recycler: RecyclerView ? = null
    private var list: ArrayList<AdType>? = null

    companion object {

        private val PERMISSIONS = Array<String>(3){Manifest.permission.ACCESS_FINE_LOCATION; Manifest.permission.ACCESS_COARSE_LOCATION;
            Manifest.permission.WRITE_EXTERNAL_STORAGE}


        private fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null) {
                for (permission in permissions) {
                    if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
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
        setSupportActionBar(toolbar)

        recycler = findViewById(R.id.recycler_view)
        recycler?.layoutManager = LinearLayoutManager(this)
        recycler?.setHasFixedSize(true)
    
        list = ArrayList()
        list?.addAll(AdType.values())

        var recyclerAdapter: RecyclerAdapter? = null
        if(null != list){
            recyclerAdapter = RecyclerAdapter(list, RecyclerItemListener())
        }
        recycler?.adapter = recyclerAdapter

        // Ask permission from user for location and write external storage
        if (!hasPermissions(this, PERMISSIONS)) {
            val MULTIPLE_PERMISSIONS_REQUEST_CODE = 123
            ActivityCompat.requestPermissions(this, PERMISSIONS, MULTIPLE_PERMISSIONS_REQUEST_CODE)
        }

    }

    /**
     * Navigates respective Activity from list info
     */
    fun displayActivity(position: Int){
        if(null != list?.get(position)?.activity){
            val intent = Intent(this, list?.get(position)?.activity)
            startActivity(intent)
        }
    }

    /**
     * Constant to represents AdType
     */
    enum class AdType constructor(val activity: Class<*>?, val displayName: String) {
        BANNER(BannerActivity::class.java, "Banner"),
        INTERSTITIAL(InterstitialActivity::class.java, "Interstitial"),
    }

    /**
     * Listener implementation to get item interaction callbacks
     */
    inner class RecyclerItemListener : RecyclerAdapter.OnItemClickListener{
        /**
         * Listener method to listen item click
         * @param position position of list item
         */
        override fun onItemClick(position: Int) {
            displayActivity(position)
        }
    }

}
