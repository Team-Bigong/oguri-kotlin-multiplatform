import ComposeApp
import Foundation
import KakaoSDKAuth
import KakaoSDKUser

final class KakaoLoginBridge: NSObject, KakaoLoginHost {
    func login(request: KakaoLoginRequest) {
        let completion: (OAuthToken?, Error?) -> Void = { token, error in
            request.complete(accessToken: token?.accessToken, errorMessage: error?.localizedDescription)
        }
        if UserApi.isKakaoTalkLoginAvailable() {
            UserApi.shared.loginWithKakaoTalk(completion: completion)
        } else {
            UserApi.shared.loginWithKakaoAccount(completion: completion)
        }
    }
}
