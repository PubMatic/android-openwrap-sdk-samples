package com.pubmatic.openbid.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.pubmatic.sdk.common.OpenBidSDK;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
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

    private  void initListView() {

        RecyclerView adList =  findViewById(R.id.ad_list);
        adList.setHasFixedSize(true);
        adList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        final List<String> itemList = new ArrayList<>();
        itemList.add("Banner");
        itemList.add("Interstitial");

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        adList.setLayoutManager(layoutManager);

        AdListAdapter adListAdapter = new AdListAdapter(itemList);
        adListAdapter.setItemClickListener(new AdListAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                loadAdScreen(itemList.get(position));
            }
        });
        adList.setAdapter(adListAdapter);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initMoPub();
        if(BuildConfig.DEBUG){
            OpenBidSDK.setLogLevel(OpenBidSDK.LogLevel.Debug);
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
    }

    private void initMoPub(){
        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder("1a4a0c6b94ad4217af017c932c3c898e")
                .build();

        MoPub.initializeSdk(this, sdkConfiguration, initSdkListener());
    }

    private SdkInitializationListener initSdkListener() {
        return new SdkInitializationListener() {
            @Override
            public void onInitializationFinished() {
                   /* MoPub SDK initialized.
                   Check if you should show the consent dialog here, and make your ad requests. */
            }
        };
    }

    public void loadAdScreen(String adType) {
        Intent intent;
        if(adType.equalsIgnoreCase("Banner"))
            intent = new Intent(MainActivity.this, BannerActivity.class);
        else  {
            intent = new Intent(MainActivity.this, InterstitialActivity.class);
        }
        startActivity(intent);
    }
}
