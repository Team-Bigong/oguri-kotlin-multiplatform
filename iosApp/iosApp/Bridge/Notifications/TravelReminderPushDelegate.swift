import ComposeApp
import FirebaseCore
import FirebaseMessaging
import UIKit
import UserNotifications

final class TravelReminderPushDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate, TravelReminderPushBridge {
    private var isAPNsRegistered = false
    private var registrationInProgress = false
    private var tokenWaiters: [(String?) -> Void] = []

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        if FirebaseApp.app() != nil {
            Messaging.messaging().delegate = self
        }
        TravelReminderNotifications_iosKt.installTravelReminderPushBridge(bridge: self)
        return true
    }

    func requestPermission(onResult: @escaping (KotlinBoolean) -> Void) {
        let center = UNUserNotificationCenter.current()
        center.getNotificationSettings { settings in
            switch settings.authorizationStatus {
            case .authorized, .provisional, .ephemeral:
                onResult(KotlinBoolean(value: true))
            case .notDetermined:
                center.requestAuthorization(options: [.alert, .sound]) { granted, _ in
                    onResult(KotlinBoolean(value: granted))
                }
            default:
                onResult(KotlinBoolean(value: false))
            }
        }
    }

    func currentToken(onResult: @escaping (String?) -> Void) {
        DispatchQueue.main.async {
            guard FirebaseApp.app() != nil else {
                onResult(nil)
                return
            }
            Messaging.messaging().delegate = self
            if self.isAPNsRegistered {
                Messaging.messaging().token { token, _ in onResult(token) }
                return
            }
            UNUserNotificationCenter.current().getNotificationSettings { settings in
                let allowed = [.authorized, .provisional, .ephemeral].contains(settings.authorizationStatus)
                DispatchQueue.main.async {
                    guard allowed else {
                        onResult(nil)
                        return
                    }
                    self.tokenWaiters.append(onResult)
                    if !self.registrationInProgress {
                        self.registrationInProgress = true
                        UIApplication.shared.registerForRemoteNotifications()
                    }
                }
            }
        }
    }

    func clearToken(onComplete: @escaping () -> Void) {
        DispatchQueue.main.async {
            guard FirebaseApp.app() != nil else {
                onComplete()
                return
            }
            let center = UNUserNotificationCenter.current()
            center.getDeliveredNotifications { delivered in
                center.removeDeliveredNotifications(withIdentifiers: delivered.compactMap { item in
                    item.request.content.userInfo["travelReminderId"] == nil ? nil : item.request.identifier
                })
            }
            Messaging.messaging().deleteToken { _ in onComplete() }
        }
    }

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        isAPNsRegistered = true
        Messaging.messaging().apnsToken = deviceToken
        Messaging.messaging().token { token, _ in
            DispatchQueue.main.async { self.finishRegistration(token) }
        }
    }

    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        finishRegistration(nil)
    }

    private func finishRegistration(_ token: String?) {
        let waiters = tokenWaiters
        tokenWaiters.removeAll()
        registrationInProgress = false
        waiters.forEach { $0(token) }
    }

    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        if fcmToken != nil {
            TravelReminderNotificationsKt.onTravelReminderTokenChanged()
        }
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completion: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completion(notification.request.content.userInfo["travelReminderId"] == nil ? [] : [.banner, .list, .sound])
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completion: @escaping () -> Void
    ) {
        if let reminderId = response.notification.request.content.userInfo["travelReminderId"] as? String {
            TravelReminderNotificationsKt.handleTravelReminderNotification(reminderId: reminderId)
        }
        completion()
    }
}
