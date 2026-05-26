import React from "react"
import { ActivityIndicator, Pressable, ScrollView, Text, View } from "react-native"
import { Country, Destination } from "../../../types/admin"
import { htmlFieldStyle, htmlImagePreviewStyle, htmlImageUrlLinkStyle, styles } from "../styles/adminStyles"
import { DestinationFormState, StorageCountryOption } from "../types/adminLocalTypes"
import { ActionButton } from "./ActionButton"
import { LabelInput } from "./LabelInput"

type DestinationsSectionProps = {
  isWideDesktopLayout: boolean
  countries: Country[]
  storageCountryOptions: StorageCountryOption[]
  selectedStorageCountryOption: StorageCountryOption | undefined
  destinationFormState: DestinationFormState
  setDestinationFormState: React.Dispatch<React.SetStateAction<DestinationFormState>>
  parseFlightTimeMinutes: (value: string) => string
  handleImageFileSelection: (event: React.ChangeEvent<HTMLInputElement>) => void
  uploadingDestinationImageCount: number
  handleRemoveDestinationImage: (destinationImageIndex: number) => void
  addExperienceItem: () => void
  moveExperienceItem: (experienceIndex: number, direction: "up" | "down") => void
  removeExperienceItem: (experienceIndex: number) => void
  handleExperienceThumbnailFileSelection: (experienceIndex: number, event: React.ChangeEvent<HTMLInputElement>) => void
  uploadingExperienceIndexes: number[]
  onSubmitDestination: () => void
  onResetDestinationForm: () => void
  destinationListSearchKeyword: string
  setDestinationListSearchKeyword: (value: string) => void
  filteredDestinations: Destination[]
  onLoadDestination: (destination: Destination) => void
  onDeleteDestination: (destinationId: number) => void
}

export const DestinationsSection = ({
  isWideDesktopLayout,
  countries,
  storageCountryOptions,
  selectedStorageCountryOption,
  destinationFormState,
  setDestinationFormState,
  parseFlightTimeMinutes,
  handleImageFileSelection,
  uploadingDestinationImageCount,
  handleRemoveDestinationImage,
  addExperienceItem,
  moveExperienceItem,
  removeExperienceItem,
  handleExperienceThumbnailFileSelection,
  uploadingExperienceIndexes,
  onSubmitDestination,
  onResetDestinationForm,
  destinationListSearchKeyword,
  setDestinationListSearchKeyword,
  filteredDestinations,
  onLoadDestination,
  onDeleteDestination
}: DestinationsSectionProps): React.JSX.Element => {
  return (
    <View style={[styles.destinationsWorkspace, !isWideDesktopLayout && styles.destinationsWorkspaceStacked]}>
      <View style={styles.destinationEditorColumn}>
        <View style={styles.sectionCard}>
          <Text style={styles.sectionTitle}>장소 추가/수정</Text>
          <View style={styles.row}>
            <Text style={styles.fieldLabel}>국가</Text>
            <input
              list="admin-country-options"
              value={destinationFormState.countryName}
              onChange={(event) => {
                const typedCountryName = event.target.value
                const matchedCountry = countries.find((country) => country.name === typedCountryName.trim())
                setDestinationFormState((previousState) => ({
                  ...previousState,
                  countryName: typedCountryName,
                  countryId: matchedCountry == null ? "" : String(matchedCountry.id)
                }))
              }}
              style={htmlFieldStyle}
              placeholder="국가명 입력 또는 선택"
            />
            <datalist id="admin-country-options">
              {countries.map((country) => (
                <option value={country.name} key={country.id} />
              ))}
            </datalist>
          </View>

          <View style={styles.row}>
            <Text style={styles.fieldLabel}>Storage 국가 경로</Text>
            <input
              list="admin-storage-country-options"
              value={destinationFormState.storageCountrySlug}
              onChange={(event) => {
                const selectedValue = event.target.value.trim()
                const countryOption = storageCountryOptions.find((item) => item.slug === selectedValue)
                setDestinationFormState((previousState) => ({
                  ...previousState,
                  storageCountrySlug: selectedValue,
                  storageCitySlug: countryOption == null
                    ? previousState.storageCitySlug
                    : (countryOption.citySlugs[0] ?? previousState.storageCitySlug)
                }))
              }}
              style={htmlFieldStyle}
              placeholder="예: australia"
            />
            <datalist id="admin-storage-country-options">
              {storageCountryOptions.map((countryOption) => (
                <option value={countryOption.slug} key={countryOption.slug} />
              ))}
            </datalist>
          </View>

          <View style={styles.row}>
            <Text style={styles.fieldLabel}>Storage 도시 경로</Text>
            <input
              list="admin-storage-city-options"
              value={destinationFormState.storageCitySlug}
              onChange={(event) => {
                setDestinationFormState((previousState) => ({
                  ...previousState,
                  storageCitySlug: event.target.value.trim()
                }))
              }}
              style={htmlFieldStyle}
              placeholder="예: brisbane"
            />
            <datalist id="admin-storage-city-options">
              {(selectedStorageCountryOption?.citySlugs ?? []).map((citySlug) => (
                <option value={citySlug} key={citySlug} />
              ))}
            </datalist>
          </View>

          <LabelInput
            label="도시명"
            value={destinationFormState.name}
            onChangeText={(value) => setDestinationFormState((previousState) => ({ ...previousState, name: value }))}
          />
          <LabelInput
            label="요약 (25자 내외)"
            value={destinationFormState.summary}
            onChangeText={(value) => setDestinationFormState((previousState) => ({ ...previousState, summary: value }))}
          />
          <LabelInput
            label="설명 (150자 내외)"
            value={destinationFormState.description}
            multiline
            enableBoldFormatting
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
          <LabelInput
            label="항공권 링크(Skyscanner)"
            value={destinationFormState.flightUrl}
            onChangeText={(value) => setDestinationFormState((previousState) => ({ ...previousState, flightUrl: value }))}
          />

          <View style={styles.uploadRow}>
            <Text style={styles.fieldLabel}>사진 업로드</Text>
            <input type="file" accept="image/*" multiple onChange={handleImageFileSelection} />
            <Text style={styles.helperText}>권장되는 장소 이미지 규격은 100:87(파랑), 100:67(민트)입니다. 크롭 프레임을 참고해 이미지를 맞춰 업로드해주세요.</Text>
            {uploadingDestinationImageCount > 0 && (
              <View style={styles.inlineUploadingBadge}>
                <ActivityIndicator size="small" color="#2a6bd8" />
                <Text style={styles.inlineUploadingBadgeText}>장소 사진 {uploadingDestinationImageCount}건 업로드 중</Text>
              </View>
            )}
          </View>

          {destinationFormState.images.length > 0 && (
            <View style={styles.imageListContainer}>
              {destinationFormState.images.map((image, index) => (
                <View style={styles.imageRow} key={`${image.imageUrl}_${index}`}>
                  <View style={styles.imagePreviewContainer}>
                    <img
                      src={image.imageUrl}
                      alt={`destination-image-${index + 1}`}
                      style={htmlImagePreviewStyle}
                    />
                  </View>
                  <a href={image.imageUrl} target="_blank" rel="noreferrer" style={htmlImageUrlLinkStyle}>
                    {image.imageUrl}
                  </a>
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
                        handleRemoveDestinationImage(index)
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

          <View style={styles.row}>
            <Text style={styles.sectionTitle}>체험 관리 (Experience)</Text>
            <Text style={styles.helperText}>현재 {destinationFormState.experiences.length}건 · 순서 변경/제목/설명/링크/썸네일을 관리합니다.</Text>
          </View>

          {destinationFormState.experiences.length > 0 && (
            <View style={styles.imageListContainer}>
              {destinationFormState.experiences.map((experience, experienceIndex) => (
                <View style={styles.imageRow} key={`experience_${experienceIndex}`}>
                  <Text style={styles.fieldLabel}>체험 {experienceIndex + 1}</Text>
                  <LabelInput
                    label="제목"
                    value={experience.title}
                    onChangeText={(value) => {
                      setDestinationFormState((previousState) => ({
                        ...previousState,
                        experiences: previousState.experiences.map((targetExperience, targetIndex) => {
                          if (targetIndex !== experienceIndex) {
                            return targetExperience
                          }
                          return { ...targetExperience, title: value }
                        })
                      }))
                    }}
                  />
                  <LabelInput
                    label="설명 (40자 내외)"
                    value={experience.description}
                    multiline
                    onChangeText={(value) => {
                      setDestinationFormState((previousState) => ({
                        ...previousState,
                        experiences: previousState.experiences.map((targetExperience, targetIndex) => {
                          if (targetIndex !== experienceIndex) {
                            return targetExperience
                          }
                          return { ...targetExperience, description: value }
                        })
                      }))
                    }}
                  />
                  <LabelInput
                    label="링크 URL"
                    value={experience.link}
                    onChangeText={(value) => {
                      setDestinationFormState((previousState) => ({
                        ...previousState,
                        experiences: previousState.experiences.map((targetExperience, targetIndex) => {
                          if (targetIndex !== experienceIndex) {
                            return targetExperience
                          }
                          return { ...targetExperience, link: value }
                        })
                      }))
                    }}
                  />
                  <View style={styles.uploadRow}>
                    <Text style={styles.fieldLabel}>썸네일 업로드</Text>
                    <input
                      type="file"
                      accept="image/*"
                      onChange={(event) => {
                        handleExperienceThumbnailFileSelection(experienceIndex, event)
                      }}
                    />
                    {experience.thumbnailUrl.length > 0 && (
                      <View style={styles.imagePreviewContainer}>
                        <img
                          src={experience.thumbnailUrl}
                          alt={`experience-thumbnail-${experienceIndex + 1}`}
                          style={htmlImagePreviewStyle}
                        />
                      </View>
                    )}
                    {experience.thumbnailUrl.length > 0 && (
                      <View>
                        <a href={experience.thumbnailUrl} target="_blank" rel="noreferrer" style={htmlImageUrlLinkStyle}>
                          {experience.thumbnailUrl}
                        </a>
                      </View>
                    )}
                    <Text style={styles.helperText}>권장되는 체험 썸네일 규격은 100:60입니다. 크롭 프레임을 참고해 이미지를 맞춰 업로드해주세요.</Text>
                    {uploadingExperienceIndexes.includes(experienceIndex) && (
                      <View style={styles.inlineUploadingBadge}>
                        <ActivityIndicator size="small" color="#2a6bd8" />
                        <Text style={styles.inlineUploadingBadgeText}>체험 썸네일 업로드 중</Text>
                      </View>
                    )}
                  </View>
                  <View style={styles.rowButtonContainer}>
                    <ActionButton label="위로" variant="secondary" onPress={() => moveExperienceItem(experienceIndex, "up")} />
                    <ActionButton label="아래로" variant="secondary" onPress={() => moveExperienceItem(experienceIndex, "down")} />
                    <ActionButton label="체험 삭제" variant="danger" onPress={() => removeExperienceItem(experienceIndex)} />
                  </View>
                </View>
              ))}
            </View>
          )}

          <View style={styles.rowButtonContainer}>
            <ActionButton label="체험 추가" variant="secondary" onPress={addExperienceItem} />
          </View>

          <View style={styles.rowButtonContainer}>
            <ActionButton label={destinationFormState.selectedId == null ? "장소 생성" : "장소 수정"} onPress={onSubmitDestination} />
            <ActionButton label="폼 초기화" variant="secondary" onPress={onResetDestinationForm} />
          </View>
        </View>
      </View>

      <View style={[styles.destinationListColumn, !isWideDesktopLayout && styles.destinationListColumnStacked]}>
        <View style={[styles.sectionCard, styles.destinationListPanel]}>
          <Text style={styles.sectionTitle}>장소 목록</Text>
          <Text style={styles.helperText}>오른쪽 목록에서 선택하면 왼쪽 폼으로 즉시 불러옵니다.</Text>
          <input
            value={destinationListSearchKeyword}
            onChange={(event) => setDestinationListSearchKeyword(event.target.value)}
            style={htmlFieldStyle}
            placeholder="국가/도시/요약 검색"
          />
          <ScrollView style={styles.destinationListScrollArea} contentContainerStyle={styles.destinationListContainer}>
            {filteredDestinations.map((destination) => (
              <View style={styles.listItemCard} key={destination.id}>
                <Text style={styles.listItemTitle}>{destination.countryName} · {destination.name}</Text>
                <Text style={styles.listItemDescription}>{destination.summary ?? "(요약 없음)"}</Text>
                <Text style={styles.listItemDescription}>이미지 {destination.images.length}장</Text>
                <Text style={styles.listItemDescription}>체험 {destination.experiences.length}건</Text>
                <View style={styles.rowButtonContainer}>
                  <ActionButton label="불러오기" variant="secondary" onPress={() => onLoadDestination(destination)} />
                  <ActionButton label="삭제" variant="danger" onPress={() => onDeleteDestination(destination.id)} />
                </View>
              </View>
            ))}
            {filteredDestinations.length === 0 && (
              <Text style={styles.helperText}>검색 결과가 없습니다.</Text>
            )}
          </ScrollView>
        </View>
      </View>
    </View>
  )
}
