import SwiftUI
import ComposeApp
import KakaoSDKCommon
import FirebaseCore
import FirebaseCrashlytics
import GoogleMaps

#if DEBUG
private let firebaseConfigurationPlistName = "GoogleService-Info-Debug"
#else
private let firebaseConfigurationPlistName = "GoogleService-Info-Release"
#endif

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(TravelReminderPushDelegate.self) private var reminderDelegate
    init() {
        configureFirebase()
        if let key = Bundle.main.object(forInfoDictionaryKey: "GOOGLE_MAPS_IOS_API_KEY") as? String,
           !key.isEmpty, !key.hasPrefix("$("), GMSServices.provideAPIKey(key) {
            PlatformTravelMap_iosKt.installTravelMapHostFactory(value: GoogleTravelMapFactory())
        }
        if let kakaoAppKey = Bundle.main.object(forInfoDictionaryKey: "KEY_KAKAO") as? String, !kakaoAppKey.isEmpty {
            KakaoSDK.initSDK(appKey: kakaoAppKey)
        }
        AmplitudeBridge.shared.startObserving()
        KakaoShareDispatcher.shared.startObserving()
        OguriAdMobBridge.shared.startObserving()
        OguriThemeModeBridge.shared.startObserving()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onContinueUserActivity(NSUserActivityTypeBrowsingWeb) { activity in
                    if let url = activity.webpageURL {
                        DeepLinkStoreProviderKt.handleIncomingAppUrl(urlText: url.absoluteString)
                    }
                }
                .onOpenURL { url in
                    let urlText = url.absoluteString
                    if urlText.hasPrefix("kakao"), urlText.contains("://oauth") {
                        KakaoAuthProvider_iosKt.handleKakaoLoginOpenUrl(url: urlText)
                    } else {
                        DeepLinkStoreProviderKt.handleIncomingAppUrl(urlText: urlText)
                    }
                }
        }
    }

    private func configureFirebase() {
        if FirebaseApp.app() != nil {
            return
        }
        guard let filePath = Bundle.main.path(forResource: firebaseConfigurationPlistName, ofType: "plist"),
              let options = FirebaseOptions(contentsOfFile: filePath) else {
            assertionFailure("Firebase plist not found: \(firebaseConfigurationPlistName).plist")
            return
        }
        FirebaseApp.configure(options: options)
        #if DEBUG
        Crashlytics.crashlytics().setCrashlyticsCollectionEnabled(false)
        #else
        Crashlytics.crashlytics().setCrashlyticsCollectionEnabled(true)
        #endif
    }
}
