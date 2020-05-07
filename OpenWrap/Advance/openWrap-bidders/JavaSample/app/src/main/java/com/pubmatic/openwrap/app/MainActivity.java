package com.pubmatic.openwrap.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.pubmatic.sdk.common.OpenWrapSDK;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

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

        final List<AdType> itemList = new ArrayList<>(Arrays.asList(AdType.values()));


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
    }


    public void loadAdScreen(@NonNull Class activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
    }


    enum AdType {
        BANNER(BannerActivity.class, "Banner"),
        INTERSTITIAL(InterstitialActivity.class, "Interstitial");

        private Class activity;
        private String displayName;

        AdType(Class activity, String displayName) {
            this.activity = activity;
            this.displayName = displayName;
        }

        public Class getActivity() {
            return activity;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
