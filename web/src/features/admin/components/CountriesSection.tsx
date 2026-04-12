import React from "react"
import { ScrollView, Text, View } from "react-native"
import { Country } from "../../../types/admin"
import { htmlFieldStyle, styles } from "../styles/adminStyles"
import { CountryFormState } from "../types/adminLocalTypes"
import { ActionButton } from "./ActionButton"
import { LabelInput } from "./LabelInput"

type CountriesSectionProps = {
  isWideDesktopLayout: boolean
  countryFormState: CountryFormState
  setCountryFormState: React.Dispatch<React.SetStateAction<CountryFormState>>
  filteredCountries: Country[]
  countryListSearchKeyword: string
  setCountryListSearchKeyword: React.Dispatch<React.SetStateAction<string>>
  onSubmitCountry: () => void
  onResetCountryForm: () => void
  onLoadCountry: (country: Country) => void
  onDeleteCountry: (countryId: number) => void
}

export const CountriesSection = ({
  isWideDesktopLayout,
  countryFormState,
  setCountryFormState,
  filteredCountries,
  countryListSearchKeyword,
  setCountryListSearchKeyword,
  onSubmitCountry,
  onResetCountryForm,
  onLoadCountry,
  onDeleteCountry
}: CountriesSectionProps): React.JSX.Element => {
  return (
    <View style={[styles.destinationsWorkspace, !isWideDesktopLayout && styles.destinationsWorkspaceStacked]}>
      <View style={styles.destinationEditorColumn}>
        <View style={styles.sectionCard}>
          <Text style={styles.sectionTitle}>나라 추가/수정</Text>
          <LabelInput
            label="국가명"
            value={countryFormState.name}
            onChangeText={(value) => setCountryFormState((previousState) => ({ ...previousState, name: value }))}
          />
          <LabelInput
            label="통화 코드 (예: USD)"
            value={countryFormState.currencyCode}
            onChangeText={(value) => setCountryFormState((previousState) => ({ ...previousState, currencyCode: value.toUpperCase() }))}
          />
          <LabelInput
            label="빅맥지수"
            value={countryFormState.bigMacIndex}
            keyboardType="numeric"
            onChangeText={(value) => setCountryFormState((previousState) => ({ ...previousState, bigMacIndex: value }))}
          />

          <View style={styles.rowButtonContainer}>
            <ActionButton label={countryFormState.selectedId == null ? "나라 생성" : "나라 수정"} onPress={onSubmitCountry} />
            <ActionButton label="폼 초기화" variant="secondary" onPress={onResetCountryForm} />
          </View>
        </View>
      </View>

      <View style={[styles.destinationListColumn, !isWideDesktopLayout && styles.destinationListColumnStacked]}>
        <View style={[styles.sectionCard, styles.destinationListPanel]}>
          <Text style={styles.sectionTitle}>나라 목록</Text>
          <Text style={styles.helperText}>국가명, 통화 코드, 빅맥지수를 함께 관리합니다.</Text>
          <input
            value={countryListSearchKeyword}
            onChange={(event) => setCountryListSearchKeyword(event.target.value)}
            style={htmlFieldStyle}
            placeholder="국가명/통화 코드 검색"
          />
          <ScrollView style={styles.destinationListScrollArea} contentContainerStyle={styles.destinationListContainer}>
            {filteredCountries.map((country) => (
              <View style={styles.listItemCard} key={country.id}>
                <Text style={styles.listItemTitle}>{country.name}</Text>
                <Text style={styles.listItemDescription}>통화 코드: {country.currencyCode ?? "-"}</Text>
                <Text style={styles.listItemDescription}>빅맥지수: {country.bigMacIndex == null ? "-" : country.bigMacIndex}</Text>
                <View style={styles.rowButtonContainer}>
                  <ActionButton label="불러오기" variant="secondary" onPress={() => onLoadCountry(country)} />
                  <ActionButton label="삭제" variant="danger" onPress={() => onDeleteCountry(country.id)} />
                </View>
              </View>
            ))}
            {filteredCountries.length === 0 && (
              <Text style={styles.helperText}>검색 결과가 없습니다.</Text>
            )}
          </ScrollView>
        </View>
      </View>
    </View>
  )
}
