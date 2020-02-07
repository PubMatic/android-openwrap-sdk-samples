package sdk.pubmatic.com.javasample.dummyadserver;

import android.content.Context;
import android.view.View;

/**
 * Emulates an Ad Server SDK
 */
public class DummyAdServerSDK {

    private DummyAdServerEventListener adServerEventListener;
    private Context context;
    private String adUnitId;
    /**
     * Constructor
     * @param context Android Application context
     * @param adUnitId Ad unit Id
     */
    public DummyAdServerSDK(Context context, String adUnitId){
        this.context = context;
        this.adUnitId = adUnitId;
    }

    /**
     * Setter to set Listener
     * @param adServerEventListener reference of DummyAdServerEventListener
     */
    public void setAdServerEventListener(DummyAdServerEventListener adServerEventListener) {
        this.adServerEventListener = adServerEventListener;
    }

    /**
     * Sets custom targeting to be sent in the ad call
     * @param customTargetting custom targetting params
     */
    public void setCustomTargetting(String customTargetting){
        // Sets custom targeting to be sent in the ad call
    }

    /**
     * loads a banner ad from the ad server
     */
    public void loadBannerAd(){
        if(null == context){
            if(null != adServerEventListener){
                adServerEventListener.onAdFailed(new DummyError(-1, "Internal Error: Context should not be null."));
            }
            return;
        }
        // Usually, the ad server determines whether the partner bid won in the
        // auction, based on provided targeting information. Then, the ad server SDK
        // will either render the banner ad or indicate that a partner ad should be
        // rendered.
        if(null != adServerEventListener){
            if("OtherASBannerAdUnit".equals(adUnitId)){
                adServerEventListener.onCustomEventReceived("SomeCustomEvent");
            }else {
                adServerEventListener.onBannerLoaded(new View(context));
            }
        }
    }

    /**
     * loads an interstitial ad from the ad server
     */
    public void loadInterstitialAd(){
        // Usually, the ad server determines whether the partner bid won in the
        // auction, based on provided targeting information. Then, the ad server SDK
        // will either load the interstitial ad or indicate that a partner ad should
        // be rendered.
        if(null != adServerEventListener){
            if("OtherASInterstitialAdUnit".equals(adUnitId)){
                adServerEventListener.onCustomEventReceived("SomeCustomEvent");
            }else {
                adServerEventListener.onInterstitialReceived();
            }
        }
    }

    /**
     * Presents an interstitial ad
     */
    public void showInterstitialAd(){
        // This implementation of your ad server SDK's interstitial ad presents an
        // interstitial ad
    }

    /**
     * method to do clean up
     */
    public void destroy(){
        adServerEventListener = null;
        context = null;
    }

    /**
     * Listener to receive ad success/failure events.
     */
    public static class DummyAdServerEventListener {

        /**
         * A dummy custom event triggered based on targeting information sent in the request.
         * @param event value
         */
        public void onCustomEventReceived(String event){
            //Callback method
        }

        /**
         * called when a banner ad is loaded
         * @param view dummy view
         */
        public void onBannerLoaded(View view){
            //Callback method
        }

        /**
         * called when a interstitial ad is loaded
         */
        public void onInterstitialReceived(){
            //Callback method
        }

        /**
         * called when the SDK fails to load an ad
         * @param dummyError Dummy error
         */
        public void onAdFailed(DummyError dummyError){
            //Callback method
        }
    }

    /**
     * Emulated the Error class
     */
    public class DummyError{
        private int errorCode;
        private String errorMsg;

        DummyError(int errorCode, String errorMsg){
            this.errorCode = errorCode;
            this.errorMsg = errorMsg;
        }

        public int getErrorCode() {
            return errorCode;
        }

        public String getErrorMsg() {
            return errorMsg;
        }
    }
}
