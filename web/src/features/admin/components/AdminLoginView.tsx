import React from "react"
import { Text, View } from "react-native"
import { styles } from "../styles/adminStyles"
import { ActionButton } from "./ActionButton"
import { LabelInput } from "./LabelInput"

type AdminLoginViewProps = {
  windowHeight: number
  loginUsername: string
  loginPassword: string
  loginLoading: boolean
  errorMessage: string
  onChangeUsername: (value: string) => void
  onChangePassword: (value: string) => void
  onSubmit: () => void
}

export const AdminLoginView = ({
  windowHeight,
  loginUsername,
  loginPassword,
  loginLoading,
  errorMessage,
  onChangeUsername,
  onChangePassword,
  onSubmit
}: AdminLoginViewProps): React.JSX.Element => {
  return (
    <View style={[styles.loginPage, { minHeight: windowHeight }]}>
      <View style={styles.loginCard}>
        <Text style={styles.loginTitle}>Oguri Admin Login</Text>
        <Text style={styles.loginDescription}>관리자 계정으로 로그인 후 DB 관리 기능을 사용할 수 있습니다.</Text>
        <LabelInput label="아이디" value={loginUsername} onChangeText={onChangeUsername} />
        <LabelInput label="비밀번호" value={loginPassword} onChangeText={onChangePassword} secureTextEntry />
        {errorMessage.length > 0 && <Text style={styles.errorText}>{errorMessage}</Text>}
        <ActionButton
          label={loginLoading ? "로그인 중..." : "로그인"}
          onPress={() => {
            if (!loginLoading) {
              onSubmit()
            }
          }}
        />
      </View>
    </View>
  )
}
