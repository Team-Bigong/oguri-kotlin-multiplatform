import UIKit

@objc(OguriThemeModeBridge)
final class OguriThemeModeBridge: NSObject {
    static let shared = OguriThemeModeBridge()

    private let notificationCenter = NotificationCenter.default

    private override init() {
        super.init()
    }

    func startObserving() {
        notificationCenter.addObserver(
            self,
            selector: #selector(handleApplyThemeModeNotification(_:)),
            name: Notification.Name("OguriApplyThemeMode"),
            object: nil,
        )
    }

    @objc private func handleApplyThemeModeNotification(_ notification: Notification) {
        let isDarkThemeEnabled = notification.userInfo?["isDarkThemeEnabled"] as? Bool ?? false
        let shouldFollowSystemTheme = notification.userInfo?["shouldFollowSystemTheme"] as? Bool ?? true
        applyThemeMode(isDarkThemeEnabled: isDarkThemeEnabled, shouldFollowSystemTheme: shouldFollowSystemTheme)
    }

    private func applyThemeMode(isDarkThemeEnabled: Bool, shouldFollowSystemTheme: Bool) {
        let interfaceStyle: UIUserInterfaceStyle
        if shouldFollowSystemTheme {
            interfaceStyle = .unspecified
        } else if isDarkThemeEnabled {
            interfaceStyle = .dark
        } else {
            interfaceStyle = .light
        }

        DispatchQueue.main.async {
            UIApplication.shared.connectedScenes
                .compactMap { $0 as? UIWindowScene }
                .flatMap { $0.windows }
                .forEach { window in
                    if window.overrideUserInterfaceStyle != interfaceStyle {
                        window.overrideUserInterfaceStyle = interfaceStyle
                    }
                }
        }
    }
}
