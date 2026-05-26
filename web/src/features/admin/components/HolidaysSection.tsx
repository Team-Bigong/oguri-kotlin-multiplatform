import React from "react"
import { ScrollView, Text, View } from "react-native"
import { PublicHoliday } from "../../../types/admin"
import { htmlFieldStyle, styles } from "../styles/adminStyles"
import { HolidayFormState } from "../types/adminLocalTypes"
import { ActionButton } from "./ActionButton"
import { LabelInput } from "./LabelInput"

type HolidaysSectionProps = {
  isWideDesktopLayout: boolean
  holidayFormState: HolidayFormState
  setHolidayFormState: React.Dispatch<React.SetStateAction<HolidayFormState>>
  filteredPublicHolidays: PublicHoliday[]
  holidayListSearchKeyword: string
  setHolidayListSearchKeyword: (value: string) => void
  onSubmitHoliday: () => void
  onDeleteHoliday: (holidayId: number) => void
  onResetHolidayForm: () => void
}

export const HolidaysSection = ({
  isWideDesktopLayout,
  holidayFormState,
  setHolidayFormState,
  filteredPublicHolidays,
  holidayListSearchKeyword,
  setHolidayListSearchKeyword,
  onSubmitHoliday,
  onDeleteHoliday,
  onResetHolidayForm
}: HolidaysSectionProps): React.JSX.Element => {
  return (
    <View style={[styles.destinationsWorkspace, !isWideDesktopLayout && styles.destinationsWorkspaceStacked]}>
      <View style={styles.destinationEditorColumn}>
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
            <ActionButton label={holidayFormState.selectedId == null ? "공휴일 생성" : "공휴일 수정"} onPress={onSubmitHoliday} />
            <ActionButton label="폼 초기화" variant="secondary" onPress={onResetHolidayForm} />
          </View>
        </View>
      </View>

      <View style={[styles.destinationListColumn, !isWideDesktopLayout && styles.destinationListColumnStacked]}>
        <View style={[styles.sectionCard, styles.destinationListPanel]}>
          <Text style={styles.sectionTitle}>공휴일 목록</Text>
          <Text style={styles.helperText}>오른쪽 목록에서 선택한 공휴일을 즉시 불러옵니다.</Text>
          <input
            value={holidayListSearchKeyword}
            onChange={(event) => setHolidayListSearchKeyword(event.target.value)}
            style={htmlFieldStyle}
            placeholder="날짜/이름 검색"
          />
          <ScrollView style={styles.destinationListScrollArea} contentContainerStyle={styles.destinationListContainer}>
            {filteredPublicHolidays.map((holiday) => (
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
                        isActualHoliday: Boolean(holiday.isActualHoliday)
                      })
                    }}
                  />
                  <ActionButton label="삭제" variant="danger" onPress={() => onDeleteHoliday(holiday.id)} />
                </View>
              </View>
            ))}
            {filteredPublicHolidays.length === 0 && (
              <Text style={styles.helperText}>검색 결과가 없습니다.</Text>
            )}
          </ScrollView>
        </View>
      </View>
    </View>
  )
}
