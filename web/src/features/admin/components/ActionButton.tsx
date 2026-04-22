import React from "react"
import { Pressable, Text } from "react-native"
import { styles } from "../styles/adminStyles"

export type ActionButtonVariant = "primary" | "secondary" | "danger"

type ActionButtonProps = {
  label: string
  onPress: () => void
  variant?: ActionButtonVariant
}

export const ActionButton = ({ label, onPress, variant = "primary" }: ActionButtonProps): React.JSX.Element => {
  const style = variant === "danger"
    ? styles.actionButtonDanger
    : variant === "secondary"
      ? styles.actionButtonSecondary
      : styles.actionButtonPrimary

  return (
    <Pressable onPress={onPress} style={style}>
      <Text style={styles.actionButtonText}>{label}</Text>
    </Pressable>
  )
}
