import { initializeApp, getApps } from "firebase/app"
import { deleteObject, getDownloadURL, getStorage, ref, uploadBytes } from "firebase/storage"
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

const extractObjectPathFromFirebaseStorageUrl = (imageUrl: string): string => {
  const parsedUrl = new URL(imageUrl)
  const encodedObjectPath = parsedUrl.pathname.split("/o/")[1] ?? ""
  const decodedObjectPath = decodeURIComponent(encodedObjectPath)

  if (decodedObjectPath.length === 0) {
    throw new Error("Firebase 이미지 경로를 확인할 수 없습니다.")
  }

  return decodedObjectPath
}

export const deleteImageFromFirebaseStorageByUrl = async (imageUrl: string): Promise<void> => {
  const objectPath = extractObjectPathFromFirebaseStorageUrl(imageUrl)
  const storageReference = ref(firebaseStorage, objectPath)
  await deleteObject(storageReference)
}
