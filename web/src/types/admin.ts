export type Country = {
  id: number
  name: string
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
  flightTime: string | null
  images: DestinationImage[]
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
  flightTime: string | null
  images: DestinationImageRequest[]
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
