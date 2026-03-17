import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    let urlText = url.absoluteString
                    if urlText.hasPrefix("kakao") {
                        KakaoAuthProvider_iosKt.handleKakaoLoginOpenUrl(url: urlText)
                    } else {
                        DeepLinkStoreProviderKt.handleIncomingAppUrl(urlText: urlText)
                    }
                }
        }
    }
}
