import { DestinationExperienceRequest, DestinationImageRequest } from "../../../types/admin"

export type AdminTab = "destinations" | "members" | "holidays" | "countries"

export type DestinationFormState = {
  selectedId: number | null
  countryId: string
  countryName: string
  storageCountrySlug: string
  storageCitySlug: string
  name: string
  summary: string
  description: string
  recommendStartMonth1: string
  recommendEndMonth1: string
  recommendStartMonth2: string
  recommendEndMonth2: string
  flightTime: string
  flightUrl: string
  weatherTemp1: string
  weatherPrecipitationMm1: string
  weatherTemp2: string
  weatherPrecipitationMm2: string
  images: DestinationImageRequest[]
  experiences: DestinationExperienceRequest[]
  existingImageUrls: string[]
  existingExperienceThumbnailUrls: string[]
  newlyUploadedImageUrls: string[]
  newlyUploadedExperienceThumbnailUrls: string[]
}

export type MemberFormState = {
  selectedId: string | null
  id: string
  nickname: string
  preferredDayOff: string
  remainingDayOff: string
  onboardingCompleted: boolean
}

export type HolidayFormState = {
  selectedId: number | null
  holidayDate: string
  name: string
  isActualHoliday: boolean
}

export type CountryFormState = {
  selectedId: number | null
  name: string
  currencyCode: string
  bigMacIndex: string
}

export type DestinationImageApiResponse = {
  id: number
  imageUrl: string
  sortOrder: number
  isThumbnail?: boolean
  thumbnail?: boolean
}

export type DestinationExperienceApiResponse = {
  id: number
  title: string
  description: string
  thumbnailUrl: string
  link: string
  sortOrder: number
}

export type DestinationApiResponse = {
  id: number
  countryId: number | null
  countryName: string
  name: string
  summary: string | null
  description: string | null
  recommendStartMonth1: number | null
  recommendEndMonth1: number | null
  recommendStartMonth2: number | null
  recommendEndMonth2: number | null
  flightTimeMinutes: number | null
  flightUrl: string | null
  weatherTemp1: number | null
  weatherPrecipitationMm1: number | null
  weatherTemp2: number | null
  weatherPrecipitationMm2: number | null
  images: DestinationImageApiResponse[]
  experiences: DestinationExperienceApiResponse[]
}

export type PublicHolidayApiResponse = {
  id: number
  holidayDate: string
  name: string
  isActualHoliday?: boolean
  actualHoliday?: boolean
}

export type StorageCountryOption = {
  slug: string
  citySlugs: string[]
}

export type CropTargetType = "destination" | "experience"

export type CropQueueItem = {
  file: File
  previewUrl: string
  naturalWidth: number
  naturalHeight: number
}

export type CropArea = {
  x: number
  y: number
  width: number
  height: number
}

export type CropSessionState = {
  targetType: CropTargetType
  applyMode: "append" | "replace"
  queueItems: CropQueueItem[]
  currentIndex: number
  destinationImageIndex: number | null
  experienceIndex: number | null
  cropArea: CropArea
  isProcessing: boolean
}

export type UploadStage = "preparing" | "uploading"

export type CropDragState = {
  mode: "move" | "resize" | null
  pointerStartX: number
  pointerStartY: number
  cropAreaAtStart: CropArea | null
}

export type CropImageRenderMetrics = {
  displayScale: number
  displayWidth: number
  displayHeight: number
}

export type GuideRect = CropArea
