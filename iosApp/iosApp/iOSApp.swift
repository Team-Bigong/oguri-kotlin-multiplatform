import SwiftUI
import ComposeApp
import KakaoSDKCommon

@main
struct iOSApp: App {
    init() {
        if let kakaoAppKey = Bundle.main.object(forInfoDictionaryKey: "KEY_KAKAO") as? String, !kakaoAppKey.isEmpty {
            KakaoSDK.initSDK(appKey: kakaoAppKey)
        }
        KakaoShareDispatcher.shared.startObserving()
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
}
