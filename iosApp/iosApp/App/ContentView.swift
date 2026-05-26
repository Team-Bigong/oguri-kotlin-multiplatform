import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    @StateObject private var floatingBottomNavigationStateStore = FloatingBottomNavigationStateStore()

    var body: some View {
        ZStack(alignment: .bottom) {
            ComposeView()
                .ignoresSafeArea()

            if floatingBottomNavigationStateStore.isVisible {
                FloatingBottomTabBarView(
                    selectedTabIndex: floatingBottomNavigationStateStore.selectedTabIndex,
                    selectedColorArgb: floatingBottomNavigationStateStore.selectedColorArgb,
                    unselectedColorArgb: floatingBottomNavigationStateStore.unselectedColorArgb,
                    backgroundColorArgb: floatingBottomNavigationStateStore.backgroundColorArgb,
                    homeTabLabel: floatingBottomNavigationStateStore.homeTabLabel,
                    calendarTabLabel: floatingBottomNavigationStateStore.calendarTabLabel,
                    myPageTabLabel: floatingBottomNavigationStateStore.myPageTabLabel,
                    onTabSelected: handleFloatingTabSelected
                )
                .frame(height: 78)
                .padding(.horizontal, 12)
                .padding(.bottom, 8)
                .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .animation(.easeInOut(duration: 0.2), value: floatingBottomNavigationStateStore.isVisible)
    }

    private func handleFloatingTabSelected(_ selectedTabIndex: Int) {
        if selectedTabIndex < 0 || selectedTabIndex > 2 {
            return
        }
        FloatingBottomNavigationBridgeKt.emitFloatingBottomNavigationSelection(tabIndex: Int32(selectedTabIndex))
    }
}
