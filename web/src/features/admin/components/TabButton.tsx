import React from "react"
import { Pressable, Text } from "react-native"
import { styles } from "../styles/adminStyles"

type TabButtonProps = {
  label: string
  selected: boolean
  onPress: () => void
}

export const TabButton = ({ label, selected, onPress }: TabButtonProps): React.JSX.Element => {
  return (
    <Pressable onPress={onPress} style={selected ? styles.tabButtonActive : styles.tabButtonDefault}>
      <Text style={selected ? styles.tabTextActive : styles.tabTextDefault}>{label}</Text>
    </Pressable>
  )
}
