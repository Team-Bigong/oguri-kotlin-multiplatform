import Foundation

private let floatingBottomNavigationStateChangedNotificationName = Notification.Name("OguriFloatingBottomNavigationStateChanged")
private let floatingBottomNavigationVisibleKey = "isVisible"
private let floatingBottomNavigationSelectedTabIndexKey = "selectedTabIndex"
private let floatingBottomNavigationSelectedColorArgbKey = "selectedColorArgb"
private let floatingBottomNavigationUnselectedColorArgbKey = "unselectedColorArgb"
private let floatingBottomNavigationBackgroundColorArgbKey = "backgroundColorArgb"
private let floatingBottomNavigationHomeTabLabelKey = "homeTabLabel"
private let floatingBottomNavigationCalendarTabLabelKey = "calendarTabLabel"
private let floatingBottomNavigationMyPageTabLabelKey = "myPageTabLabel"

final class FloatingBottomNavigationStateStore: ObservableObject {
    @Published var isVisible: Bool = false
    @Published var selectedTabIndex: Int = 0
    @Published var selectedColorArgb: UInt32 = 0
    @Published var unselectedColorArgb: UInt32 = 0
    @Published var backgroundColorArgb: UInt32 = 0
    @Published var homeTabLabel: String = "Home"
    @Published var calendarTabLabel: String = "Calendar"
    @Published var myPageTabLabel: String = "My Page"

    private var observerToken: NSObjectProtocol?

    init() {
        observerToken = NotificationCenter.default.addObserver(
            forName: floatingBottomNavigationStateChangedNotificationName,
            object: nil,
            queue: .main
        ) { [weak self] notification in
            guard let self else { return }
            let userInfo = notification.userInfo
            let isVisibleValue = userInfo?[floatingBottomNavigationVisibleKey] as? NSNumber
            let selectedTabIndexValue = userInfo?[floatingBottomNavigationSelectedTabIndexKey] as? NSNumber
            let selectedColorArgbValue = userInfo?[floatingBottomNavigationSelectedColorArgbKey] as? String
            let unselectedColorArgbValue = userInfo?[floatingBottomNavigationUnselectedColorArgbKey] as? String
            let backgroundColorArgbValue = userInfo?[floatingBottomNavigationBackgroundColorArgbKey] as? String
            let homeTabLabelValue = userInfo?[floatingBottomNavigationHomeTabLabelKey] as? String
            let calendarTabLabelValue = userInfo?[floatingBottomNavigationCalendarTabLabelKey] as? String
            let myPageTabLabelValue = userInfo?[floatingBottomNavigationMyPageTabLabelKey] as? String

            self.isVisible = isVisibleValue?.boolValue ?? false
            self.selectedTabIndex = selectedTabIndexValue?.intValue ?? 0
            if let selectedColorArgbValue, let parsedSelectedColorArgb = UInt32(selectedColorArgbValue) {
                self.selectedColorArgb = parsedSelectedColorArgb
            }
            if let unselectedColorArgbValue, let parsedUnselectedColorArgb = UInt32(unselectedColorArgbValue) {
                self.unselectedColorArgb = parsedUnselectedColorArgb
            }
            if let backgroundColorArgbValue, let parsedBackgroundColorArgb = UInt32(backgroundColorArgbValue) {
                self.backgroundColorArgb = parsedBackgroundColorArgb
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
