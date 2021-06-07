**This is an Android feeds/list application written in Kotlin to 
demonstrate the use of OpenWrap SDK + TAM parallel header bidding into 
GAM SDK.**
-----------------------------------------------------------------------
To use this application, you need to open the application in Android 
Studio and run the application on Android Device or emulator.

- By default feed list contains 100 feed items and at every 10th interval 
  banner ad is shown, to update these default values you need to open 
  constants.kt file and follow below steps
  - Search for MAX_FEEDS constant and update its value as per your 
      requirement
  - To update banner interval search for BANNER_INTERVAL and update its 
      value as per your requirement

- By default ad unit id's are set to In-Banner Video ads with
  size 300x250, to update it to Display Banner you need to open
  constants.kt file and update PROFILE_ID constant value to Display 
  Banner profile id as mentioned in the inline comments.

  


