import React from "react"
import { ScrollView, Text, View } from "react-native"
import { Member } from "../../../types/admin"
import { htmlFieldStyle, styles } from "../styles/adminStyles"
import { MemberFormState } from "../types/adminLocalTypes"
import { ActionButton } from "./ActionButton"
import { LabelInput } from "./LabelInput"

type MembersSectionProps = {
  isWideDesktopLayout: boolean
  memberFormState: MemberFormState
  setMemberFormState: React.Dispatch<React.SetStateAction<MemberFormState>>
  filteredMembers: Member[]
  memberListSearchKeyword: string
  setMemberListSearchKeyword: (value: string) => void
  onSubmitMember: () => void
  onDeleteMember: (memberId: string) => void
  onResetMemberForm: () => void
}

export const MembersSection = ({
  isWideDesktopLayout,
  memberFormState,
  setMemberFormState,
  filteredMembers,
  memberListSearchKeyword,
  setMemberListSearchKeyword,
  onSubmitMember,
  onDeleteMember,
  onResetMemberForm
}: MembersSectionProps): React.JSX.Element => {
  return (
    <View style={[styles.destinationsWorkspace, !isWideDesktopLayout && styles.destinationsWorkspaceStacked]}>
      <View style={styles.destinationEditorColumn}>
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
            <ActionButton label={memberFormState.selectedId == null ? "사용자 생성" : "사용자 수정"} onPress={onSubmitMember} />
            <ActionButton label="폼 초기화" variant="secondary" onPress={onResetMemberForm} />
          </View>
        </View>
      </View>

      <View style={[styles.destinationListColumn, !isWideDesktopLayout && styles.destinationListColumnStacked]}>
        <View style={[styles.sectionCard, styles.destinationListPanel]}>
          <Text style={styles.sectionTitle}>사용자 목록</Text>
          <Text style={styles.helperText}>오른쪽 목록에서 선택한 회원 정보를 즉시 불러옵니다.</Text>
          <input
            value={memberListSearchKeyword}
            onChange={(event) => setMemberListSearchKeyword(event.target.value)}
            style={htmlFieldStyle}
            placeholder="ID/닉네임 검색"
          />
          <ScrollView style={styles.destinationListScrollArea} contentContainerStyle={styles.destinationListContainer}>
            {filteredMembers.map((member) => (
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
                  <ActionButton label="삭제" variant="danger" onPress={() => onDeleteMember(member.id)} />
                </View>
              </View>
            ))}
            {filteredMembers.length === 0 && (
              <Text style={styles.helperText}>검색 결과가 없습니다.</Text>
            )}
          </ScrollView>
        </View>
      </View>
    </View>
  )
}
