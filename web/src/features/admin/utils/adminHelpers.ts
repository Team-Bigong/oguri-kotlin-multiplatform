import { getImageBlobFromFirebaseStorageByUrl } from "../../../lib/firebase"
import { Destination, PublicHoliday } from "../../../types/admin"
import {
  CropArea,
  CropQueueItem,
  CropTargetType,
  DestinationApiResponse,
  DestinationFormState,
  HolidayFormState,
  MemberFormState,
  CountryFormState,
  PublicHolidayApiResponse
} from "../types/adminLocalTypes"

export const PLACE_IMAGE_PRIMARY_ASPECT_RATIO = 100 / 87
export const PLACE_IMAGE_SECONDARY_ASPECT_RATIO = 100 / 67
export const EXPERIENCE_IMAGE_ASPECT_RATIO = 100 / 60
export const PLACE_IMAGE_OUTPUT_WIDTH_PX = 1280
export const EXPERIENCE_IMAGE_OUTPUT_WIDTH_PX = 1200
export const CROP_MINIMUM_SIZE_PX = 120
export const IMAGE_DOWNLOAD_TIMEOUT_MILLISECONDS = 10_000

export const storagePathSegmentPattern = /^[a-z0-9]+(?:-[a-z0-9]+)*$/

export const createInitialDestinationFormState = (): DestinationFormState => ({
  selectedId: null,
  countryId: "",
  countryName: "",
  storageCountrySlug: "",
  storageCitySlug: "",
  name: "",
  summary: "",
  description: "",
  recommendStartMonth1: "",
  recommendEndMonth1: "",
  recommendStartMonth2: "",
  recommendEndMonth2: "",
  flightTime: "",
  flightUrl: "",
  weatherTemp1: "",
  weatherPrecipitationMm1: "",
  weatherTemp2: "",
  weatherPrecipitationMm2: "",
  images: [],
  experiences: [],
  existingImageUrls: [],
  existingExperienceThumbnailUrls: [],
  newlyUploadedImageUrls: [],
  newlyUploadedExperienceThumbnailUrls: []
})

export const createInitialMemberFormState = (): MemberFormState => ({
  selectedId: null,
  id: "",
  nickname: "",
  preferredDayOff: "3",
  remainingDayOff: "3",
  onboardingCompleted: false
})

export const createInitialHolidayFormState = (): HolidayFormState => ({
  selectedId: null,
  holidayDate: "",
  name: "",
  isActualHoliday: true
})

export const createInitialCountryFormState = (): CountryFormState => ({
  selectedId: null,
  name: "",
  currencyCode: "",
  bigMacIndex: ""
})

export const normalizePublicHoliday = (holiday: PublicHolidayApiResponse): PublicHoliday => {
  const resolvedActualHoliday = holiday.isActualHoliday ?? holiday.actualHoliday ?? true
  return {
    id: holiday.id,
    holidayDate: holiday.holidayDate,
    name: holiday.name,
    isActualHoliday: resolvedActualHoliday
  }
}

export const normalizeDestination = (destination: DestinationApiResponse): Destination => {
  return {
    ...destination,
    images: destination.images.map((image) => ({
      id: image.id,
      imageUrl: image.imageUrl,
      sortOrder: image.sortOrder,
      isThumbnail: image.isThumbnail ?? image.thumbnail ?? false
    })),
    experiences: destination.experiences
  }
}

export const parseFlightTimeMinutes = (value: string): string => {
  return value.replace(/[^0-9]/g, "")
}

export const parseStorageSlugsFromImageUrl = (imageUrl: string): { countrySlug: string; citySlug: string } => {
  try {
    const parsedUrl = new URL(imageUrl)
    const objectPathEncoded = parsedUrl.pathname.split("/o/")[1] ?? ""
    const objectPath = decodeURIComponent(objectPathEncoded)
    const pathSegments = objectPath.split("/")
    if (pathSegments.length >= 4 && pathSegments[0] === "places") {
      return {
        countrySlug: pathSegments[1],
        citySlug: pathSegments[2]
      }
    }
  } catch (error) {
    return { countrySlug: "", citySlug: "" }
  }

  return { countrySlug: "", citySlug: "" }
}

export const parseImageSequenceNumberFromImageUrl = (imageUrl: string): number | null => {
  try {
    const parsedUrl = new URL(imageUrl)
    const objectPathEncoded = parsedUrl.pathname.split("/o/")[1] ?? ""
    const objectPath = decodeURIComponent(objectPathEncoded)
    const fileName = objectPath.split("/").pop() ?? ""
    const sequenceText = fileName.replace(/\.jpg$/i, "")
    const parsedSequence = Number(sequenceText)
    if (Number.isInteger(parsedSequence) && parsedSequence > 0) {
      return parsedSequence
    }
  } catch (error) {
    return null
  }
  return null
}

export const parseExperienceSequenceNumberFromThumbnailUrl = (thumbnailUrl: string): number | null => {
  try {
    const parsedUrl = new URL(thumbnailUrl)
    const objectPathEncoded = parsedUrl.pathname.split("/o/")[1] ?? ""
    const objectPath = decodeURIComponent(objectPathEncoded)
    const matchedSequence = objectPath.match(/\/experiences\/([0-9]+)\.jpg$/i)
    if (matchedSequence == null) {
      return null
    }
    const parsedSequence = Number(matchedSequence[1])
    return Number.isInteger(parsedSequence) && parsedSequence > 0 ? parsedSequence : null
  } catch (error) {
    return null
  }
}

export const parseFirebaseObjectPathFromImageUrl = (imageUrl: string): string | null => {
  try {
    const parsedUrl = new URL(imageUrl)
    const objectPathEncoded = parsedUrl.pathname.split("/o/")[1] ?? ""
    const objectPath = decodeURIComponent(objectPathEncoded)
    return objectPath.length > 0 ? objectPath : null
  } catch (error) {
    return null
  }
}

export const validateStoragePathSegment = (value: string, label: string): string => {
  const trimmedValue = value.trim()
  if (trimmedValue.length === 0) {
    throw new Error(`${label}를 입력해주세요.`)
  }
  if (!storagePathSegmentPattern.test(trimmedValue)) {
    throw new Error(`${label}는 영문 소문자, 숫자, 하이픈(-)만 사용할 수 있습니다.`)
  }
  return trimmedValue
}

export const loadImageNaturalSize = async (file: File): Promise<{ naturalWidth: number; naturalHeight: number }> => {
  const previewUrl = URL.createObjectURL(file)
  try {
    const imageElement = new Image()
    imageElement.src = previewUrl
    await imageElement.decode()
    return {
      naturalWidth: imageElement.naturalWidth,
      naturalHeight: imageElement.naturalHeight
    }
  } finally {
    URL.revokeObjectURL(previewUrl)
  }
}

export const createCropQueueItems = async (files: File[]): Promise<CropQueueItem[]> => {
  const queueItems = await Promise.all(
    files.map(async (file) => {
      const { naturalWidth, naturalHeight } = await loadImageNaturalSize(file)
      return {
        file,
        previewUrl: URL.createObjectURL(file),
        naturalWidth,
        naturalHeight
      } as CropQueueItem
    })
  )
  return queueItems
}

export const withTimeout = async <T,>(promise: Promise<T>, timeoutMilliseconds: number): Promise<T> => {
  return new Promise((resolve, reject) => {
    const timeoutIdentifier = window.setTimeout(() => {
      reject(new Error("timeout"))
    }, timeoutMilliseconds)
    promise
      .then((value) => {
        window.clearTimeout(timeoutIdentifier)
        resolve(value)
      })
      .catch((error) => {
        window.clearTimeout(timeoutIdentifier)
        reject(error)
      })
  })
}

export const createFileFromImageUrl = async (imageUrl: string): Promise<File> => {
  const sanitizedImageUrl = imageUrl.trim()
  try {
    const binary = await withTimeout(
      getImageBlobFromFirebaseStorageByUrl(sanitizedImageUrl),
      IMAGE_DOWNLOAD_TIMEOUT_MILLISECONDS
    )
    const fileType = binary.type.length > 0 ? binary.type : "image/jpeg"
    const fileName = `crop-source-${Date.now()}.jpg`
    return new File([binary], fileName, { type: fileType })
  } catch (firebaseSdkError) {
    throw new Error("Firebase에서 이미지를 가져오지 못했습니다. 잠시 후 다시 시도해주세요.")
  }
}

export const createInitialCropArea = (item: CropQueueItem): CropArea => {
  return {
    x: 0,
    y: 0,
    width: item.naturalWidth,
    height: item.naturalHeight
  }
}

export const createGuideRect = (
  containerWidth: number,
  containerHeight: number,
  aspectRatio: number
): CropArea => {
  let width = containerWidth
  let height = width / aspectRatio
  if (height > containerHeight) {
    height = containerHeight
    width = height * aspectRatio
  }
  return {
    x: (containerWidth - width) / 2,
    y: (containerHeight - height) / 2,
    width,
    height
  }
}

export const clampGuideRectWithinCropArea = (
  guideRect: CropArea,
  cropAreaWidth: number,
  cropAreaHeight: number
): CropArea => {
  const clampedWidth = Math.min(guideRect.width, cropAreaWidth)
  const clampedHeight = Math.min(guideRect.height, cropAreaHeight)
  return {
    x: Math.max(0, Math.min(cropAreaWidth - clampedWidth, guideRect.x)),
    y: Math.max(0, Math.min(cropAreaHeight - clampedHeight, guideRect.y)),
    width: clampedWidth,
    height: clampedHeight
  }
}

export const getCropGuideAspectRatio = (targetType: CropTargetType): number => {
  return targetType === "experience" ? EXPERIENCE_IMAGE_ASPECT_RATIO : PLACE_IMAGE_PRIMARY_ASPECT_RATIO
}
