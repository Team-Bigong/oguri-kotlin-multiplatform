import React, { useCallback, useEffect, useMemo, useState } from "react"
import { adminApiClient, clearAdminAccessToken, getAdminAccessToken, setAdminAccessToken } from "../../../lib/apiClient"
import { deleteImageFromFirebaseStorageByUrl } from "../../../lib/firebase"
import {
  AdminLoginResponse,
  Country,
  CountryUpsertRequest,
  Destination,
  DestinationUpsertRequest,
  Member,
  MemberCreateRequest,
  MemberUpdateRequest,
  PublicHoliday,
  PublicHolidayUpsertRequest
} from "../../../types/admin"
import {
  AdminTab,
  DestinationApiResponse,
  DestinationFormState,
  HolidayFormState,
  MemberFormState,
  CountryFormState,
  PublicHolidayApiResponse,
  StorageCountryOption
} from "../types/adminLocalTypes"
import {
  createInitialDestinationFormState,
  createInitialCountryFormState,
  createInitialHolidayFormState,
  createInitialMemberFormState,
  normalizeDestination,
  normalizePublicHoliday,
  parseFirebaseObjectPathFromImageUrl,
  parseFlightTimeMinutes,
  parseStorageSlugsFromImageUrl
} from "../utils/adminHelpers"

type UseAdminDataManagementResult = {
  isAuthenticated: boolean
  loginUsername: string
  loginPassword: string
  loginLoading: boolean
  activeTab: AdminTab
  loading: boolean
  noticeMessage: string
  errorMessage: string
  countries: Country[]
  destinations: Destination[]
  members: Member[]
  publicHolidays: PublicHoliday[]
  destinationListSearchKeyword: string
  memberListSearchKeyword: string
  holidayListSearchKeyword: string
  countryListSearchKeyword: string
  destinationFormState: DestinationFormState
  memberFormState: MemberFormState
  holidayFormState: HolidayFormState
  countryFormState: CountryFormState
  storageCountryOptions: StorageCountryOption[]
  selectedStorageCountryOption: StorageCountryOption | undefined
  filteredDestinations: Destination[]
  filteredMembers: Member[]
  filteredPublicHolidays: PublicHoliday[]
  filteredCountries: Country[]
  setLoginUsername: React.Dispatch<React.SetStateAction<string>>
  setLoginPassword: React.Dispatch<React.SetStateAction<string>>
  setActiveTab: React.Dispatch<React.SetStateAction<AdminTab>>
  setDestinationListSearchKeyword: React.Dispatch<React.SetStateAction<string>>
  setMemberListSearchKeyword: React.Dispatch<React.SetStateAction<string>>
  setHolidayListSearchKeyword: React.Dispatch<React.SetStateAction<string>>
  setCountryListSearchKeyword: React.Dispatch<React.SetStateAction<string>>
  setDestinationFormState: React.Dispatch<React.SetStateAction<DestinationFormState>>
  setMemberFormState: React.Dispatch<React.SetStateAction<MemberFormState>>
  setHolidayFormState: React.Dispatch<React.SetStateAction<HolidayFormState>>
  setCountryFormState: React.Dispatch<React.SetStateAction<CountryFormState>>
  setNoticeMessage: React.Dispatch<React.SetStateAction<string>>
  setErrorMessage: React.Dispatch<React.SetStateAction<string>>
  loadAll: () => Promise<void>
  submitAdminLogin: () => Promise<void>
  submitAdminLogout: () => void
  submitDestination: () => Promise<void>
  deleteDestination: (destinationId: number) => Promise<void>
  submitMember: () => Promise<void>
  deleteMember: (memberId: string) => Promise<void>
  submitHoliday: () => Promise<void>
  deleteHoliday: (holidayId: number) => Promise<void>
  submitCountry: () => Promise<void>
  deleteCountry: (countryId: number) => Promise<void>
  resetDestinationFormWithCleanup: () => void
  loadDestinationToForm: (destination: Destination) => void
  loadCountryToForm: (country: Country) => void
}

export const useAdminDataManagement = (): UseAdminDataManagementResult => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(getAdminAccessToken().length > 0)
  const [loginUsername, setLoginUsername] = useState<string>("")
  const [loginPassword, setLoginPassword] = useState<string>("")
  const [loginLoading, setLoginLoading] = useState<boolean>(false)

  const [activeTab, setActiveTab] = useState<AdminTab>("destinations")
  const [loading, setLoading] = useState<boolean>(false)
  const [noticeMessage, setNoticeMessage] = useState<string>("")
  const [errorMessage, setErrorMessage] = useState<string>("")

  const [countries, setCountries] = useState<Country[]>([])
  const [destinations, setDestinations] = useState<Destination[]>([])
  const [members, setMembers] = useState<Member[]>([])
  const [publicHolidays, setPublicHolidays] = useState<PublicHoliday[]>([])

  const [destinationListSearchKeyword, setDestinationListSearchKeyword] = useState<string>("")
  const [memberListSearchKeyword, setMemberListSearchKeyword] = useState<string>("")
  const [holidayListSearchKeyword, setHolidayListSearchKeyword] = useState<string>("")
  const [countryListSearchKeyword, setCountryListSearchKeyword] = useState<string>("")

  const [destinationFormState, setDestinationFormState] = useState<DestinationFormState>(createInitialDestinationFormState)
  const [memberFormState, setMemberFormState] = useState<MemberFormState>(createInitialMemberFormState)
  const [holidayFormState, setHolidayFormState] = useState<HolidayFormState>(createInitialHolidayFormState)
  const [countryFormState, setCountryFormState] = useState<CountryFormState>(createInitialCountryFormState)

  const storageCountryOptions = useMemo<StorageCountryOption[]>(() => {
    const countryCityMap = new Map<string, Set<string>>()
    destinations.forEach((destination) => {
      const imageUrls = [
        ...destination.images.map((image) => image.imageUrl),
        ...destination.experiences.map((experience) => experience.thumbnailUrl)
      ]
      imageUrls.forEach((imageUrl) => {
        const { countrySlug, citySlug } = parseStorageSlugsFromImageUrl(imageUrl)
        if (countrySlug.length === 0 || citySlug.length === 0) {
          return
        }
        const existingCitySlugSet = countryCityMap.get(countrySlug) ?? new Set<string>()
        existingCitySlugSet.add(citySlug)
        countryCityMap.set(countrySlug, existingCitySlugSet)
      })
    })
    return Array.from(countryCityMap.entries())
      .sort(([leftSlug], [rightSlug]) => leftSlug.localeCompare(rightSlug))
      .map(([countrySlug, citySlugSet]) => ({
        slug: countrySlug,
        citySlugs: Array.from(citySlugSet).sort((leftSlug, rightSlug) => leftSlug.localeCompare(rightSlug))
      }))
  }, [destinations])

  const selectedStorageCountryOption = useMemo(() => {
    const trimmedStorageCountrySlug = destinationFormState.storageCountrySlug.trim()
    return storageCountryOptions.find((countryOption) => countryOption.slug === trimmedStorageCountrySlug)
  }, [destinationFormState.storageCountrySlug, storageCountryOptions])

  const filteredDestinations = useMemo<Destination[]>(() => {
    const normalizedKeyword = destinationListSearchKeyword.trim().toLowerCase()
    if (normalizedKeyword.length === 0) {
      return destinations
    }
    return destinations.filter((destination) => {
      const normalizedTitle = `${destination.countryName} ${destination.name}`.toLowerCase()
      const normalizedSummary = (destination.summary ?? "").toLowerCase()
      return normalizedTitle.includes(normalizedKeyword) || normalizedSummary.includes(normalizedKeyword)
    })
  }, [destinationListSearchKeyword, destinations])

  const filteredMembers = useMemo<Member[]>(() => {
    const normalizedKeyword = memberListSearchKeyword.trim().toLowerCase()
    if (normalizedKeyword.length === 0) {
      return members
    }
    return members.filter((member) => {
      const normalizedMemberId = member.id.toLowerCase()
      const normalizedNickname = (member.nickname ?? "").toLowerCase()
      return normalizedMemberId.includes(normalizedKeyword) || normalizedNickname.includes(normalizedKeyword)
    })
  }, [memberListSearchKeyword, members])

  const filteredPublicHolidays = useMemo<PublicHoliday[]>(() => {
    const normalizedKeyword = holidayListSearchKeyword.trim().toLowerCase()
    if (normalizedKeyword.length === 0) {
      return publicHolidays
    }
    return publicHolidays.filter((holiday) => {
      const normalizedName = holiday.name.toLowerCase()
      const normalizedDate = holiday.holidayDate.toLowerCase()
      return normalizedName.includes(normalizedKeyword) || normalizedDate.includes(normalizedKeyword)
    })
  }, [holidayListSearchKeyword, publicHolidays])

  const filteredCountries = useMemo<Country[]>(() => {
    const normalizedKeyword = countryListSearchKeyword.trim().toLowerCase()
    if (normalizedKeyword.length === 0) {
      return countries
    }
    return countries.filter((country) => {
      const normalizedName = country.name.toLowerCase()
      const normalizedCurrencyCode = (country.currencyCode ?? "").toLowerCase()
      return normalizedName.includes(normalizedKeyword) || normalizedCurrencyCode.includes(normalizedKeyword)
    })
  }, [countries, countryListSearchKeyword])

  const loadAll = useCallback(async () => {
    if (!isAuthenticated) {
      return
    }

    setLoading(true)
    setErrorMessage("")

    try {
      const [countryList, destinationList, memberList, holidayList] = await Promise.all([
        adminApiClient.get<Country[]>("/api/admin/v1/countries"),
        adminApiClient.get<DestinationApiResponse[]>("/api/admin/v1/destinations"),
        adminApiClient.get<Member[]>("/api/admin/v1/members"),
        adminApiClient.get<PublicHolidayApiResponse[]>("/api/admin/v1/public-holidays")
      ])

      setCountries(countryList)
      setDestinations(destinationList.map(normalizeDestination))
      setMembers(memberList)
      setPublicHolidays(holidayList.map(normalizePublicHoliday))
    } catch (error) {
      const resolvedMessage = error instanceof Error ? error.message : "데이터를 불러오지 못했습니다."
      if (resolvedMessage.includes("401")) {
        clearAdminAccessToken()
        setIsAuthenticated(false)
        setErrorMessage("관리자 세션이 만료되었습니다. 다시 로그인해주세요.")
        return
      }
      setErrorMessage(resolvedMessage)
    } finally {
      setLoading(false)
    }
  }, [isAuthenticated])

  useEffect(() => {
    if (isAuthenticated) {
      void loadAll()
    }
  }, [isAuthenticated, loadAll])

  const submitAdminLogin = useCallback(async () => {
    if (loginUsername.trim().length === 0 || loginPassword.trim().length === 0) {
      setErrorMessage("아이디와 비밀번호를 모두 입력해주세요.")
      return
    }

    setLoginLoading(true)
    setErrorMessage("")

    try {
      const response = await adminApiClient.postPublic<AdminLoginResponse>(
        "/api/admin/v1/auth/login",
        {
          username: loginUsername.trim(),
          password: loginPassword
        }
      )
      setAdminAccessToken(response.accessToken)
      setIsAuthenticated(true)
      setLoginPassword("")
      setNoticeMessage("관리자 로그인에 성공했습니다.")
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "로그인에 실패했습니다.")
    } finally {
      setLoginLoading(false)
    }
  }, [loginPassword, loginUsername])

  const submitAdminLogout = useCallback(() => {
    clearAdminAccessToken()
    setIsAuthenticated(false)
    setCountries([])
    setDestinations([])
    setMembers([])
    setPublicHolidays([])
    setNoticeMessage("로그아웃되었습니다.")
    setErrorMessage("")
  }, [])

  const createDestinationPayload = useCallback((state: DestinationFormState): DestinationUpsertRequest => {
    const parseMonth = (value: string): number | null => {
      if (value.trim().length === 0) {
        return null
      }
      const parsedValue = Number(value)
      return Number.isNaN(parsedValue) ? null : parsedValue
    }
    const parseOptionalInteger = (value: string): number | null => {
      const trimmedValue = value.trim()
      if (trimmedValue.length === 0) {
        return null
      }
      const parsedValue = Number(trimmedValue)
      if (!Number.isFinite(parsedValue) || !Number.isInteger(parsedValue)) {
        throw new Error("날씨 온도는 정수로 입력해주세요.")
      }
      return parsedValue
    }
    const parseOptionalDecimal = (value: string): number | null => {
      const trimmedValue = value.trim()
      if (trimmedValue.length === 0) {
        return null
      }
      const parsedValue = Number(trimmedValue)
      if (!Number.isFinite(parsedValue)) {
        throw new Error("강수량은 숫자로 입력해주세요.")
      }
      return parsedValue
    }

    const flightTimeMinutes = parseFlightTimeMinutes(state.flightTime)
    const trimmedCountryName = state.countryName.trim()
    const matchedCountry = countries.find((country) => country.name === trimmedCountryName)
    const resolvedCountryId = state.countryId.length > 0 ? Number(state.countryId) : (matchedCountry?.id ?? null)

    if (resolvedCountryId == null || Number.isNaN(resolvedCountryId)) {
      throw new Error("국가를 정확히 입력해주세요. 목록의 국가명과 일치해야 합니다.")
    }

    return {
      countryId: resolvedCountryId,
      name: state.name,
      summary: state.summary.trim().length > 0 ? state.summary : null,
      description: state.description.trim().length > 0 ? state.description : null,
      recommendStartMonth1: parseMonth(state.recommendStartMonth1),
      recommendEndMonth1: parseMonth(state.recommendEndMonth1),
      recommendStartMonth2: parseMonth(state.recommendStartMonth2),
      recommendEndMonth2: parseMonth(state.recommendEndMonth2),
      flightTimeMinutes: flightTimeMinutes.length > 0 ? Number(flightTimeMinutes) : null,
      flightUrl: state.flightUrl.trim().length > 0 ? state.flightUrl.trim() : null,
      weatherTemp1: parseOptionalInteger(state.weatherTemp1),
      weatherPrecipitationMm1: parseOptionalDecimal(state.weatherPrecipitationMm1),
      weatherTemp2: parseOptionalInteger(state.weatherTemp2),
      weatherPrecipitationMm2: parseOptionalDecimal(state.weatherPrecipitationMm2),
      images: state.images.map((image) => ({
        imageUrl: image.imageUrl,
        sortOrder: image.sortOrder,
        isThumbnail: image.isThumbnail
      })),
      experiences: state.experiences.map((experience, index) => ({
        title: experience.title.trim(),
        description: experience.description.trim(),
        thumbnailUrl: experience.thumbnailUrl.trim(),
        link: experience.link.trim(),
        sortOrder: index + 1
      }))
    }
  }, [countries])

  const deleteImagesInFirebaseStorage = useCallback(async (imageUrls: string[]): Promise<number> => {
    if (imageUrls.length === 0) {
      return 0
    }

    const deleteResults = await Promise.allSettled(
      imageUrls.map((imageUrl) => deleteImageFromFirebaseStorageByUrl(imageUrl))
    )

    return deleteResults.filter((result) => result.status === "rejected").length
  }, [])

  const cleanupPendingUploadedImages = useCallback(async (imageUrls: string[], experienceThumbnailUrls: string[]): Promise<void> => {
    const failedDeleteCount = await deleteImagesInFirebaseStorage([...imageUrls, ...experienceThumbnailUrls])
    if (failedDeleteCount > 0) {
      setNoticeMessage(`임시 업로드 이미지 ${failedDeleteCount}건 정리에 실패했습니다.`)
    }
  }, [deleteImagesInFirebaseStorage])

  const submitDestination = useCallback(async () => {
    setErrorMessage("")
    setNoticeMessage("")

    try {
      const payload = createDestinationPayload(destinationFormState)
      const currentImageObjectPathSet = new Set(
        destinationFormState.images
          .map((image) => parseFirebaseObjectPathFromImageUrl(image.imageUrl))
          .filter((objectPath): objectPath is string => objectPath != null)
      )
      const removedExistingImageUrls = destinationFormState.existingImageUrls.filter((imageUrl) => {
        const objectPath = parseFirebaseObjectPathFromImageUrl(imageUrl)
        if (objectPath == null) {
          return !destinationFormState.images.some((image) => image.imageUrl === imageUrl)
        }
        return !currentImageObjectPathSet.has(objectPath)
      })
      const currentExperienceThumbnailObjectPathSet = new Set(
        destinationFormState.experiences
          .map((experience) => parseFirebaseObjectPathFromImageUrl(experience.thumbnailUrl))
          .filter((objectPath): objectPath is string => objectPath != null)
      )
      const removedExistingExperienceThumbnailUrls = destinationFormState.existingExperienceThumbnailUrls.filter((thumbnailUrl) => {
        const objectPath = parseFirebaseObjectPathFromImageUrl(thumbnailUrl)
        if (objectPath == null) {
          return !destinationFormState.experiences.some((experience) => experience.thumbnailUrl === thumbnailUrl)
        }
        return !currentExperienceThumbnailObjectPathSet.has(objectPath)
      })

      if (destinationFormState.selectedId == null) {
        await adminApiClient.post<Destination>("/api/admin/v1/destinations", payload)
        setNoticeMessage("장소가 생성되었습니다.")
      } else {
        await adminApiClient.put<Destination>(`/api/admin/v1/destinations/${destinationFormState.selectedId}`, payload)
        const failedDeleteCount = await deleteImagesInFirebaseStorage([
          ...removedExistingImageUrls,
          ...removedExistingExperienceThumbnailUrls
        ])
        if (failedDeleteCount > 0) {
          setNoticeMessage(`장소가 수정되었습니다. 삭제된 사진 ${failedDeleteCount}건은 Firebase 정리에 실패했습니다.`)
        } else {
          setNoticeMessage("장소가 수정되었습니다.")
        }
      }
      setDestinationFormState(createInitialDestinationFormState())
      await loadAll()
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "장소 저장에 실패했습니다.")
    }
  }, [createDestinationPayload, deleteImagesInFirebaseStorage, destinationFormState, loadAll])

  const deleteDestination = useCallback(async (destinationId: number) => {
    setErrorMessage("")
    setNoticeMessage("")

    try {
      const targetDestination = destinations.find((destination) => destination.id === destinationId)
      await adminApiClient.delete<void>(`/api/admin/v1/destinations/${destinationId}`)
      const failedDeleteCount = await deleteImagesInFirebaseStorage([
        ...(targetDestination?.images.map((image) => image.imageUrl) ?? []),
        ...(targetDestination?.experiences.map((experience) => experience.thumbnailUrl) ?? [])
      ])
      if (failedDeleteCount > 0) {
        setNoticeMessage(`장소가 삭제되었습니다. 사진 ${failedDeleteCount}건은 Firebase 정리에 실패했습니다.`)
      } else {
        setNoticeMessage("장소가 삭제되었습니다.")
      }
      await loadAll()
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "장소 삭제에 실패했습니다.")
    }
  }, [deleteImagesInFirebaseStorage, destinations, loadAll])

  const submitMember = useCallback(async () => {
    setErrorMessage("")
    setNoticeMessage("")

    try {
      if (memberFormState.selectedId == null) {
        const payload: MemberCreateRequest = {
          id: memberFormState.id,
          nickname: memberFormState.nickname.trim().length > 0 ? memberFormState.nickname : null,
          preferredDayOff: Number(memberFormState.preferredDayOff),
          remainingDayOff: Number(memberFormState.remainingDayOff),
          onboardingCompleted: memberFormState.onboardingCompleted
        }
        await adminApiClient.post<Member>("/api/admin/v1/members", payload)
        setNoticeMessage("사용자가 생성되었습니다.")
      } else {
        const payload: MemberUpdateRequest = {
          nickname: memberFormState.nickname.trim().length > 0 ? memberFormState.nickname : null,
          preferredDayOff: Number(memberFormState.preferredDayOff),
          remainingDayOff: Number(memberFormState.remainingDayOff),
          onboardingCompleted: memberFormState.onboardingCompleted
        }
        await adminApiClient.put<Member>(`/api/admin/v1/members/${memberFormState.selectedId}`, payload)
        setNoticeMessage("사용자가 수정되었습니다.")
      }

      setMemberFormState(createInitialMemberFormState())
      await loadAll()
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "사용자 저장에 실패했습니다.")
    }
  }, [loadAll, memberFormState])

  const deleteMember = useCallback(async (memberId: string) => {
    setErrorMessage("")
    setNoticeMessage("")

    try {
      await adminApiClient.delete<void>(`/api/admin/v1/members/${memberId}`)
      setNoticeMessage("사용자가 삭제되었습니다.")
      await loadAll()
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "사용자 삭제에 실패했습니다.")
    }
  }, [loadAll])

  const submitHoliday = useCallback(async () => {
    setErrorMessage("")
    setNoticeMessage("")

    try {
      const payload: PublicHolidayUpsertRequest = {
        holidayDate: holidayFormState.holidayDate,
        name: holidayFormState.name,
        isActualHoliday: holidayFormState.isActualHoliday
      }

      if (holidayFormState.selectedId == null) {
        await adminApiClient.post<PublicHoliday>("/api/admin/v1/public-holidays", payload)
        setNoticeMessage("공휴일이 생성되었습니다.")
      } else {
        await adminApiClient.put<PublicHoliday>(`/api/admin/v1/public-holidays/${holidayFormState.selectedId}`, payload)
        setNoticeMessage("공휴일이 수정되었습니다.")
      }

      setHolidayFormState(createInitialHolidayFormState())
      await loadAll()
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "공휴일 저장에 실패했습니다.")
    }
  }, [holidayFormState, loadAll])

  const deleteHoliday = useCallback(async (holidayId: number) => {
    setErrorMessage("")
    setNoticeMessage("")

    try {
      await adminApiClient.delete<void>(`/api/admin/v1/public-holidays/${holidayId}`)
      setNoticeMessage("공휴일이 삭제되었습니다.")
      await loadAll()
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "공휴일 삭제에 실패했습니다.")
    }
  }, [loadAll])

  const submitCountry = useCallback(async () => {
    setErrorMessage("")
    setNoticeMessage("")

    try {
      const payload: CountryUpsertRequest = {
        name: countryFormState.name.trim(),
        currencyCode: countryFormState.currencyCode.trim().length > 0 ? countryFormState.currencyCode.trim().toUpperCase() : null,
        bigMacIndex: countryFormState.bigMacIndex.trim().length > 0 ? Number(countryFormState.bigMacIndex.trim()) : null
      }

      if (payload.name.length === 0) {
        throw new Error("국가 이름을 입력해주세요.")
      }
      if (payload.bigMacIndex != null && Number.isNaN(payload.bigMacIndex)) {
        throw new Error("빅맥 지수는 숫자로 입력해주세요.")
      }

      if (countryFormState.selectedId == null) {
        await adminApiClient.post<Country>("/api/admin/v1/countries", payload)
        setNoticeMessage("국가가 생성되었습니다.")
      } else {
        await adminApiClient.put<Country>(`/api/admin/v1/countries/${countryFormState.selectedId}`, payload)
        setNoticeMessage("국가가 수정되었습니다.")
      }

      setCountryFormState(createInitialCountryFormState())
      await loadAll()
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "국가 저장에 실패했습니다.")
    }
  }, [countryFormState.bigMacIndex, countryFormState.currencyCode, countryFormState.name, countryFormState.selectedId, loadAll])

  const deleteCountry = useCallback(async (countryId: number) => {
    setErrorMessage("")
    setNoticeMessage("")

    try {
      await adminApiClient.delete<void>(`/api/admin/v1/countries/${countryId}`)
      setNoticeMessage("국가가 삭제되었습니다.")
      await loadAll()
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "국가 삭제에 실패했습니다.")
    }
  }, [loadAll])

  const resetDestinationFormWithCleanup = useCallback((): void => {
    void (async () => {
      await cleanupPendingUploadedImages(
        destinationFormState.newlyUploadedImageUrls,
        destinationFormState.newlyUploadedExperienceThumbnailUrls
      )
      setDestinationFormState(createInitialDestinationFormState())
    })()
  }, [cleanupPendingUploadedImages, destinationFormState.newlyUploadedExperienceThumbnailUrls, destinationFormState.newlyUploadedImageUrls])

  const loadDestinationToForm = useCallback((destination: Destination): void => {
    void (async () => {
      await cleanupPendingUploadedImages(
        destinationFormState.newlyUploadedImageUrls,
        destinationFormState.newlyUploadedExperienceThumbnailUrls
      )
      setDestinationFormState({
        selectedId: destination.id,
        countryId: destination.countryId == null ? "" : String(destination.countryId),
        countryName: destination.countryName,
        storageCountrySlug: parseStorageSlugsFromImageUrl(destination.images[0]?.imageUrl ?? "").countrySlug,
        storageCitySlug: parseStorageSlugsFromImageUrl(destination.images[0]?.imageUrl ?? "").citySlug,
        name: destination.name,
        summary: destination.summary ?? "",
        description: destination.description ?? "",
        recommendStartMonth1: destination.recommendStartMonth1 == null ? "" : String(destination.recommendStartMonth1),
        recommendEndMonth1: destination.recommendEndMonth1 == null ? "" : String(destination.recommendEndMonth1),
        recommendStartMonth2: destination.recommendStartMonth2 == null ? "" : String(destination.recommendStartMonth2),
        recommendEndMonth2: destination.recommendEndMonth2 == null ? "" : String(destination.recommendEndMonth2),
        flightTime: destination.flightTimeMinutes == null ? "" : String(destination.flightTimeMinutes),
        flightUrl: destination.flightUrl ?? "",
        weatherTemp1: destination.weatherTemp1 == null ? "" : String(destination.weatherTemp1),
        weatherPrecipitationMm1: destination.weatherPrecipitationMm1 == null ? "" : String(destination.weatherPrecipitationMm1),
        weatherTemp2: destination.weatherTemp2 == null ? "" : String(destination.weatherTemp2),
        weatherPrecipitationMm2: destination.weatherPrecipitationMm2 == null ? "" : String(destination.weatherPrecipitationMm2),
        images: destination.images.map((image) => ({
          imageUrl: image.imageUrl,
          isThumbnail: image.isThumbnail,
          sortOrder: image.sortOrder
        })),
        experiences: destination.experiences.map((experience) => ({
          title: experience.title,
          description: experience.description,
          thumbnailUrl: experience.thumbnailUrl,
          link: experience.link,
          sortOrder: experience.sortOrder
        })),
        existingImageUrls: destination.images.map((image) => image.imageUrl),
        existingExperienceThumbnailUrls: destination.experiences.map((experience) => experience.thumbnailUrl),
        newlyUploadedImageUrls: [],
        newlyUploadedExperienceThumbnailUrls: []
      })
    })()
  }, [cleanupPendingUploadedImages, destinationFormState.newlyUploadedExperienceThumbnailUrls, destinationFormState.newlyUploadedImageUrls])

  const loadCountryToForm = useCallback((country: Country): void => {
    setCountryFormState({
      selectedId: country.id,
      name: country.name,
      currencyCode: country.currencyCode ?? "",
      bigMacIndex: country.bigMacIndex == null ? "" : String(country.bigMacIndex)
    })
  }, [])

  return {
    isAuthenticated,
    loginUsername,
    loginPassword,
    loginLoading,
    activeTab,
    loading,
    noticeMessage,
    errorMessage,
    countries,
    destinations,
    members,
    publicHolidays,
    destinationListSearchKeyword,
    memberListSearchKeyword,
    holidayListSearchKeyword,
    countryListSearchKeyword,
    destinationFormState,
    memberFormState,
    holidayFormState,
    countryFormState,
    storageCountryOptions,
    selectedStorageCountryOption,
    filteredDestinations,
    filteredMembers,
    filteredPublicHolidays,
    filteredCountries,
    setLoginUsername,
    setLoginPassword,
    setActiveTab,
    setDestinationListSearchKeyword,
    setMemberListSearchKeyword,
    setHolidayListSearchKeyword,
    setCountryListSearchKeyword,
    setDestinationFormState,
    setMemberFormState,
    setHolidayFormState,
    setCountryFormState,
    setNoticeMessage,
    setErrorMessage,
    loadAll,
    submitAdminLogin,
    submitAdminLogout,
    submitDestination,
    deleteDestination,
    submitMember,
    deleteMember,
    submitHoliday,
    deleteHoliday,
    submitCountry,
    deleteCountry,
    resetDestinationFormWithCleanup,
    loadDestinationToForm,
    loadCountryToForm
  }
}
