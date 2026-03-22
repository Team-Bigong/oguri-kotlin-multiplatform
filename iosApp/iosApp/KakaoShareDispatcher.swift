import Foundation
import UIKit
import KakaoSDKShare
import KakaoSDKTemplate

final class KakaoShareDispatcher {
    static let shared: KakaoShareDispatcher = KakaoShareDispatcher()

    private static let notificationName = Notification.Name("OguriKakaoShareRequest")
    private static let titleKey = "title"
    private static let descriptionKey = "description"
    private static let imageUrlKey = "imageUrl"
    private static let deepLinkUrlKey = "deepLinkUrl"
    private static let buttonTitleKey = "buttonTitle"
    private static let fallbackMessageKey = "fallbackMessage"
    private static let fallbackUrlKey = "fallbackUrl"
    private static let isPreloadKey = "isPreload"
    private static let executionDeepLinkKey = "deeplink"

    private var hasStartedObserving: Bool = false
    private var cachedSharingUrlByPayloadKey: [String: URL] = [:]

    private init() {}

    func startObserving() {
        guard !hasStartedObserving else { return }
        hasStartedObserving = true
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleShareRequest(_:)),
            name: Self.notificationName,
            object: nil
        )
    }

    @objc
    private func handleShareRequest(_ notification: Notification) {
        guard let userInfo = notification.userInfo else { return }

        let title = (userInfo[Self.titleKey] as? String) ?? ""
        let description = (userInfo[Self.descriptionKey] as? String) ?? ""
        let imageUrlText = (userInfo[Self.imageUrlKey] as? String) ?? ""
        let deepLinkUrlText = (userInfo[Self.deepLinkUrlKey] as? String) ?? ""
        let buttonTitle = (userInfo[Self.buttonTitleKey] as? String) ?? ""
        let fallbackMessage = (userInfo[Self.fallbackMessageKey] as? String) ?? ""
        let fallbackUrl = (userInfo[Self.fallbackUrlKey] as? String) ?? ""
        let isPreload = (userInfo[Self.isPreloadKey] as? Bool) ?? false
        let fallbackText = "\(fallbackMessage)\n\n\(fallbackUrl)"
        let payloadKey = "\(title)|\(description)|\(imageUrlText)|\(deepLinkUrlText)|\(buttonTitle)"

        guard
            let topViewController = UIApplication.shared.topMostViewController()
        else {
            return
        }

        if let cachedSharingUrl = cachedSharingUrlByPayloadKey[payloadKey] {
            if isPreload {
                return
            }
            UIApplication.shared.open(cachedSharingUrl, options: [:], completionHandler: nil)
            return
        }

        guard
            ShareApi.isKakaoTalkSharingAvailable(),
            let deepLinkUrl = URL(string: deepLinkUrlText),
            let imageUrl = URL(string: imageUrlText),
            !title.isEmpty,
            !buttonTitle.isEmpty
        else {
            if isPreload {
                return
            }
            presentFallbackShare(text: fallbackText, from: topViewController)
            return
        }

        let executionParams = [Self.executionDeepLinkKey: deepLinkUrlText]
        let link = Link(
            webUrl: deepLinkUrl,
            mobileWebUrl: deepLinkUrl,
            androidExecutionParams: executionParams,
            iosExecutionParams: executionParams
        )
        let template = FeedTemplate(
            content: Content(
                title: title,
                imageUrl: imageUrl,
                description: description,
                link: link
            ),
            buttons: [
                Button(
                    title: buttonTitle,
                    link: link
                )
            ]
        )

        ShareApi.shared.shareDefault(templatable: template) { sharingResult, error in
            if let sharingUrl = sharingResult?.url, error == nil {
                self.cachedSharingUrlByPayloadKey[payloadKey] = sharingUrl
                if isPreload {
                    return
                }
                UIApplication.shared.open(sharingUrl, options: [:], completionHandler: nil)
            } else {
                if isPreload {
                    return
                }
                self.presentFallbackShare(text: fallbackText, from: topViewController)
            }
        }
    }

    private func presentFallbackShare(
        text: String,
        from viewController: UIViewController
    ) {
        guard !text.isEmpty else { return }
        let activityViewController = UIActivityViewController(
            activityItems: [text],
            applicationActivities: nil
        )
        viewController.present(
            activityViewController,
            animated: true,
            completion: nil
        )
    }
}

private extension UIApplication {
    func topMostViewController() -> UIViewController? {
        let rootViewController: UIViewController? =
            connectedScenes
                .compactMap { $0 as? UIWindowScene }
                .flatMap { $0.windows }
                .first { $0.isKeyWindow }?
                .rootViewController

        guard var currentViewController = rootViewController else {
            return nil
        }

        while let presentedViewController = currentViewController.presentedViewController {
            currentViewController = presentedViewController
        }
        return currentViewController
    }
}
