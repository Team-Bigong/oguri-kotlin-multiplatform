import UIKit
import GoogleMobileAds

#if DEBUG
private let iosBannerAdUnitId = "ca-app-pub-3940256099942544/2435281174"
private let iosAppOpenAdUnitId = "ca-app-pub-3940256099942544/5575463023"
#else
private let iosBannerAdUnitId = "ca-app-pub-9643550840413935/1095385664"
private let iosAppOpenAdUnitId = "ca-app-pub-9643550840413935/1058191792"
#endif
private let iosTestDeviceIdentifier = "8ca04675fef46ae5bc7a3764ddea6526"

@objc(OguriAdMobBridge)
final class OguriAdMobBridge: NSObject, FullScreenContentDelegate {
    static let shared = OguriAdMobBridge()

    private let notificationCenter = NotificationCenter.default
    private var isMobileAdsInitialized = false
    private var appOpenAd: AppOpenAd?
    private var isLoadingAppOpenAd = false
    private var isShowingAppOpenAd = false
    private var bannerByContainerId: [ObjectIdentifier: BannerView] = [:]
    private var bannerLoadRetryCountByContainerId: [ObjectIdentifier: Int] = [:]

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
        initializeMobileAdsIfNeeded()
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
        if isMobileAdsInitialized {
            return
        }
        isMobileAdsInitialized = true
        #if DEBUG
        MobileAds.shared.requestConfiguration.testDeviceIdentifiers = ["SIMULATOR", iosTestDeviceIdentifier]
        #endif
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
            completionHandler: { [weak self] ad, error in
                guard let self else { return }
                if let error {
                    print("OguriAdMobBridge: iOS app open failed to load: \(error.localizedDescription)")
                }
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

        let measuredContainerWidth = max(containerView.bounds.width, UIScreen.main.bounds.width - 40)
        let adWidth = max(measuredContainerWidth, 320)
        let adSize = largeAnchoredAdaptiveBanner(width: adWidth)
        let bannerView = BannerView(adSize: adSize)
        bannerView.translatesAutoresizingMaskIntoConstraints = false
        bannerView.adUnitID = resolveBannerAdUnitId(placementKey: placementKey)
        bannerView.delegate = self

        containerView.subviews.forEach { $0.removeFromSuperview() }
        containerView.addSubview(bannerView)
        NSLayoutConstraint.activate([
            bannerView.centerXAnchor.constraint(equalTo: containerView.centerXAnchor),
            bannerView.centerYAnchor.constraint(equalTo: containerView.centerYAnchor),
            bannerView.leadingAnchor.constraint(greaterThanOrEqualTo: containerView.leadingAnchor),
            bannerView.trailingAnchor.constraint(lessThanOrEqualTo: containerView.trailingAnchor),
            bannerView.topAnchor.constraint(greaterThanOrEqualTo: containerView.topAnchor),
            bannerView.bottomAnchor.constraint(lessThanOrEqualTo: containerView.bottomAnchor),
        ])

        bannerByContainerId[containerId] = bannerView
        bannerLoadRetryCountByContainerId[containerId] = 0
        loadBannerWhenViewControllerIsReady(containerView: containerView)
    }

    private func detachBanner(from containerView: UIView) {
        let containerId = ObjectIdentifier(containerView)
        let bannerView = bannerByContainerId.removeValue(forKey: containerId)
        bannerView?.removeFromSuperview()
        bannerLoadRetryCountByContainerId.removeValue(forKey: containerId)
    }

    private func resolveBannerAdUnitId(placementKey: String) -> String {
        switch placementKey {
        case "CALENDAR_INLINE", "PHOTO_DETAIL_BOTTOM":
            return iosBannerAdUnitId
        default:
            return iosBannerAdUnitId
        }
    }

    private func loadBannerWhenViewControllerIsReady(containerView: UIView) {
        let containerId = ObjectIdentifier(containerView)
        guard let bannerView = bannerByContainerId[containerId] else {
            return
        }

        let rootViewController = containerView.enclosingViewController() ?? UIApplication.shared.topMostViewController()
        guard let rootViewController else {
            scheduleBannerLoadRetry(containerView: containerView)
            return
        }

        bannerView.rootViewController = rootViewController
        if bannerView.adUnitID?.isEmpty != false {
            print("OguriAdMobBridge: iOS banner adUnitID is empty")
            return
        }
        bannerView.load(Request())
    }

    private func scheduleBannerLoadRetry(containerView: UIView) {
        let containerId = ObjectIdentifier(containerView)
        let currentRetryCount = bannerLoadRetryCountByContainerId[containerId] ?? 0
        if currentRetryCount >= 20 {
            return
        }
        bannerLoadRetryCountByContainerId[containerId] = currentRetryCount + 1

        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) { [weak self, weak containerView] in
            guard let self, let containerView else { return }
            self.loadBannerWhenViewControllerIsReady(containerView: containerView)
        }
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

extension OguriAdMobBridge: BannerViewDelegate {
    func bannerViewDidReceiveAd(_ bannerView: BannerView) {
        print("OguriAdMobBridge: iOS banner did receive ad")
    }

    func bannerView(_ bannerView: BannerView, didFailToReceiveAdWithError error: any Error) {
        print("OguriAdMobBridge: iOS banner failed to load: \(error.localizedDescription)")
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
