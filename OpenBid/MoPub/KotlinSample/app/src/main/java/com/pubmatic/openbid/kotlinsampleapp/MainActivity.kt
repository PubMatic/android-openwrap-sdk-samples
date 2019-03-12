package com.pubmatic.openbid.kotlinsampleapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.mopub.common.MoPub
import com.mopub.common.SdkConfiguration
import com.mopub.common.SdkInitializationListener

import kotlinx.android.synthetic.main.activity_home.*


class MainActivity : AppCompatActivity()  {

    var recycler: RecyclerView ? = null
    var list: ArrayList<String> ? = null

    companion object {

        private val PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        private fun hasPermissions(context: Context?, vararg permissions: String): Boolean {
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

        initMoPub()

        recycler = findViewById(R.id.recycler_view)
        recycler?.layoutManager = LinearLayoutManager(this)
        recycler?.setHasFixedSize(true)

        list = ArrayList<String>()
        list?.add("Banner")
        list?.add("Interstitial")
        val recyclerAdapter = RecyclerAdapter(list, RecyclerItemListener())
        recycler?.adapter = recyclerAdapter

        // Ask permission from user for location and write external storage
        if (!hasPermissions(this@MainActivity, *PERMISSIONS)) {
            val MULTIPLE_PERMISSIONS_REQUEST_CODE = 123
            ActivityCompat.requestPermissions(this@MainActivity, PERMISSIONS, MULTIPLE_PERMISSIONS_REQUEST_CODE)
        }

    }

    private fun initMoPub() {
        val sdkConfiguration = SdkConfiguration.Builder("1a4a0c6b94ad4217af017c932c3c898e")
                .build()

        MoPub.initializeSdk(this, sdkConfiguration, initSdkListener())
    }

    private fun initSdkListener(): SdkInitializationListener {
        return SdkInitializationListener {
            /* MoPub SDK initialized.
           Check if you should show the consent dialog here, and make your ad requests. */
        }
    }

    fun displayActivity(position: Int){
        val intent :Intent
        if(position == 0){
            intent = Intent(this, BannerActivity::class.java)
        }else{
            intent = Intent(this, InterstitialActivity::class.java)
        }
        startActivity(intent)
    }


    inner class RecyclerItemListener : RecyclerAdapter.OnItemClickListener{
        override fun onItemClick(position: Int) {
            displayActivity(position)
        }

    }







}
