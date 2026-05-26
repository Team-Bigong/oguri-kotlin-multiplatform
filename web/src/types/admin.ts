export type Country = {
  id: number
  name: string
  currencyCode: string | null
  bigMacIndex: number | null
}

export type DestinationImage = {
  id: number
  imageUrl: string
  isThumbnail: boolean
  sortOrder: number
}

export type Destination = {
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
  images: DestinationImage[]
  experiences: DestinationExperience[]
}

export type DestinationExperience = {
  id: number
  title: string
  description: string
  thumbnailUrl: string
  link: string
  sortOrder: number
}

export type DestinationImageRequest = {
  imageUrl: string
  isThumbnail: boolean
  sortOrder: number
}

export type DestinationUpsertRequest = {
  countryId: number
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
  images: DestinationImageRequest[]
  experiences: DestinationExperienceRequest[]
}

export type CountryUpsertRequest = {
  name: string
  currencyCode: string | null
  bigMacIndex: number | null
}

export type DestinationExperienceRequest = {
  title: string
  description: string
  thumbnailUrl: string
  link: string
  sortOrder: number
}

export type Member = {
  id: string
  nickname: string | null
  preferredDayOff: number
  remainingDayOff: number
  onboardingCompleted: boolean
  updatedAt: string
}

export type MemberCreateRequest = {
  id: string
  nickname: string | null
  preferredDayOff: number
  remainingDayOff: number
  onboardingCompleted: boolean
}

export type MemberUpdateRequest = {
  nickname: string | null
  preferredDayOff: number
  remainingDayOff: number
  onboardingCompleted: boolean
}

export type PublicHoliday = {
  id: number
  holidayDate: string
  name: string
  isActualHoliday: boolean
}

export type PublicHolidayUpsertRequest = {
  holidayDate: string
  name: string
  isActualHoliday: boolean
}

export type AdminLoginResponse = {
  accessToken: string
  expiresInSeconds: number
}
