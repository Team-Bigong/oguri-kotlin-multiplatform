const readRequiredEnvironment = (key: string): string => {
  const environmentVariables = import.meta.env as Record<string, string | undefined>
  const value = environmentVariables[key]
  if (!value || value.trim().length === 0) {
    throw new Error(`[web env] ${key} 값이 필요합니다.`)
  }
  return value
}

export const webEnvironment = {
  apiBaseUrl: readRequiredEnvironment("VITE_API_BASE_URL"),
  firebaseApiKey: readRequiredEnvironment("VITE_FIREBASE_API_KEY"),
  firebaseAuthDomain: readRequiredEnvironment("VITE_FIREBASE_AUTH_DOMAIN"),
  firebaseProjectId: readRequiredEnvironment("VITE_FIREBASE_PROJECT_ID"),
  firebaseStorageBucket: readRequiredEnvironment("VITE_FIREBASE_STORAGE_BUCKET"),
  firebaseMessagingSenderId: readRequiredEnvironment("VITE_FIREBASE_MESSAGING_SENDER_ID"),
  firebaseAppId: readRequiredEnvironment("VITE_FIREBASE_APP_ID"),
  firebaseMeasurementId: import.meta.env.VITE_FIREBASE_MEASUREMENT_ID ?? "",
  playStoreUrl: import.meta.env.VITE_PLAY_STORE_URL ?? "",
  appStoreUrl: import.meta.env.VITE_APP_STORE_URL ?? ""
}
