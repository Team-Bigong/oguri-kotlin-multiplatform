import SwiftUI
import ComposeApp
import KakaoSDKCommon
import FirebaseCore
import FirebaseCrashlytics

#if DEBUG
private let firebaseConfigurationPlistName = "GoogleService-Info-Debug"
#else
private let firebaseConfigurationPlistName = "GoogleService-Info-Release"
#endif

@main
struct iOSApp: App {
    init() {
        configureFirebase()
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
