import Foundation
import AmplitudeUnified

final class AmplitudeBridge {
    static let shared: AmplitudeBridge = AmplitudeBridge()

    private static let initializeNotificationName = Notification.Name("OguriAmplitudeInitialize")
    private static let trackNotificationName = Notification.Name("OguriAmplitudeTrack")
    private static let apiKeyKey = "apiKey"
    private static let eventNameKey = "eventName"
    private static let eventPropertiesKey = "eventProperties"

    private var amplitude: Amplitude?
    private var hasStartedObserving: Bool = false

    private init() {}

    func startObserving() {
        guard !hasStartedObserving else { return }
        hasStartedObserving = true
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleInitialize(_:)),
            name: Self.initializeNotificationName,
            object: nil
        )
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleTrack(_:)),
            name: Self.trackNotificationName,
            object: nil
        )
    }

    @objc
    private func handleInitialize(_ notification: Notification) {
        guard
            let userInfo = notification.userInfo,
            let apiKey = userInfo[Self.apiKeyKey] as? String,
            !apiKey.isEmpty
        else {
            return
        }
        if amplitude != nil {
            return
        }
        amplitude = Amplitude(apiKey: apiKey)
    }

    @objc
    private func handleTrack(_ notification: Notification) {
        guard
            let userInfo = notification.userInfo,
            let eventName = userInfo[Self.eventNameKey] as? String,
            !eventName.isEmpty
        else {
            return
        }

        if amplitude == nil {
            handleInitialize(
                Notification(
                    name: Self.initializeNotificationName,
                    object: nil,
                    userInfo: notification.userInfo
                )
            )
        }

        guard let amplitude else {
            return
        }

        if let rawProperties = userInfo[Self.eventPropertiesKey] as? [String: String] {
            amplitude.track(
                eventType: eventName,
                eventProperties: rawProperties
            )
        } else {
            amplitude.track(eventType: eventName)
        }
    }
}
