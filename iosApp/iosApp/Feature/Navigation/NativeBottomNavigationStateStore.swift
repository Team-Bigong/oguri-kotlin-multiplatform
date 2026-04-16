import Foundation

private let nativeBottomNavigationStateChangedNotificationName = Notification.Name("OguriNativeBottomNavigationStateChanged")
private let nativeBottomNavigationVisibleKey = "isVisible"
private let nativeBottomNavigationSelectedTabIndexKey = "selectedTabIndex"
private let nativeBottomNavigationSelectedColorArgbKey = "selectedColorArgb"
private let nativeBottomNavigationUnselectedColorArgbKey = "unselectedColorArgb"
private let nativeBottomNavigationHomeTabLabelKey = "homeTabLabel"
private let nativeBottomNavigationCalendarTabLabelKey = "calendarTabLabel"
private let nativeBottomNavigationMyPageTabLabelKey = "myPageTabLabel"

final class NativeBottomNavigationStateStore: ObservableObject {
    @Published var isVisible: Bool = false
    @Published var selectedTabIndex: Int = 0
    @Published var selectedColorArgb: UInt32 = 0xFF0F172A
    @Published var unselectedColorArgb: UInt32 = 0xFF94A3B8
    @Published var homeTabLabel: String = "Home"
    @Published var calendarTabLabel: String = "Calendar"
    @Published var myPageTabLabel: String = "My Page"

    private var observerToken: NSObjectProtocol?

    init() {
        observerToken = NotificationCenter.default.addObserver(
            forName: nativeBottomNavigationStateChangedNotificationName,
            object: nil,
            queue: .main
        ) { [weak self] notification in
            guard let self else { return }
            let userInfo = notification.userInfo
            let isVisibleValue = userInfo?[nativeBottomNavigationVisibleKey] as? NSNumber
            let selectedTabIndexValue = userInfo?[nativeBottomNavigationSelectedTabIndexKey] as? NSNumber
            let selectedColorArgbValue = userInfo?[nativeBottomNavigationSelectedColorArgbKey] as? String
            let unselectedColorArgbValue = userInfo?[nativeBottomNavigationUnselectedColorArgbKey] as? String
            let homeTabLabelValue = userInfo?[nativeBottomNavigationHomeTabLabelKey] as? String
            let calendarTabLabelValue = userInfo?[nativeBottomNavigationCalendarTabLabelKey] as? String
            let myPageTabLabelValue = userInfo?[nativeBottomNavigationMyPageTabLabelKey] as? String

            self.isVisible = isVisibleValue?.boolValue ?? false
            self.selectedTabIndex = selectedTabIndexValue?.intValue ?? 0
            if let selectedColorArgbValue, let parsedSelectedColorArgb = UInt32(selectedColorArgbValue) {
                self.selectedColorArgb = parsedSelectedColorArgb
            }
            if let unselectedColorArgbValue, let parsedUnselectedColorArgb = UInt32(unselectedColorArgbValue) {
                self.unselectedColorArgb = parsedUnselectedColorArgb
            }
            self.homeTabLabel = homeTabLabelValue ?? self.homeTabLabel
            self.calendarTabLabel = calendarTabLabelValue ?? self.calendarTabLabel
            self.myPageTabLabel = myPageTabLabelValue ?? self.myPageTabLabel
        }
    }

    deinit {
        if let observerToken {
            NotificationCenter.default.removeObserver(observerToken)
        }
    }
}
