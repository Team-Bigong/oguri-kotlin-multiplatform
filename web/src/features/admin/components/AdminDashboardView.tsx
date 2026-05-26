import React from "react"
import { ActivityIndicator, ScrollView, Text, View } from "react-native"
import { Country, Destination, Member, PublicHoliday } from "../../../types/admin"
import { ActionButton } from "./ActionButton"
import { CountriesSection } from "./CountriesSection"
import { DestinationsSection } from "./DestinationsSection"
import { HolidaysSection } from "./HolidaysSection"
import { MembersSection } from "./MembersSection"
import { TabButton } from "./TabButton"
import { styles } from "../styles/adminStyles"
import {
  AdminTab,
  DestinationFormState,
  HolidayFormState,
  MemberFormState,
  CountryFormState,
  StorageCountryOption,
  UploadStage
} from "../types/adminLocalTypes"
import { createInitialCountryFormState, createInitialHolidayFormState, createInitialMemberFormState, parseFlightTimeMinutes } from "../utils/adminHelpers"

type AdminDashboardViewProps = {
  isWideDesktopLayout: boolean
  activeTab: AdminTab
  onChangeTab: (tab: AdminTab) => void
  onReload: () => void
  onLogout: () => void
  loading: boolean
  noticeMessage: string
  errorMessage: string
  isUploadingImages: boolean
  currentUploadStage: UploadStage | null
  countries: Country[]
  destinations: Destination[]
  members: Member[]
  publicHolidays: PublicHoliday[]
  storageCountryOptions: StorageCountryOption[]
  selectedStorageCountryOption: StorageCountryOption | undefined
  destinationFormState: DestinationFormState
  setDestinationFormState: React.Dispatch<React.SetStateAction<DestinationFormState>>
  uploadingDestinationImageCount: number
  uploadingExperienceIndexes: number[]
  memberFormState: MemberFormState
  setMemberFormState: React.Dispatch<React.SetStateAction<MemberFormState>>
  holidayFormState: HolidayFormState
  setHolidayFormState: React.Dispatch<React.SetStateAction<HolidayFormState>>
  destinationListSearchKeyword: string
  setDestinationListSearchKeyword: React.Dispatch<React.SetStateAction<string>>
  filteredDestinations: Destination[]
  memberListSearchKeyword: string
  setMemberListSearchKeyword: React.Dispatch<React.SetStateAction<string>>
  filteredMembers: Member[]
  holidayListSearchKeyword: string
  setHolidayListSearchKeyword: React.Dispatch<React.SetStateAction<string>>
  filteredPublicHolidays: PublicHoliday[]
  countryFormState: CountryFormState
  setCountryFormState: React.Dispatch<React.SetStateAction<CountryFormState>>
  countryListSearchKeyword: string
  setCountryListSearchKeyword: React.Dispatch<React.SetStateAction<string>>
  filteredCountries: Country[]
  handleImageFileSelection: (event: React.ChangeEvent<HTMLInputElement>) => void
  handleRemoveDestinationImage: (destinationImageIndex: number) => void
  addExperienceItem: () => void
  moveExperienceItem: (experienceIndex: number, direction: "up" | "down") => void
  removeExperienceItem: (experienceIndex: number) => void
  handleExperienceThumbnailFileSelection: (
    experienceIndex: number,
    event: React.ChangeEvent<HTMLInputElement>
  ) => void
  onSubmitDestination: () => void
  onResetDestinationForm: () => void
  onLoadDestination: (destination: Destination) => void
  onDeleteDestination: (destinationId: number) => void
  onSubmitMember: () => void
  onDeleteMember: (memberId: string) => void
  onSubmitHoliday: () => void
  onDeleteHoliday: (holidayId: number) => void
  onSubmitCountry: () => void
  onDeleteCountry: (countryId: number) => void
  onLoadCountry: (country: Country) => void
}

export const AdminDashboardView = ({
  isWideDesktopLayout,
  activeTab,
  onChangeTab,
  onReload,
  onLogout,
  loading,
  noticeMessage,
  errorMessage,
  isUploadingImages,
  currentUploadStage,
  countries,
  destinations,
  members,
  publicHolidays,
  storageCountryOptions,
  selectedStorageCountryOption,
  destinationFormState,
  setDestinationFormState,
  uploadingDestinationImageCount,
  uploadingExperienceIndexes,
  memberFormState,
  setMemberFormState,
  holidayFormState,
  setHolidayFormState,
  destinationListSearchKeyword,
  setDestinationListSearchKeyword,
  filteredDestinations,
  memberListSearchKeyword,
  setMemberListSearchKeyword,
  filteredMembers,
  holidayListSearchKeyword,
  setHolidayListSearchKeyword,
  filteredPublicHolidays,
  countryFormState,
  setCountryFormState,
  countryListSearchKeyword,
  setCountryListSearchKeyword,
  filteredCountries,
  handleImageFileSelection,
  handleRemoveDestinationImage,
  addExperienceItem,
  moveExperienceItem,
  removeExperienceItem,
  handleExperienceThumbnailFileSelection,
  onSubmitDestination,
  onResetDestinationForm,
  onLoadDestination,
  onDeleteDestination,
  onSubmitMember,
  onDeleteMember,
  onSubmitHoliday,
  onDeleteHoliday,
  onSubmitCountry,
  onDeleteCountry,
  onLoadCountry
}: AdminDashboardViewProps): React.JSX.Element => {
  const activeTitle = activeTab === "destinations"
    ? "장소 DB 관리"
    : activeTab === "members"
      ? "사용자 DB 관리"
      : activeTab === "holidays"
        ? "공휴일 DB 관리"
        : "나라 DB 관리"

  const totalDestinationImageCount = destinations.reduce((totalCount, destination) => {
    return totalCount + destination.images.length
  }, 0)
  const totalDestinationExperienceCount = destinations.reduce((totalCount, destination) => {
    return totalCount + destination.experiences.length
  }, 0)
  const dashboardSummaryItems = [
    { label: "장소", value: destinations.length, accentColor: "#2865d8" },
    { label: "체험", value: totalDestinationExperienceCount, accentColor: "#0f9d8f" },
    { label: "장소 이미지", value: totalDestinationImageCount, accentColor: "#2f7b4a" },
    { label: "사용자", value: members.length, accentColor: "#5b53d9" },
    { label: "공휴일", value: publicHolidays.length, accentColor: "#b0671f" }
  ]

  return (
    <View style={styles.adminLayout}>
      <View style={styles.sidebarContainer}>
          <Text style={styles.sidebarBrandTitle}>오구리 어드민</Text>
          <Text style={styles.sidebarBrandDescription}>운영 데이터를 한 화면에서 빠르게 관리하세요</Text>
          <View style={styles.sidebarTabList}>
            <TabButton label="장소 관리" selected={activeTab === "destinations"} onPress={() => onChangeTab("destinations")} />
            <TabButton label="사용자 관리" selected={activeTab === "members"} onPress={() => onChangeTab("members")} />
            <TabButton label="공휴일 관리" selected={activeTab === "holidays"} onPress={() => onChangeTab("holidays")} />
            <TabButton label="나라 관리" selected={activeTab === "countries"} onPress={() => onChangeTab("countries")} />
          </View>
        </View>

      <View style={styles.mainPanel}>
          <View style={styles.headerContainer}>
            <View>
              <Text style={styles.headline}>Oguri Admin Dashboard</Text>
              <Text style={styles.subtitle}>{activeTitle}</Text>
            </View>
            <View style={styles.headerActionRow}>
              <ActionButton label="새로고침" variant="secondary" onPress={onReload} />
              <ActionButton label="로그아웃" variant="danger" onPress={onLogout} />
            </View>
          </View>

          <View style={styles.metricCardContainer}>
            {dashboardSummaryItems.map((summaryItem) => (
              <View style={styles.metricCard} key={summaryItem.label}>
                <View style={[styles.metricCardAccent, { backgroundColor: summaryItem.accentColor }]} />
                <Text style={styles.metricCardLabel}>{summaryItem.label}</Text>
                <Text style={styles.metricCardValue}>{summaryItem.value}</Text>
              </View>
            ))}
          </View>

          {noticeMessage.length > 0 && <Text style={styles.noticeText}>{noticeMessage}</Text>}
          {errorMessage.length > 0 && <Text style={styles.errorText}>{errorMessage}</Text>}
          {isUploadingImages && (
            <View style={styles.uploadStatusCard}>
              <ActivityIndicator color="#2a6bd8" />
              <View style={styles.uploadStatusTextContainer}>
                <Text style={styles.uploadStatusTitle}>이미지 업로드 진행 중</Text>
                <Text style={styles.uploadStatusDescription}>
                  {currentUploadStage === "preparing" ? "크롭/압축 처리 중" : "Firebase 업로드 중"} · 다른 입력 작업은 계속 가능합니다.
                </Text>
              </View>
            </View>
          )}

          {loading ? (
            <View style={styles.loadingContainer}>
              <ActivityIndicator color="#2a6bd8" />
            </View>
          ) : (
            <ScrollView style={styles.contentContainer} contentContainerStyle={styles.contentInnerContainer}>
              {activeTab === "destinations" && (
                <DestinationsSection
                  isWideDesktopLayout={isWideDesktopLayout}
                  countries={countries}
                  storageCountryOptions={storageCountryOptions}
                  selectedStorageCountryOption={selectedStorageCountryOption}
                  destinationFormState={destinationFormState}
                  setDestinationFormState={setDestinationFormState}
                  parseFlightTimeMinutes={parseFlightTimeMinutes}
                  handleImageFileSelection={handleImageFileSelection}
                  uploadingDestinationImageCount={uploadingDestinationImageCount}
                  handleRemoveDestinationImage={handleRemoveDestinationImage}
                  addExperienceItem={addExperienceItem}
                  moveExperienceItem={moveExperienceItem}
                  removeExperienceItem={removeExperienceItem}
                  handleExperienceThumbnailFileSelection={handleExperienceThumbnailFileSelection}
                  uploadingExperienceIndexes={uploadingExperienceIndexes}
                  onSubmitDestination={onSubmitDestination}
                  onResetDestinationForm={onResetDestinationForm}
                  destinationListSearchKeyword={destinationListSearchKeyword}
                  setDestinationListSearchKeyword={setDestinationListSearchKeyword}
                  filteredDestinations={filteredDestinations}
                  onLoadDestination={onLoadDestination}
                  onDeleteDestination={onDeleteDestination}
                />
              )}

              {activeTab === "members" && (
                <MembersSection
                  isWideDesktopLayout={isWideDesktopLayout}
                  memberFormState={memberFormState}
                  setMemberFormState={setMemberFormState}
                  filteredMembers={filteredMembers}
                  memberListSearchKeyword={memberListSearchKeyword}
                  setMemberListSearchKeyword={setMemberListSearchKeyword}
                  onSubmitMember={onSubmitMember}
                  onDeleteMember={onDeleteMember}
                  onResetMemberForm={() => setMemberFormState(createInitialMemberFormState())}
                />
              )}

              {activeTab === "holidays" && (
                <HolidaysSection
                  isWideDesktopLayout={isWideDesktopLayout}
                  holidayFormState={holidayFormState}
                  setHolidayFormState={setHolidayFormState}
                  filteredPublicHolidays={filteredPublicHolidays}
                  holidayListSearchKeyword={holidayListSearchKeyword}
                  setHolidayListSearchKeyword={setHolidayListSearchKeyword}
                  onSubmitHoliday={onSubmitHoliday}
                  onDeleteHoliday={onDeleteHoliday}
                  onResetHolidayForm={() => setHolidayFormState(createInitialHolidayFormState())}
                />
              )}

              {activeTab === "countries" && (
                <CountriesSection
                  isWideDesktopLayout={isWideDesktopLayout}
                  countryFormState={countryFormState}
                  setCountryFormState={setCountryFormState}
                  filteredCountries={filteredCountries}
                  countryListSearchKeyword={countryListSearchKeyword}
                  setCountryListSearchKeyword={setCountryListSearchKeyword}
                  onSubmitCountry={onSubmitCountry}
                  onDeleteCountry={onDeleteCountry}
                  onLoadCountry={onLoadCountry}
                  onResetCountryForm={() => setCountryFormState(createInitialCountryFormState())}
                />
              )}
            </ScrollView>
          )}
      </View>
    </View>
  )
}
