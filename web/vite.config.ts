import path from "node:path"
import { defineConfig } from "vite"
import react from "@vitejs/plugin-react"

export default defineConfig({
  envDir: "..",
  plugins: [react()],
  resolve: {
    alias: {
      "react-native$": "react-native-web",
      "react-native": "react-native-web",
      "@": path.resolve(__dirname, "src")
    }
  }
})
