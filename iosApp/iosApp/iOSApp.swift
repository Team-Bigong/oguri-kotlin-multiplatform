import SwiftUI
import UIKit
import Foundation
import ComposeApp
import KakaoSDKCommon
import GoogleMobileAds

private let iosBannerAdUnitId = "ca-app-pub-2833810411143763/4722770918"
private let iosAppOpenAdUnitId = "ca-app-pub-2833810411143763/4746554741"

@main
struct iOSApp: App {
    init() {
        if let kakaoAppKey = Bundle.main.object(forInfoDictionaryKey: "KEY_KAKAO") as? String, !kakaoAppKey.isEmpty {
            KakaoSDK.initSDK(appKey: kakaoAppKey)
        }
        KakaoShareDispatcher.shared.startObserving()
        OguriAdMobBridge.shared.startObserving()
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

@objc(OguriAdMobBridge)
final class OguriAdMobBridge: NSObject, FullScreenContentDelegate {
    static let shared = OguriAdMobBridge()

    private let notificationCenter = NotificationCenter.default
    private var appOpenAd: AppOpenAd?
    private var isLoadingAppOpenAd = false
    private var isShowingAppOpenAd = false
    private var bannerByContainerId: [ObjectIdentifier: BannerView] = [:]

    private override init() {
        super.init()
    }

    func startObserving() {
        notificationCenter.addObserver(
            self,
            selector: #selector(handleInitializeNotification),
            name: Notification.Name("OguriAdMobInitialize"),
            object: nil,
        )
        notificationCenter.addObserver(
            self,
            selector: #selector(handlePreloadAppOpenNotification),
            name: Notification.Name("OguriAdMobPreloadAppOpen"),
            object: nil,
        )
        notificationCenter.addObserver(
            self,
            selector: #selector(handleShowAppOpenNotification),
            name: Notification.Name("OguriAdMobShowAppOpen"),
            object: nil,
        )
        notificationCenter.addObserver(
            self,
            selector: #selector(handleAttachBannerNotification(_:)),
            name: Notification.Name("OguriAdMobAttachBanner"),
            object: nil,
        )
        notificationCenter.addObserver(
            self,
            selector: #selector(handleDetachBannerNotification(_:)),
            name: Notification.Name("OguriAdMobDetachBanner"),
            object: nil,
        )
    }

    @objc private func handleInitializeNotification() {
        initializeMobileAdsIfNeeded()
    }

    @objc private func handlePreloadAppOpenNotification() {
        preloadAppOpenAd()
    }

    @objc private func handleShowAppOpenNotification() {
        _ = showAppOpenAdIfAvailable()
    }

    @objc private func handleAttachBannerNotification(_ notification: Notification) {
        guard let containerView = notification.object as? UIView else {
            return
        }
        let placementKey = notification.userInfo?["placementKey"] as? String ?? ""
        attachBanner(to: containerView, placementKey: placementKey)
    }

    @objc private func handleDetachBannerNotification(_ notification: Notification) {
        guard let containerView = notification.object as? UIView else {
            return
        }
        detachBanner(from: containerView)
    }

    private func initializeMobileAdsIfNeeded() {
        MobileAds.shared.start(completionHandler: nil)
        preloadAppOpenAd()
    }

    private func preloadAppOpenAd() {
        if appOpenAd != nil || isLoadingAppOpenAd {
            return
        }

        isLoadingAppOpenAd = true
        AppOpenAd.load(
            with: iosAppOpenAdUnitId,
            request: Request(),
            completionHandler: { [weak self] ad, _ in
                guard let self else { return }
                self.appOpenAd = ad
                self.appOpenAd?.fullScreenContentDelegate = self
                self.isLoadingAppOpenAd = false
            },
        )
    }

    @discardableResult
    private func showAppOpenAdIfAvailable() -> Bool {
        if isShowingAppOpenAd {
            return false
        }
        guard let appOpenAd else {
            preloadAppOpenAd()
            return false
        }

        guard let rootViewController = UIApplication.shared.topMostViewController() else {
            return false
        }

        isShowingAppOpenAd = true
        appOpenAd.present(from: rootViewController)
        return true
    }

    private func attachBanner(to containerView: UIView, placementKey: String) {
        let containerId = ObjectIdentifier(containerView)
        if bannerByContainerId[containerId] != nil {
            return
        }

        let adWidth = max(containerView.bounds.width, UIScreen.main.bounds.width - 40)
        let adSize = currentOrientationAnchoredAdaptiveBanner(width: adWidth)
        let bannerView = BannerView(adSize: adSize)
        bannerView.translatesAutoresizingMaskIntoConstraints = false
        bannerView.adUnitID = resolveBannerAdUnitId(placementKey: placementKey)
        bannerView.rootViewController = containerView.enclosingViewController()

        containerView.subviews.forEach { $0.removeFromSuperview() }
        containerView.addSubview(bannerView)
        NSLayoutConstraint.activate([
            bannerView.leadingAnchor.constraint(equalTo: containerView.leadingAnchor),
            bannerView.trailingAnchor.constraint(equalTo: containerView.trailingAnchor),
            bannerView.topAnchor.constraint(equalTo: containerView.topAnchor),
            bannerView.bottomAnchor.constraint(equalTo: containerView.bottomAnchor),
        ])

        bannerByContainerId[containerId] = bannerView
        bannerView.load(Request())
    }

    private func detachBanner(from containerView: UIView) {
        let containerId = ObjectIdentifier(containerView)
        let bannerView = bannerByContainerId.removeValue(forKey: containerId)
        bannerView?.removeFromSuperview()
    }

    private func resolveBannerAdUnitId(placementKey: String) -> String {
        _ = placementKey
        return iosBannerAdUnitId
    }

    func adDidDismissFullScreenContent(_ ad: FullScreenPresentingAd) {
        isShowingAppOpenAd = false
        appOpenAd = nil
        preloadAppOpenAd()
    }

    func ad(_ ad: FullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: any Error) {
        isShowingAppOpenAd = false
        appOpenAd = nil
        preloadAppOpenAd()
    }

    func adWillPresentFullScreenContent(_ ad: FullScreenPresentingAd) {
        isShowingAppOpenAd = true
    }
}

private extension UIApplication {
    func topMostViewController(
        baseViewController: UIViewController? = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap { $0.windows }
            .first { $0.isKeyWindow }?
            .rootViewController,
    ) -> UIViewController? {
        if let navigationController = baseViewController as? UINavigationController {
            return topMostViewController(baseViewController: navigationController.visibleViewController)
        }
        if let tabBarController = baseViewController as? UITabBarController,
           let selectedViewController = tabBarController.selectedViewController {
            return topMostViewController(baseViewController: selectedViewController)
        }
        if let presentedViewController = baseViewController?.presentedViewController {
            return topMostViewController(baseViewController: presentedViewController)
        }
        return baseViewController
    }
}

private extension UIView {
    func enclosingViewController() -> UIViewController? {
        sequence(first: self as UIResponder?, next: { $0?.next }).first { responder in
            responder is UIViewController
        } as? UIViewController
    }
}
