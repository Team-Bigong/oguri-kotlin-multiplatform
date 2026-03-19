import React, { useCallback, useEffect, useMemo, useState } from "react"
import { ActivityIndicator, Pressable, ScrollView, StyleSheet, Text, TextInput, View } from "react-native"
import { adminApiClient, clearAdminAccessToken, getAdminAccessToken, setAdminAccessToken } from "../../lib/apiClient"
import { resizeAndCompressImage } from "../../lib/imageProcessing"
import { uploadImageToFirebaseStorage } from "../../lib/firebase"
import {
  AdminLoginResponse,
  Country,
  Destination,
  DestinationImageRequest,
  DestinationUpsertRequest,
  Member,
  MemberCreateRequest,
  MemberUpdateRequest,
  PublicHoliday,
  PublicHolidayUpsertRequest
} from "../../types/admin"

type AdminTab = "destinations" | "members" | "holidays"

type DestinationFormState = {
  selectedId: number | null
  countryId: string
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
  images: DestinationImageRequest[]
}

type MemberFormState = {
  selectedId: string | null
  id: string
  nickname: string
  preferredDayOff: string
  remainingDayOff: string
  onboardingCompleted: boolean
}

type HolidayFormState = {
  selectedId: number | null
  holidayDate: string
  name: string
  isActualHoliday: boolean
}

const createInitialDestinationFormState = (): DestinationFormState => ({
  selectedId: null,
  countryId: "",
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
  images: []
})

const createInitialMemberFormState = (): MemberFormState => ({
  selectedId: null,
  id: "",
  nickname: "",
  preferredDayOff: "3",
  remainingDayOff: "3",
  onboardingCompleted: false
})

const createInitialHolidayFormState = (): HolidayFormState => ({
  selectedId: null,
  holidayDate: "",
  name: "",
  isActualHoliday: true
})

const toStoragePathSegment = (value: string): string => {
  return value
    .trim()
    .toLowerCase()
    .replace(/\s+/g, "-")
    .replace(/[^a-z0-9-_]/g, "")
}

const parseFlightTimeMinutes = (value: string): string => {
  return value.replace(/[^0-9]/g, "")
}

type StorageCountryOption = {
  label: string
  slug: string
  cityOptions: Array<{ label: string; slug: string }>
}

const storageCountryOptions: StorageCountryOption[] = [
  {
    label: "일본",
    slug: "japan",
    cityOptions: [
      { label: "도쿄", slug: "tokyo" },
      { label: "오사카", slug: "osaka" },
      { label: "후쿠오카", slug: "fukuoka" },
      { label: "삿포로", slug: "sapporo" }
    ]
  },
  {
    label: "호주",
    slug: "australia",
    cityOptions: [
      { label: "브리즈번", slug: "brisbane" },
      { label: "시드니", slug: "sydney" },
      { label: "멜버른", slug: "melbourne" }
    ]
  },
  {
    label: "미국",
    slug: "united-states",
    cityOptions: [
      { label: "뉴욕", slug: "new-york" },
      { label: "샌프란시스코", slug: "san-francisco" },
      { label: "LA", slug: "los-angeles" }
    ]
  },
  {
    label: "중국",
    slug: "china",
    cityOptions: [
      { label: "상하이", slug: "shanghai" },
      { label: "베이징", slug: "beijing" },
      { label: "칭다오", slug: "qingdao" }
    ]
  },
  {
    label: "필리핀",
    slug: "philippines",
    cityOptions: [
      { label: "세부", slug: "cebu" },
      { label: "보라카이", slug: "boracay" },
      { label: "보홀", slug: "bohol" }
    ]
  },
  {
    label: "프랑스",
    slug: "france",
    cityOptions: [
      { label: "파리", slug: "paris" },
      { label: "니스", slug: "nice" }
    ]
  },
  {
    label: "스페인",
    slug: "spain",
    cityOptions: [
      { label: "바르셀로나", slug: "barcelona" },
      { label: "마드리드", slug: "madrid" }
    ]
  },
  {
    label: "베트남",
    slug: "vietnam",
    cityOptions: [
      { label: "다낭", slug: "danang" },
      { label: "호치민", slug: "ho-chi-minh" }
    ]
  },
  {
    label: "대한민국",
    slug: "south-korea",
    cityOptions: [
      { label: "서울", slug: "seoul" },
      { label: "부산", slug: "busan" },
      { label: "제주도", slug: "jeju" }
    ]
  }
]

const parseStorageSlugsFromImageUrl = (imageUrl: string): { countrySlug: string; citySlug: string } => {
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

export const AdminApp = (): React.JSX.Element => {
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

  const [destinationFormState, setDestinationFormState] = useState<DestinationFormState>(createInitialDestinationFormState)
  const [memberFormState, setMemberFormState] = useState<MemberFormState>(createInitialMemberFormState)
  const [holidayFormState, setHolidayFormState] = useState<HolidayFormState>(createInitialHolidayFormState)

  const [uploadingImages, setUploadingImages] = useState<boolean>(false)
  const selectedStorageCountryOption = storageCountryOptions.find(
    (countryOption) => countryOption.slug === destinationFormState.storageCountrySlug
  )

  const loadAll = useCallback(async () => {
    if (!isAuthenticated) {
      return
    }

    setLoading(true)
    setErrorMessage("")

    try {
      const [countryList, destinationList, memberList, holidayList] = await Promise.all([
        adminApiClient.get<Country[]>("/api/admin/v1/countries"),
        adminApiClient.get<Destination[]>("/api/admin/v1/destinations"),
        adminApiClient.get<Member[]>("/api/admin/v1/members"),
        adminApiClient.get<PublicHoliday[]>("/api/admin/v1/public-holidays")
      ])

      setCountries(countryList)
      setDestinations(destinationList)
      setMembers(memberList)
      setPublicHolidays(holidayList)
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

    const flightTimeMinutes = parseFlightTimeMinutes(state.flightTime)

    return {
      countryId: Number(state.countryId),
      name: state.name,
      summary: state.summary.trim().length > 0 ? state.summary : null,
      description: state.description.trim().length > 0 ? state.description : null,
      recommendStartMonth1: parseMonth(state.recommendStartMonth1),
      recommendEndMonth1: parseMonth(state.recommendEndMonth1),
      recommendStartMonth2: parseMonth(state.recommendStartMonth2),
      recommendEndMonth2: parseMonth(state.recommendEndMonth2),
      flightTimeMinutes: flightTimeMinutes.length > 0 ? Number(flightTimeMinutes) : null,
      images: state.images
    }
  }, [])

  const submitDestination = useCallback(async () => {
    setErrorMessage("")
    setNoticeMessage("")

    try {
      const payload = createDestinationPayload(destinationFormState)
      if (destinationFormState.selectedId == null) {
        await adminApiClient.post<Destination>("/api/admin/v1/destinations", payload)
        setNoticeMessage("장소가 생성되었습니다.")
      } else {
        await adminApiClient.put<Destination>(`/api/admin/v1/destinations/${destinationFormState.selectedId}`, payload)
        setNoticeMessage("장소가 수정되었습니다.")
      }
      setDestinationFormState(createInitialDestinationFormState())
      await loadAll()
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "장소 저장에 실패했습니다.")
    }
  }, [createDestinationPayload, destinationFormState, loadAll])

  const deleteDestination = useCallback(async (destinationId: number) => {
    setErrorMessage("")
    setNoticeMessage("")

    try {
      await adminApiClient.delete<void>(`/api/admin/v1/destinations/${destinationId}`)
      setNoticeMessage("장소가 삭제되었습니다.")
      await loadAll()
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "장소 삭제에 실패했습니다.")
    }
  }, [loadAll])

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

  const handleImageFileSelection = useCallback(async (event: React.ChangeEvent<HTMLInputElement>) => {
    const fileList = event.target.files
    if (fileList == null || fileList.length === 0) {
      return
    }

    setUploadingImages(true)
    setErrorMessage("")
    setNoticeMessage("")

    try {
      const countryPathSegment = toStoragePathSegment(destinationFormState.storageCountrySlug)
      const cityPathSegment = toStoragePathSegment(destinationFormState.storageCitySlug)
      if (countryPathSegment.length === 0 || cityPathSegment.length === 0) {
        throw new Error("Storage 국가/도시 경로를 먼저 입력하거나 메뉴에서 선택해주세요.")
      }

      const selectedFiles = Array.from(fileList)
      const uploadedImages = await Promise.all(
        selectedFiles.map(async (file, index) => {
          const compressedBinary = await resizeAndCompressImage(file)
          const imageSequenceNumber = destinationFormState.images.length + index + 1
          const objectPath = `places/${countryPathSegment}/${cityPathSegment}/${imageSequenceNumber}.jpg`
          const imageUrl = await uploadImageToFirebaseStorage(compressedBinary, objectPath)

          return {
            imageUrl,
            isThumbnail: destinationFormState.images.length === 0 && index === 0,
            sortOrder: destinationFormState.images.length + index + 1
          } as DestinationImageRequest
        })
      )

      setDestinationFormState((previousState) => ({
        ...previousState,
        images: [...previousState.images, ...uploadedImages]
      }))
      setNoticeMessage("이미지 업로드가 완료되었습니다.")
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "이미지 업로드에 실패했습니다.")
    } finally {
      setUploadingImages(false)
      event.target.value = ""
    }
  }, [destinationFormState.images.length, destinationFormState.storageCitySlug, destinationFormState.storageCountrySlug])

  const activeTitle = useMemo(() => {
    if (activeTab === "destinations") return "장소 DB 관리"
    if (activeTab === "members") return "사용자 DB 관리"
    return "공휴일 DB 관리"
  }, [activeTab])

  if (!isAuthenticated) {
    return (
      <View style={styles.loginPage}>
        <View style={styles.loginCard}>
          <Text style={styles.loginTitle}>Oguri Admin Login</Text>
          <Text style={styles.loginDescription}>관리자 계정으로 로그인 후 DB 관리 기능을 사용할 수 있습니다.</Text>
          <LabelInput label="아이디" value={loginUsername} onChangeText={setLoginUsername} />
          <LabelInput label="비밀번호" value={loginPassword} onChangeText={setLoginPassword} secureTextEntry />
          {errorMessage.length > 0 && <Text style={styles.errorText}>{errorMessage}</Text>}
          <ActionButton
            label={loginLoading ? "로그인 중..." : "로그인"}
            onPress={() => {
              if (!loginLoading) {
                void submitAdminLogin()
              }
            }}
          />
        </View>
      </View>
    )
  }

  return (
    <View style={styles.page}>
      <View style={styles.headerContainer}>
        <Text style={styles.headline}>Oguri Admin</Text>
        <Text style={styles.subtitle}>{activeTitle}</Text>
        <View style={styles.headerActionRow}>
          <ActionButton label="로그아웃" variant="secondary" onPress={submitAdminLogout} />
        </View>
      </View>

      <View style={styles.tabRow}>
        <TabButton label="장소" selected={activeTab === "destinations"} onPress={() => setActiveTab("destinations")} />
        <TabButton label="사용자" selected={activeTab === "members"} onPress={() => setActiveTab("members")} />
        <TabButton label="공휴일" selected={activeTab === "holidays"} onPress={() => setActiveTab("holidays")} />
      </View>

      {noticeMessage.length > 0 && <Text style={styles.noticeText}>{noticeMessage}</Text>}
      {errorMessage.length > 0 && <Text style={styles.errorText}>{errorMessage}</Text>}

      {loading ? (
        <View style={styles.loadingContainer}>
          <ActivityIndicator color="#1f6b45" />
        </View>
      ) : (
        <ScrollView style={styles.contentContainer} contentContainerStyle={styles.contentInnerContainer}>
          {activeTab === "destinations" && (
            <View style={styles.sectionCard}>
              <Text style={styles.sectionTitle}>장소 추가/수정</Text>
              <View style={styles.row}>
                <Text style={styles.fieldLabel}>국가</Text>
                <select
                  value={destinationFormState.countryId}
                  onChange={(event) => {
                    setDestinationFormState((previousState) => ({ ...previousState, countryId: event.target.value }))
                  }}
                  style={htmlFieldStyle}
                >
                  <option value="">국가 선택</option>
                  {countries.map((country) => (
                    <option value={String(country.id)} key={country.id}>{country.name}</option>
                  ))}
                </select>
              </View>

              <View style={styles.row}>
                <Text style={styles.fieldLabel}>Storage 국가 경로</Text>
                <select
                  value={destinationFormState.storageCountrySlug}
                  onChange={(event) => {
                    const selectedSlug = event.target.value
                    const countryOption = storageCountryOptions.find((item) => item.slug === selectedSlug)
                    setDestinationFormState((previousState) => ({
                      ...previousState,
                      storageCountrySlug: selectedSlug,
                      storageCitySlug: countryOption?.cityOptions[0]?.slug ?? ""
                    }))
                  }}
                  style={htmlFieldStyle}
                >
                  <option value="">경로 국가 선택</option>
                  {storageCountryOptions.map((countryOption) => (
                    <option value={countryOption.slug} key={countryOption.slug}>
                      {countryOption.label} ({countryOption.slug})
                    </option>
                  ))}
                </select>
              </View>

              <View style={styles.row}>
                <Text style={styles.fieldLabel}>Storage 도시 경로</Text>
                <select
                  value={destinationFormState.storageCitySlug}
                  onChange={(event) => {
                    setDestinationFormState((previousState) => ({
                      ...previousState,
                      storageCitySlug: event.target.value
                    }))
                  }}
                  style={htmlFieldStyle}
                >
                  <option value="">경로 도시 선택</option>
                  {(selectedStorageCountryOption?.cityOptions ?? []).map((cityOption) => (
                    <option value={cityOption.slug} key={cityOption.slug}>
                      {cityOption.label} ({cityOption.slug})
                    </option>
                  ))}
                </select>
              </View>

              <LabelInput
                label="Storage 국가 경로 직접입력"
                value={destinationFormState.storageCountrySlug}
                onChangeText={(value) => setDestinationFormState((previousState) => ({ ...previousState, storageCountrySlug: value }))}
              />
              <LabelInput
                label="Storage 도시 경로 직접입력"
                value={destinationFormState.storageCitySlug}
                onChangeText={(value) => setDestinationFormState((previousState) => ({ ...previousState, storageCitySlug: value }))}
              />

              <LabelInput
                label="도시명"
                value={destinationFormState.name}
                onChangeText={(value) => setDestinationFormState((previousState) => ({ ...previousState, name: value }))}
              />
              <LabelInput
                label="요약"
                value={destinationFormState.summary}
                onChangeText={(value) => setDestinationFormState((previousState) => ({ ...previousState, summary: value }))}
              />
              <LabelInput
                label="설명"
                value={destinationFormState.description}
                multiline
                onChangeText={(value) => setDestinationFormState((previousState) => ({ ...previousState, description: value }))}
              />

              <View style={styles.rowSplitContainer}>
                <LabelInput
                  label="추천 시작 월 1"
                  value={destinationFormState.recommendStartMonth1}
                  keyboardType="numeric"
                  onChangeText={(value) => setDestinationFormState((previousState) => ({ ...previousState, recommendStartMonth1: value }))}
                />
                <LabelInput
                  label="추천 종료 월 1"
                  value={destinationFormState.recommendEndMonth1}
                  keyboardType="numeric"
                  onChangeText={(value) => setDestinationFormState((previousState) => ({ ...previousState, recommendEndMonth1: value }))}
                />
              </View>

              <View style={styles.rowSplitContainer}>
                <LabelInput
                  label="추천 시작 월 2"
                  value={destinationFormState.recommendStartMonth2}
                  keyboardType="numeric"
                  onChangeText={(value) => setDestinationFormState((previousState) => ({ ...previousState, recommendStartMonth2: value }))}
                />
                <LabelInput
                  label="추천 종료 월 2"
                  value={destinationFormState.recommendEndMonth2}
                  keyboardType="numeric"
                  onChangeText={(value) => setDestinationFormState((previousState) => ({ ...previousState, recommendEndMonth2: value }))}
                />
              </View>

              <LabelInput
                label="비행 시간(분)"
                value={destinationFormState.flightTime}
                keyboardType="numeric"
                onChangeText={(value) => setDestinationFormState((previousState) => ({ ...previousState, flightTime: parseFlightTimeMinutes(value) }))}
              />

              <View style={styles.uploadRow}>
                <Text style={styles.fieldLabel}>사진 업로드 (가로 1280px / 800KB 이하로 자동 압축 후 Firebase 업로드)</Text>
                <input type="file" accept="image/*" multiple onChange={handleImageFileSelection} />
                {uploadingImages && <Text style={styles.helperText}>이미지를 처리 중입니다...</Text>}
              </View>

              {destinationFormState.images.length > 0 && (
                <View style={styles.imageListContainer}>
                  {destinationFormState.images.map((image, index) => (
                    <View style={styles.imageRow} key={`${image.imageUrl}_${index}`}>
                      <Text style={styles.imageUrlText}>{image.imageUrl}</Text>
                      <View style={styles.imageRowControls}>
                        <Pressable
                          onPress={() => {
                            setDestinationFormState((previousState) => ({
                              ...previousState,
                              images: previousState.images.map((targetImage, targetIndex) => ({
                                ...targetImage,
                                isThumbnail: targetIndex === index
                              }))
                            }))
                          }}
                          style={image.isThumbnail ? styles.badgePrimary : styles.badgeDefault}
                        >
                          <Text style={styles.badgeText}>{image.isThumbnail ? "썸네일" : "썸네일 지정"}</Text>
                        </Pressable>
                        <Pressable
                          onPress={() => {
                            setDestinationFormState((previousState) => ({
                              ...previousState,
                              images: previousState.images
                                .filter((_, targetIndex) => targetIndex !== index)
                                .map((targetImage, sequence) => ({ ...targetImage, sortOrder: sequence + 1 }))
                            }))
                          }}
                          style={styles.badgeDanger}
                        >
                          <Text style={styles.badgeText}>삭제</Text>
                        </Pressable>
                      </View>
                    </View>
                  ))}
                </View>
              )}

              <View style={styles.rowButtonContainer}>
                <ActionButton label={destinationFormState.selectedId == null ? "장소 생성" : "장소 수정"} onPress={() => void submitDestination()} />
                <ActionButton
                  label="폼 초기화"
                  variant="secondary"
                  onPress={() => setDestinationFormState(createInitialDestinationFormState())}
                />
              </View>

              <Text style={styles.sectionTitle}>장소 목록</Text>
              {destinations.map((destination) => (
                <View style={styles.listItemCard} key={destination.id}>
                  <Text style={styles.listItemTitle}>{destination.countryName} · {destination.name}</Text>
                  <Text style={styles.listItemDescription}>{destination.summary ?? "(요약 없음)"}</Text>
                  <Text style={styles.listItemDescription}>이미지 {destination.images.length}장</Text>
                  <View style={styles.rowButtonContainer}>
                    <ActionButton
                      label="불러오기"
                      variant="secondary"
                      onPress={() => {
                        setDestinationFormState({
                          selectedId: destination.id,
                          countryId: destination.countryId == null ? "" : String(destination.countryId),
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
                          images: destination.images.map((image) => ({
                            imageUrl: image.imageUrl,
                            isThumbnail: image.isThumbnail,
                            sortOrder: image.sortOrder
                          }))
                        })
                      }}
                    />
                    <ActionButton label="삭제" variant="danger" onPress={() => void deleteDestination(destination.id)} />
                  </View>
                </View>
              ))}
            </View>
          )}

          {activeTab === "members" && (
            <View style={styles.sectionCard}>
              <Text style={styles.sectionTitle}>사용자 추가/수정</Text>
              {memberFormState.selectedId == null && (
                <LabelInput
                  label="사용자 ID"
                  value={memberFormState.id}
                  onChangeText={(value) => setMemberFormState((previousState) => ({ ...previousState, id: value }))}
                />
              )}
              <LabelInput
                label="닉네임"
                value={memberFormState.nickname}
                onChangeText={(value) => setMemberFormState((previousState) => ({ ...previousState, nickname: value }))}
              />

              <View style={styles.rowSplitContainer}>
                <LabelInput
                  label="선호 연차"
                  keyboardType="numeric"
                  value={memberFormState.preferredDayOff}
                  onChangeText={(value) => setMemberFormState((previousState) => ({ ...previousState, preferredDayOff: value }))}
                />
                <LabelInput
                  label="남은 연차"
                  keyboardType="numeric"
                  value={memberFormState.remainingDayOff}
                  onChangeText={(value) => setMemberFormState((previousState) => ({ ...previousState, remainingDayOff: value }))}
                />
              </View>

              <View style={styles.row}>
                <Text style={styles.fieldLabel}>온보딩 완료</Text>
                <input
                  type="checkbox"
                  checked={memberFormState.onboardingCompleted}
                  onChange={(event) => {
                    setMemberFormState((previousState) => ({ ...previousState, onboardingCompleted: event.target.checked }))
                  }}
                />
              </View>

              <View style={styles.rowButtonContainer}>
                <ActionButton label={memberFormState.selectedId == null ? "사용자 생성" : "사용자 수정"} onPress={() => void submitMember()} />
                <ActionButton
                  label="폼 초기화"
                  variant="secondary"
                  onPress={() => setMemberFormState(createInitialMemberFormState())}
                />
              </View>

              <Text style={styles.sectionTitle}>사용자 목록</Text>
              {members.map((member) => (
                <View style={styles.listItemCard} key={member.id}>
                  <Text style={styles.listItemTitle}>{member.id}</Text>
                  <Text style={styles.listItemDescription}>{member.nickname ?? "(닉네임 없음)"}</Text>
                  <Text style={styles.listItemDescription}>연차 {member.preferredDayOff}/{member.remainingDayOff}</Text>
                  <View style={styles.rowButtonContainer}>
                    <ActionButton
                      label="불러오기"
                      variant="secondary"
                      onPress={() => {
                        setMemberFormState({
                          selectedId: member.id,
                          id: member.id,
                          nickname: member.nickname ?? "",
                          preferredDayOff: String(member.preferredDayOff),
                          remainingDayOff: String(member.remainingDayOff),
                          onboardingCompleted: member.onboardingCompleted
                        })
                      }}
                    />
                    <ActionButton label="삭제" variant="danger" onPress={() => void deleteMember(member.id)} />
                  </View>
                </View>
              ))}
            </View>
          )}

          {activeTab === "holidays" && (
            <View style={styles.sectionCard}>
              <Text style={styles.sectionTitle}>공휴일 추가/수정</Text>
              <View style={styles.row}>
                <Text style={styles.fieldLabel}>공휴일 날짜 (YYYY-MM-DD)</Text>
                <input
                  type="date"
                  value={holidayFormState.holidayDate}
                  onChange={(event) => {
                    setHolidayFormState((previousState) => ({ ...previousState, holidayDate: event.target.value }))
                  }}
                  style={htmlFieldStyle}
                />
              </View>
              <LabelInput
                label="이름"
                value={holidayFormState.name}
                onChangeText={(value) => setHolidayFormState((previousState) => ({ ...previousState, name: value }))}
              />
              <View style={styles.row}>
                <Text style={styles.fieldLabel}>실제 공휴일 여부</Text>
                <input
                  type="checkbox"
                  checked={holidayFormState.isActualHoliday}
                  onChange={(event) => {
                    setHolidayFormState((previousState) => ({ ...previousState, isActualHoliday: event.target.checked }))
                  }}
                />
              </View>

              <View style={styles.rowButtonContainer}>
                <ActionButton label={holidayFormState.selectedId == null ? "공휴일 생성" : "공휴일 수정"} onPress={() => void submitHoliday()} />
                <ActionButton
                  label="폼 초기화"
                  variant="secondary"
                  onPress={() => setHolidayFormState(createInitialHolidayFormState())}
                />
              </View>

              <Text style={styles.sectionTitle}>공휴일 목록</Text>
              {publicHolidays.map((holiday) => (
                <View style={styles.listItemCard} key={holiday.id}>
                  <Text style={styles.listItemTitle}>{holiday.holidayDate}</Text>
                  <Text style={styles.listItemDescription}>{holiday.name}</Text>
                  <Text style={styles.listItemDescription}>{holiday.isActualHoliday ? "실제 공휴일" : "참고일"}</Text>
                  <View style={styles.rowButtonContainer}>
                    <ActionButton
                      label="불러오기"
                      variant="secondary"
                      onPress={() => {
                        setHolidayFormState({
                          selectedId: holiday.id,
                          holidayDate: holiday.holidayDate,
                          name: holiday.name,
                          isActualHoliday: holiday.isActualHoliday
                        })
                      }}
                    />
                    <ActionButton label="삭제" variant="danger" onPress={() => void deleteHoliday(holiday.id)} />
                  </View>
                </View>
              ))}
            </View>
          )}
        </ScrollView>
      )}
    </View>
  )
}

type TabButtonProps = {
  label: string
  selected: boolean
  onPress: () => void
}

const TabButton = ({ label, selected, onPress }: TabButtonProps): React.JSX.Element => {
  return (
    <Pressable onPress={onPress} style={selected ? styles.tabButtonActive : styles.tabButtonDefault}>
      <Text style={selected ? styles.tabTextActive : styles.tabTextDefault}>{label}</Text>
    </Pressable>
  )
}

type ActionButtonVariant = "primary" | "secondary" | "danger"

type ActionButtonProps = {
  label: string
  onPress: () => void
  variant?: ActionButtonVariant
}

const ActionButton = ({ label, onPress, variant = "primary" }: ActionButtonProps): React.JSX.Element => {
  const style = variant === "danger" ? styles.actionButtonDanger : variant === "secondary" ? styles.actionButtonSecondary : styles.actionButtonPrimary

  return (
    <Pressable onPress={onPress} style={style}>
      <Text style={styles.actionButtonText}>{label}</Text>
    </Pressable>
  )
}

type LabelInputProps = {
  label: string
  value: string
  onChangeText: (value: string) => void
  multiline?: boolean
  keyboardType?: "default" | "numeric"
  secureTextEntry?: boolean
}

const LabelInput = ({
  label,
  value,
  onChangeText,
  multiline = false,
  keyboardType = "default",
  secureTextEntry = false
}: LabelInputProps): React.JSX.Element => {
  return (
    <View style={styles.row}>
      <Text style={styles.fieldLabel}>{label}</Text>
      <TextInput
        value={value}
        multiline={multiline}
        keyboardType={keyboardType}
        secureTextEntry={secureTextEntry}
        onChangeText={onChangeText}
        style={multiline ? styles.textInputMultiline : styles.textInput}
      />
    </View>
  )
}

const styles = StyleSheet.create({
  loginPage: {
    minHeight: "100%",
    backgroundColor: "#f4f7f3",
    padding: 20,
    justifyContent: "center",
    alignItems: "center"
  },
  loginCard: {
    width: "100%",
    maxWidth: 420,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: "#d0ddd0",
    backgroundColor: "#ffffff",
    padding: 16,
    gap: 12
  },
  loginTitle: {
    color: "#1d2e21",
    fontSize: 24,
    fontWeight: "700"
  },
  loginDescription: {
    color: "#4f6654",
    fontSize: 14
  },
  page: {
    minHeight: "100%",
    backgroundColor: "#f4f7f3",
    paddingHorizontal: 22,
    paddingVertical: 20
  },
  headerContainer: {
    marginBottom: 14
  },
  headerActionRow: {
    marginTop: 10,
    flexDirection: "row"
  },
  headline: {
    color: "#1d2e21",
    fontSize: 28,
    fontWeight: "700"
  },
  subtitle: {
    color: "#4f6654",
    fontSize: 15,
    marginTop: 6
  },
  tabRow: {
    flexDirection: "row",
    gap: 8,
    marginBottom: 14
  },
  tabButtonDefault: {
    paddingHorizontal: 14,
    paddingVertical: 10,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: "#b8cbb5",
    backgroundColor: "#f8fbf6"
  },
  tabButtonActive: {
    paddingHorizontal: 14,
    paddingVertical: 10,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: "#2c6a3d",
    backgroundColor: "#2f6e43"
  },
  tabTextDefault: {
    color: "#38513f",
    fontWeight: "600",
    fontSize: 14
  },
  tabTextActive: {
    color: "#ffffff",
    fontWeight: "700",
    fontSize: 14
  },
  noticeText: {
    color: "#1a6a3e",
    marginBottom: 10
  },
  errorText: {
    color: "#af2121",
    marginBottom: 10
  },
  loadingContainer: {
    minHeight: 300,
    justifyContent: "center",
    alignItems: "center"
  },
  contentContainer: {
    flex: 1
  },
  contentInnerContainer: {
    paddingBottom: 120
  },
  sectionCard: {
    borderRadius: 16,
    borderWidth: 1,
    borderColor: "#d0ddd0",
    backgroundColor: "#ffffff",
    padding: 16,
    gap: 12
  },
  sectionTitle: {
    color: "#26352a",
    fontSize: 18,
    fontWeight: "700",
    marginTop: 4
  },
  row: {
    gap: 7
  },
  rowSplitContainer: {
    flexDirection: "row",
    gap: 12
  },
  fieldLabel: {
    color: "#37563f",
    fontSize: 14,
    fontWeight: "600"
  },
  textInput: {
    borderWidth: 1,
    borderColor: "#bbcfbb",
    borderRadius: 10,
    backgroundColor: "#fbfdfb",
    minHeight: 44,
    paddingHorizontal: 10,
    paddingVertical: 8
  },
  textInputMultiline: {
    borderWidth: 1,
    borderColor: "#bbcfbb",
    borderRadius: 10,
    backgroundColor: "#fbfdfb",
    minHeight: 94,
    paddingHorizontal: 10,
    paddingVertical: 8,
    textAlignVertical: "top"
  },
  helperText: {
    color: "#4c6c53"
  },
  uploadRow: {
    gap: 8
  },
  imageListContainer: {
    gap: 8
  },
  imageRow: {
    borderWidth: 1,
    borderColor: "#d5e0d4",
    borderRadius: 10,
    padding: 8,
    gap: 8,
    backgroundColor: "#f8fbf8"
  },
  imageUrlText: {
    color: "#3f5a46",
    fontSize: 12
  },
  imageRowControls: {
    flexDirection: "row",
    gap: 8
  },
  badgePrimary: {
    backgroundColor: "#2f6e43",
    borderRadius: 8,
    paddingHorizontal: 10,
    paddingVertical: 6
  },
  badgeDefault: {
    backgroundColor: "#9bb59f",
    borderRadius: 8,
    paddingHorizontal: 10,
    paddingVertical: 6
  },
  badgeDanger: {
    backgroundColor: "#ad4040",
    borderRadius: 8,
    paddingHorizontal: 10,
    paddingVertical: 6
  },
  badgeText: {
    color: "#ffffff",
    fontSize: 12,
    fontWeight: "600"
  },
  rowButtonContainer: {
    flexDirection: "row",
    gap: 8,
    flexWrap: "wrap"
  },
  actionButtonPrimary: {
    borderRadius: 10,
    backgroundColor: "#2d6d43",
    paddingHorizontal: 14,
    paddingVertical: 10
  },
  actionButtonSecondary: {
    borderRadius: 10,
    backgroundColor: "#6f8673",
    paddingHorizontal: 14,
    paddingVertical: 10
  },
  actionButtonDanger: {
    borderRadius: 10,
    backgroundColor: "#ad4040",
    paddingHorizontal: 14,
    paddingVertical: 10
  },
  actionButtonText: {
    color: "#ffffff",
    fontSize: 13,
    fontWeight: "700"
  },
  listItemCard: {
    borderWidth: 1,
    borderColor: "#d4dfd3",
    borderRadius: 12,
    padding: 12,
    gap: 6,
    backgroundColor: "#f9fcf7"
  },
  listItemTitle: {
    color: "#203324",
    fontSize: 15,
    fontWeight: "700"
  },
  listItemDescription: {
    color: "#4f6754",
    fontSize: 13
  }
})

const htmlFieldStyle: React.CSSProperties = {
  minHeight: 40,
  borderColor: "#bbcfbb",
  borderRadius: 10,
  borderWidth: 1,
  padding: 8,
  backgroundColor: "#fbfdfb"
}
