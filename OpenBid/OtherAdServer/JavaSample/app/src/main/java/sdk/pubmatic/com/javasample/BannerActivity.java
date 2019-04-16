package sdk.pubmatic.com.javasample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.pubmatic.sdk.common.OpenBidSDK;
import com.pubmatic.sdk.common.POBError;
import com.pubmatic.sdk.common.models.POBApplicationInfo;
import com.pubmatic.sdk.openbid.banner.POBBannerView;

import java.net.MalformedURLException;
import java.net.URL;

import sdk.pubmatic.com.javasample.customhandler.CustomBannerEventHandler;

public class BannerActivity extends AppCompatActivity {

    private static final String OPENWRAP_AD_UNIT_ID = "OtherASBannerAdUnit";
    private static final String PUB_ID = "156276";
    private static final int PROFILE_ID = 1165;
    private static final String DUMMY_AD_SERVER_AD_UNIT_ID = "OtherASBannerAdUnit";

    private POBBannerView banner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);

        // A valid Play Store Url of an Android application is required.
        POBApplicationInfo appInfo = new POBApplicationInfo();
        try {
            appInfo.setStoreURL(new URL("https://play.google.com/store/apps/details?id=com.example.android&hl=en"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // This app information is a global configuration & you
        // Need not set this for every ad request(of any ad type)
        OpenBidSDK.setApplicationInfo(appInfo);

        // Create a banner custom event handler for your ad server. Make sure you use
        // separate event handler objects to create each banner view.
        // For example, The code below creates an event handler for Primary ad server.
        CustomBannerEventHandler eventHandler = new CustomBannerEventHandler(this, DUMMY_AD_SERVER_AD_UNIT_ID);

        // Initialise banner view
        banner = findViewById(R.id.banner);
        banner.init(PUB_ID,
                PROFILE_ID,
                OPENWRAP_AD_UNIT_ID,
                eventHandler);


        //optional listener to listen banner events
        banner.setListener(new POBBannerViewListener());

        // Call loadAd() on banner instance
        banner.loadAd();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Destroy banner before destroying activity
        if (null != banner) {
            banner.destroy();
        }
    }


    class POBBannerViewListener extends POBBannerView.POBBannerViewListener {
        private final String TAG = "POBBannerViewListener";

        // Callback method Notifies that an ad has been successfully loaded and rendered.
        @Override
        public void onAdReceived(POBBannerView view) {
            Log.d(TAG, "Ad Received");
        }

        // Callback method Notifies an error encountered while loading or rendering an ad.
        @Override
        public void onAdFailed(POBBannerView view, POBError error) {
            Log.e(TAG, error.toString());
        }

        // Callback method notifies ad click
        @Override
        public void onAdClick(POBBannerView view) {
            Log.d(TAG, "onAdClicked");
        }

        // Callback method Notifies that the banner ad view will launch a dialog on top of the current view
        @Override
        public void onAdOpened(POBBannerView view) {
            Log.d(TAG, "Ad Opened");
        }

        // Callback method Notifies that the banner ad view has dismissed the modal on top of the current view
        @Override
        public void onAdClosed(POBBannerView view) {
            Log.d(TAG, "Ad Closed");
        }

    }


}

