import path from "node:path"
import { defineConfig, loadEnv } from "vite"
import react from "@vitejs/plugin-react"

export default defineConfig(({ mode }) => {
  const settings = { ...loadEnv(mode, path.resolve(__dirname, ".."), ""), ...process.env }
  const androidFingerprint = settings.TRAVEL_ANDROID_SIGNING_SHA256?.trim().toUpperCase()
  const appleAppId = settings.TRAVEL_IOS_APP_ID?.trim()
  return {
    envDir: "..",
    plugins: [
      react(),
      {
        name: "travel-invite-domain-associations",
        generateBundle() {
          if (androidFingerprint && /^([A-F0-9]{2}:){31}[A-F0-9]{2}$/.test(androidFingerprint)) {
            this.emitFile({
              type: "asset",
              fileName: ".well-known/assetlinks.json",
              source: JSON.stringify([{
                relation: ["delegate_permission/common.handle_all_urls"],
                target: {
                  namespace: "android_app",
                  package_name: "com.bigong.oguri",
                  sha256_cert_fingerprints: [androidFingerprint]
                }
              }])
            })
          }
          if (appleAppId && /^[A-Z0-9]{10}\.[A-Za-z0-9.-]+$/.test(appleAppId)) {
            this.emitFile({
              type: "asset",
              fileName: ".well-known/apple-app-site-association",
              source: JSON.stringify({
                applinks: { details: [{ appIDs: [appleAppId], components: [{ "/": "/trip-invite" }, { "/": "/trip-invite/" }] }] }
              })
            })
          }
        }
      }
    ],
    build: {
      rollupOptions: {
        input: {
          main: path.resolve(__dirname, "index.html"),
          tripInvite: path.resolve(__dirname, "trip-invite/index.html")
        }
      }
    },
    resolve: {
      alias: {
        "react-native$": "react-native-web",
        "react-native": "react-native-web",
        "@": path.resolve(__dirname, "src")
      }
    }
  }
})
