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

    private var hasStartedObserving: Bool = false

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
        let fallbackText = "\(fallbackMessage)\n\n\(fallbackUrl)"

        guard
            let topViewController = UIApplication.shared.topMostViewController()
        else {
            return
        }

        guard
            ShareApi.isKakaoTalkSharingAvailable(),
            let deepLinkUrl = URL(string: deepLinkUrlText),
            let imageUrl = URL(string: imageUrlText),
            !title.isEmpty,
            !buttonTitle.isEmpty
        else {
            presentFallbackShare(text: fallbackText, from: topViewController)
            return
        }

        let link = Link(webUrl: deepLinkUrl, mobileWebUrl: deepLinkUrl)
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
                UIApplication.shared.open(sharingUrl, options: [:], completionHandler: nil)
            } else {
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
