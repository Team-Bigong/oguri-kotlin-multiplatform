import { initializeApp, getApps } from "firebase/app"
import { getDownloadURL, getStorage, ref, uploadBytes } from "firebase/storage"
import { webEnvironment } from "../config/env"

const firebaseApplication = getApps()[0] ?? initializeApp({
  apiKey: webEnvironment.firebaseApiKey,
  authDomain: webEnvironment.firebaseAuthDomain,
  projectId: webEnvironment.firebaseProjectId,
  storageBucket: webEnvironment.firebaseStorageBucket,
  messagingSenderId: webEnvironment.firebaseMessagingSenderId,
  appId: webEnvironment.firebaseAppId,
  measurementId: webEnvironment.firebaseMeasurementId || undefined
})

const firebaseStorage = getStorage(firebaseApplication)

export const uploadImageToFirebaseStorage = async (binary: Blob, objectPath: string): Promise<string> => {
  const storageReference = ref(firebaseStorage, objectPath)
  await uploadBytes(storageReference, binary, { contentType: "image/jpeg" })
  return getDownloadURL(storageReference)
}
