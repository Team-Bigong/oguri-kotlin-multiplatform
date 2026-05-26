import React from "react"
import { useWindowDimensions, View } from "react-native"
import { AdminCropModal } from "./components/AdminCropModal"
import { AdminDashboardView } from "./components/AdminDashboardView"
import { AdminLoginView } from "./components/AdminLoginView"
import { useAdminDataManagement } from "./hooks/useAdminDataManagement"
import { useDestinationImageFormActions } from "./hooks/useDestinationImageFormActions"
import { useDestinationImageCrop } from "./hooks/useDestinationImageCrop"
import { styles } from "./styles/adminStyles"

export const AdminApp = (): React.JSX.Element => {
  const { width: windowWidth, height: windowHeight } = useWindowDimensions()
  const isWideDesktopLayout = windowWidth >= 1360

  const {
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
  } = useAdminDataManagement()

  const {
    activeUploadTaskCount,
    currentUploadStage,
    uploadingDestinationImageCount,
    uploadingExperienceIndexes,
    cropSessionState,
    cropSessionItem,
    cropImageRenderMetrics,
    primaryGuideRectInCropArea,
    secondaryGuideRectInCropArea,
    openCropSession,
    beginMoveCropArea,
    beginResizeCropArea,
    resetCurrentCropArea,
    closeCurrentCropSession,
    applyCurrentCrop
  } = useDestinationImageCrop({
    destinationFormState,
    setDestinationFormState,
    windowWidth,
    windowHeight,
    setErrorMessage,
    setNoticeMessage
  })

  const {
    handleImageFileSelection,
    handleExperienceThumbnailFileSelection,
    handleRemoveDestinationImage,
    addExperienceItem,
    moveExperienceItem,
    removeExperienceItem
  } = useDestinationImageFormActions({
    destinationFormState,
    setDestinationFormState,
    setErrorMessage,
    openCropSession
  })

  const isUploadingImages = activeUploadTaskCount > 0

  if (!isAuthenticated) {
    return (
      <AdminLoginView
        windowHeight={windowHeight}
        loginUsername={loginUsername}
        loginPassword={loginPassword}
        loginLoading={loginLoading}
        errorMessage={errorMessage}
        onChangeUsername={setLoginUsername}
        onChangePassword={setLoginPassword}
        onSubmit={() => {
          void submitAdminLogin()
        }}
      />
    )
  }

  return (
    <View style={styles.page}>
      <AdminDashboardView
        isWideDesktopLayout={isWideDesktopLayout}
        activeTab={activeTab}
        onChangeTab={setActiveTab}
        onReload={() => {
          void loadAll()
        }}
        onLogout={submitAdminLogout}
        loading={loading}
        noticeMessage={noticeMessage}
        errorMessage={errorMessage}
        isUploadingImages={isUploadingImages}
        currentUploadStage={currentUploadStage}
        countries={countries}
        destinations={destinations}
        members={members}
        publicHolidays={publicHolidays}
        storageCountryOptions={storageCountryOptions}
        selectedStorageCountryOption={selectedStorageCountryOption}
        destinationFormState={destinationFormState}
        setDestinationFormState={setDestinationFormState}
        uploadingDestinationImageCount={uploadingDestinationImageCount}
        uploadingExperienceIndexes={uploadingExperienceIndexes}
        memberFormState={memberFormState}
        setMemberFormState={setMemberFormState}
        holidayFormState={holidayFormState}
        setHolidayFormState={setHolidayFormState}
        destinationListSearchKeyword={destinationListSearchKeyword}
        setDestinationListSearchKeyword={setDestinationListSearchKeyword}
        filteredDestinations={filteredDestinations}
        memberListSearchKeyword={memberListSearchKeyword}
        setMemberListSearchKeyword={setMemberListSearchKeyword}
        filteredMembers={filteredMembers}
        holidayListSearchKeyword={holidayListSearchKeyword}
        setHolidayListSearchKeyword={setHolidayListSearchKeyword}
        filteredPublicHolidays={filteredPublicHolidays}
        countryFormState={countryFormState}
        setCountryFormState={setCountryFormState}
        countryListSearchKeyword={countryListSearchKeyword}
        setCountryListSearchKeyword={setCountryListSearchKeyword}
        filteredCountries={filteredCountries}
        handleImageFileSelection={handleImageFileSelection}
        handleRemoveDestinationImage={handleRemoveDestinationImage}
        addExperienceItem={addExperienceItem}
        moveExperienceItem={moveExperienceItem}
        removeExperienceItem={removeExperienceItem}
        handleExperienceThumbnailFileSelection={handleExperienceThumbnailFileSelection}
        onSubmitDestination={() => {
          void submitDestination()
        }}
        onResetDestinationForm={resetDestinationFormWithCleanup}
        onLoadDestination={loadDestinationToForm}
        onDeleteDestination={(destinationId) => {
          void deleteDestination(destinationId)
        }}
        onSubmitMember={() => {
          void submitMember()
        }}
        onDeleteMember={(memberId) => {
          void deleteMember(memberId)
        }}
        onSubmitHoliday={() => {
          void submitHoliday()
        }}
        onDeleteHoliday={(holidayId) => {
          void deleteHoliday(holidayId)
        }}
        onSubmitCountry={() => {
          void submitCountry()
        }}
        onDeleteCountry={(countryId) => {
          void deleteCountry(countryId)
        }}
        onLoadCountry={loadCountryToForm}
      />

      {cropSessionState != null && cropSessionItem != null && cropImageRenderMetrics != null && (
        <AdminCropModal
          cropSessionState={cropSessionState}
          cropSessionItem={cropSessionItem}
          cropImageRenderMetrics={cropImageRenderMetrics}
          primaryGuideRectInCropArea={primaryGuideRectInCropArea}
          secondaryGuideRectInCropArea={secondaryGuideRectInCropArea}
          onBeginMoveCropArea={beginMoveCropArea}
          onBeginResizeCropArea={beginResizeCropArea}
          onResetCropArea={resetCurrentCropArea}
          onClose={closeCurrentCropSession}
          onApply={() => {
            if (!cropSessionState.isProcessing) {
              void applyCurrentCrop()
            }
          }}
        />
      )}
    </View>
  )
}
