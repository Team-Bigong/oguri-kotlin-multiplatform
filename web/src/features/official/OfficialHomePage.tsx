import React from "react"
import { Pressable, StyleSheet, Text, View } from "react-native"

const OFFICIAL_WEB_TITLE = "오구리 공식 웹"

export const OfficialHomePage = (): React.JSX.Element => {
  return (
    <View style={styles.page}>
      <View style={styles.heroCard}>
        <Text style={styles.label}>OGURI</Text>
        <Text style={styles.title}>{OFFICIAL_WEB_TITLE}</Text>
        <Text style={styles.description}>
          React Native 기반 공식 웹 시작 지점입니다. 브랜드/콘텐츠 섹션은 여기서 확장하면 됩니다.
        </Text>
        <Pressable
          accessibilityRole="link"
          onPress={() => {
            window.location.hash = "/admin"
          }}
          style={styles.primaryButton}
        >
          <Text style={styles.primaryButtonText}>어드민으로 이동</Text>
        </Pressable>
      </View>
    </View>
  )
}

const styles = StyleSheet.create({
  page: {
    minHeight: "100%",
    backgroundColor: "#f6f7f2",
    alignItems: "center",
    justifyContent: "center",
    padding: 24
  },
  heroCard: {
    width: "100%",
    maxWidth: 760,
    borderRadius: 24,
    borderWidth: 1,
    borderColor: "#d9dfd0",
    backgroundColor: "#ffffff",
    padding: 32,
    gap: 12,
    boxShadow: "0 12px 30px rgba(43, 62, 46, 0.08)"
  },
  label: {
    color: "#395239",
    fontSize: 13,
    fontWeight: "700",
    letterSpacing: 1.2
  },
  title: {
    color: "#1f2f1f",
    fontSize: 34,
    fontWeight: "700"
  },
  description: {
    color: "#4b5f4b",
    fontSize: 16,
    lineHeight: 24
  },
  primaryButton: {
    marginTop: 10,
    alignSelf: "flex-start",
    borderRadius: 12,
    paddingHorizontal: 18,
    paddingVertical: 11,
    backgroundColor: "#2f5d3f"
  },
  primaryButtonText: {
    color: "#ffffff",
    fontSize: 14,
    fontWeight: "700"
  }
})
