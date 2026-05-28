#!/bin/sh
set -eu

SCRIPT_DIRECTORY_PATH="$(cd "$(dirname "$0")" && pwd)"
REPOSITORY_ROOT_PATH="$(cd "${SCRIPT_DIRECTORY_PATH}/../.." && pwd)"
LOCAL_PROPERTIES_FILE_PATH="${REPOSITORY_ROOT_PATH}/local.properties"
XCODE_CONFIGURATION_DIRECTORY_PATH="${REPOSITORY_ROOT_PATH}/iosApp/Configuration"
DEBUG_XCCONFIG_FILE_PATH="${XCODE_CONFIGURATION_DIRECTORY_PATH}/Debug.xcconfig"
RELEASE_XCCONFIG_FILE_PATH="${XCODE_CONFIGURATION_DIRECTORY_PATH}/Release.xcconfig"

require_environment_value() {
  ENVIRONMENT_KEY="$1"
  ENVIRONMENT_VALUE="$(printenv "${ENVIRONMENT_KEY}" || true)"

  if [ -z "${ENVIRONMENT_VALUE}" ]; then
    echo "Missing required Xcode Cloud environment variable: ${ENVIRONMENT_KEY}" >&2
    exit 1
  fi

  printf "%s" "${ENVIRONMENT_VALUE}"
}

ensure_java_runtime() {
  if /usr/libexec/java_home -v 17 >/dev/null 2>&1; then
    JAVA_HOME_VALUE="$(/usr/libexec/java_home -v 17)"
    echo "Found Java runtime for Xcode Cloud at ${JAVA_HOME_VALUE}"
    return
  fi

  for JAVA_HOME_VALUE in \
    "/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home" \
    "/usr/local/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
  do
    if [ -x "${JAVA_HOME_VALUE}/bin/java" ]; then
      echo "Found Java runtime for Xcode Cloud at ${JAVA_HOME_VALUE}"
      return
    fi
  done

  if ! command -v brew >/dev/null 2>&1; then
    echo "Java 17 runtime is missing and Homebrew is not available." >&2
    exit 1
  fi

  echo "Installing Java 17 runtime for Xcode Cloud"
  brew install openjdk@17

  if /usr/libexec/java_home -v 17 >/dev/null 2>&1; then
    JAVA_HOME_VALUE="$(/usr/libexec/java_home -v 17)"
    echo "Installed Java runtime for Xcode Cloud at ${JAVA_HOME_VALUE}"
    return
  fi

  for JAVA_HOME_VALUE in \
    "/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home" \
    "/usr/local/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
  do
    if [ -x "${JAVA_HOME_VALUE}/bin/java" ]; then
      echo "Installed Java runtime for Xcode Cloud at ${JAVA_HOME_VALUE}"
      return
    fi
  done

  echo "Java 17 runtime installation finished, but no usable Java runtime was found." >&2
  exit 1
}

ensure_java_runtime

DEBUG_BASE_URL_VALUE="$(require_environment_value "OGURI_DEBUG_BASE_URL")"
RELEASE_BASE_URL_VALUE="$(require_environment_value "OGURI_RELEASE_BASE_URL")"
KAKAO_KEY_VALUE="$(require_environment_value "OGURI_KAKAO_KEY")"
GOOGLE_WEB_CLIENT_ID_VALUE="$(require_environment_value "OGURI_GOOGLE_WEB_CLIENT_ID")"
AMPLITUDE_API_KEY_VALUE="$(require_environment_value "OGURI_AMPLITUDE_API_KEY")"
IOS_TEAM_ID_VALUE="$(require_environment_value "OGURI_IOS_TEAM_ID")"
IOS_PRODUCT_NAME_VALUE="$(require_environment_value "OGURI_IOS_PRODUCT_NAME")"
IOS_RELEASE_BUNDLE_IDENTIFIER_VALUE="$(require_environment_value "OGURI_IOS_RELEASE_BUNDLE_IDENTIFIER")"

cat > "${LOCAL_PROPERTIES_FILE_PATH}" <<EOF
debug.base.url=${DEBUG_BASE_URL_VALUE}
release.base.url=${RELEASE_BASE_URL_VALUE}
kakao.key=${KAKAO_KEY_VALUE}
google.web.client.id=${GOOGLE_WEB_CLIENT_ID_VALUE}
amplitude.api.key=${AMPLITUDE_API_KEY_VALUE}
EOF

mkdir -p "${XCODE_CONFIGURATION_DIRECTORY_PATH}"

cat > "${DEBUG_XCCONFIG_FILE_PATH}" <<EOF
#include "../../version.properties"

KEY_KAKAO=${KAKAO_KEY_VALUE}

PRODUCT_NAME=${IOS_PRODUCT_NAME_VALUE}
PRODUCT_BUNDLE_IDENTIFIER=${IOS_RELEASE_BUNDLE_IDENTIFIER_VALUE}.debug
DEVELOPMENT_TEAM=${IOS_TEAM_ID_VALUE}
EOF

cat > "${RELEASE_XCCONFIG_FILE_PATH}" <<EOF
#include "../../version.properties"

KEY_KAKAO=${KAKAO_KEY_VALUE}

PRODUCT_NAME=${IOS_PRODUCT_NAME_VALUE}
PRODUCT_BUNDLE_IDENTIFIER=${IOS_RELEASE_BUNDLE_IDENTIFIER_VALUE}
DEVELOPMENT_TEAM=${IOS_TEAM_ID_VALUE}
EOF

echo "Created local.properties for Xcode Cloud at ${LOCAL_PROPERTIES_FILE_PATH}"
echo "Created Debug.xcconfig for Xcode Cloud at ${DEBUG_XCCONFIG_FILE_PATH}"
echo "Created Release.xcconfig for Xcode Cloud at ${RELEASE_XCCONFIG_FILE_PATH}"
echo "Loaded OGURI_DEBUG_BASE_URL length: ${#DEBUG_BASE_URL_VALUE}"
echo "Loaded OGURI_RELEASE_BASE_URL length: ${#RELEASE_BASE_URL_VALUE}"
echo "Loaded OGURI_KAKAO_KEY length: ${#KAKAO_KEY_VALUE}"
echo "Loaded OGURI_GOOGLE_WEB_CLIENT_ID length: ${#GOOGLE_WEB_CLIENT_ID_VALUE}"
echo "Loaded OGURI_AMPLITUDE_API_KEY length: ${#AMPLITUDE_API_KEY_VALUE}"
echo "Loaded OGURI_IOS_TEAM_ID length: ${#IOS_TEAM_ID_VALUE}"
echo "Loaded OGURI_IOS_PRODUCT_NAME length: ${#IOS_PRODUCT_NAME_VALUE}"
echo "Loaded OGURI_IOS_RELEASE_BUNDLE_IDENTIFIER length: ${#IOS_RELEASE_BUNDLE_IDENTIFIER_VALUE}"
