import UIKit
import SwiftUI
import ComposeApp

private let nativeBottomNavigationStateChangedNotificationName = Notification.Name("OguriNativeBottomNavigationStateChanged")
private let nativeBottomNavigationVisibleKey = "isVisible"
private let nativeBottomNavigationSelectedTabIndexKey = "selectedTabIndex"
private let nativeBottomNavigationSelectedColorArgbKey = "selectedColorArgb"
private let nativeBottomNavigationUnselectedColorArgbKey = "unselectedColorArgb"
private let nativeBottomNavigationHomeTabLabelKey = "homeTabLabel"
private let nativeBottomNavigationCalendarTabLabelKey = "calendarTabLabel"
private let nativeBottomNavigationMyPageTabLabelKey = "myPageTabLabel"
private let bottomTabIconPointSize: CGFloat = 18
private let bottomTabTitleFontSize: CGFloat = 11
private let defaultBottomTabNormalColor = UIColor(red: 0.58, green: 0.64, blue: 0.72, alpha: 1.0) // #94A3B8
private let defaultBottomTabSelectedColor = UIColor(red: 0.06, green: 0.09, blue: 0.16, alpha: 1.0) // #0F172A

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

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

struct NativeBottomTabBarView: UIViewRepresentable {
    final class Coordinator: NSObject, UITabBarDelegate {
        private let onTabSelected: (Int) -> Void

        init(onTabSelected: @escaping (Int) -> Void) {
            self.onTabSelected = onTabSelected
        }

        func tabBar(_ tabBar: UITabBar, didSelect item: UITabBarItem) {
            onTabSelected(item.tag)
        }
    }

    let selectedTabIndex: Int
    let selectedColorArgb: UInt32
    let unselectedColorArgb: UInt32
    let homeTabLabel: String
    let calendarTabLabel: String
    let myPageTabLabel: String
    let onTabSelected: (Int) -> Void

    func makeCoordinator() -> Coordinator {
        Coordinator(onTabSelected: onTabSelected)
    }

    private func color(from argb: UInt32, fallback: UIColor) -> UIColor {
        let alpha = CGFloat((argb >> 24) & 0xFF) / 255.0
        let red = CGFloat((argb >> 16) & 0xFF) / 255.0
        let green = CGFloat((argb >> 8) & 0xFF) / 255.0
        let blue = CGFloat(argb & 0xFF) / 255.0
        if alpha <= 0 {
            return fallback
        }
        return UIColor(red: red, green: green, blue: blue, alpha: alpha)
    }

    private func applyTabBarAppearance(tabBar: UITabBar) {
        let selectedColor = color(from: selectedColorArgb, fallback: defaultBottomTabSelectedColor)
        let unselectedColor = color(from: unselectedColorArgb, fallback: defaultBottomTabNormalColor)
        let itemTitleFont = UIFont.systemFont(ofSize: bottomTabTitleFontSize, weight: .regular)

        let tabBarAppearance = UITabBarAppearance()
        tabBarAppearance.configureWithDefaultBackground()
        tabBarAppearance.backgroundColor = UIColor(red: 0.97, green: 0.98, blue: 0.99, alpha: 0.9)

        func configureItemAppearance(_ itemAppearance: UITabBarItemAppearance) {
            itemAppearance.normal.iconColor = unselectedColor
            itemAppearance.normal.titleTextAttributes = [
                .foregroundColor: unselectedColor,
                .font: itemTitleFont
            ]
            itemAppearance.selected.iconColor = selectedColor
            itemAppearance.selected.titleTextAttributes = [
                .foregroundColor: selectedColor,
                .font: itemTitleFont
            ]
        }

        configureItemAppearance(tabBarAppearance.stackedLayoutAppearance)
        configureItemAppearance(tabBarAppearance.inlineLayoutAppearance)
        configureItemAppearance(tabBarAppearance.compactInlineLayoutAppearance)

        tabBar.standardAppearance = tabBarAppearance
        if #available(iOS 15.0, *) {
            tabBar.scrollEdgeAppearance = tabBarAppearance
        }
        tabBar.tintColor = selectedColor
        tabBar.unselectedItemTintColor = unselectedColor
        tabBar.items?.forEach { item in
            item.titlePositionAdjustment = UIOffset(horizontal: 0, vertical: 0)
        }
    }

    func makeUIView(context: Context) -> UITabBar {
        let tabBar = UITabBar()
        tabBar.delegate = context.coordinator
        tabBar.itemPositioning = .automatic
        tabBar.isTranslucent = true

        let symbolConfiguration = UIImage.SymbolConfiguration(pointSize: bottomTabIconPointSize, weight: .regular)
        let homeItem = UITabBarItem(
            title: homeTabLabel,
            image: UIImage(systemName: "house", withConfiguration: symbolConfiguration)?.withRenderingMode(.alwaysTemplate),
            selectedImage: UIImage(systemName: "house", withConfiguration: symbolConfiguration)?.withRenderingMode(.alwaysTemplate)
        )
        homeItem.tag = 0
        let calendarItem = UITabBarItem(
            title: calendarTabLabel,
            image: UIImage(systemName: "calendar", withConfiguration: symbolConfiguration)?.withRenderingMode(.alwaysTemplate),
            selectedImage: UIImage(systemName: "calendar", withConfiguration: symbolConfiguration)?.withRenderingMode(.alwaysTemplate)
        )
        calendarItem.tag = 1
        let myPageItem = UITabBarItem(
            title: myPageTabLabel,
            image: UIImage(systemName: "person", withConfiguration: symbolConfiguration)?.withRenderingMode(.alwaysTemplate),
            selectedImage: UIImage(systemName: "person", withConfiguration: symbolConfiguration)?.withRenderingMode(.alwaysTemplate)
        )
        myPageItem.tag = 2
        tabBar.items = [homeItem, calendarItem, myPageItem]
        tabBar.selectedItem = tabBar.items?[safe: selectedTabIndex]
        applyTabBarAppearance(tabBar: tabBar)
        tabBar.layer.cornerRadius = 24
        tabBar.layer.masksToBounds = true

        return tabBar
    }

    func updateUIView(_ uiView: UITabBar, context: Context) {
        if let items = uiView.items, items.count >= 3 {
            items[0].title = homeTabLabel
            items[1].title = calendarTabLabel
            items[2].title = myPageTabLabel
        }
        applyTabBarAppearance(tabBar: uiView)
        uiView.selectedItem = uiView.items?[safe: selectedTabIndex]
    }
}

private extension Collection {
    subscript(safe index: Index) -> Element? {
        indices.contains(index) ? self[index] : nil
    }
}

struct ContentView: View {
    @StateObject private var nativeBottomNavigationStateStore = NativeBottomNavigationStateStore()

    var body: some View {
        ZStack(alignment: .bottom) {
            ComposeView()
                .ignoresSafeArea()

            if nativeBottomNavigationStateStore.isVisible {
                NativeBottomTabBarView(
                    selectedTabIndex: nativeBottomNavigationStateStore.selectedTabIndex,
                    selectedColorArgb: nativeBottomNavigationStateStore.selectedColorArgb,
                    unselectedColorArgb: nativeBottomNavigationStateStore.unselectedColorArgb,
                    homeTabLabel: nativeBottomNavigationStateStore.homeTabLabel,
                    calendarTabLabel: nativeBottomNavigationStateStore.calendarTabLabel,
                    myPageTabLabel: nativeBottomNavigationStateStore.myPageTabLabel,
                    onTabSelected: handleNativeTabSelected
                )
                .frame(height: 78)
                .padding(.horizontal, 12)
                .padding(.bottom, 8)
                .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .animation(.easeInOut(duration: 0.2), value: nativeBottomNavigationStateStore.isVisible)
    }

    private func handleNativeTabSelected(_ selectedTabIndex: Int) {
        if selectedTabIndex < 0 || selectedTabIndex > 2 {
            return
        }
        NativeBottomNavigationBridgeKt.emitNativeBottomNavigationSelection(tabIndex: Int32(selectedTabIndex))
    }
}
