#!/usr/bin/env bash
set -euo pipefail

repository_root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"

# Respect an explicit Xcode selection; handle the common Command Line Tools default.
if [[ -z "${DEVELOPER_DIR:-}" ]]; then
    selected_developer="$(xcode-select -p)"
    if [[ "$selected_developer" == */CommandLineTools && -d /Applications/Xcode.app/Contents/Developer ]]; then
        export DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer
    fi
fi

# All dependencies are public. Avoid SwiftPM's keychain authorization lookup,
# which can stall binary-artifact downloads in noninteractive Xcode builds.
xcrun xcodebuild -quiet \
    -project "$repository_root/iosApp/iosApp.xcodeproj" \
    -scheme iosApp-Debug \
    -configuration Debug \
    -sdk iphonesimulator \
    -destination 'generic/platform=iOS Simulator' \
    -onlyUsePackageVersionsFromResolvedFile \
    -packageAuthorizationProvider netrc \
    ARCHS=arm64 \
    ONLY_ACTIVE_ARCH=YES \
    CODE_SIGNING_ALLOWED=NO \
    build

printf 'iOS Simulator arm64 build succeeded.\n'
