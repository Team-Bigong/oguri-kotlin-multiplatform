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
