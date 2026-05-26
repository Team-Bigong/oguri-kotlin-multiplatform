import React from "react"
import { StyleSheet } from "react-native"

export const styles = StyleSheet.create({
  loginPage: {
    minHeight: "100%",
    backgroundColor: "#edf4ff",
    padding: 20,
    justifyContent: "center",
    alignItems: "center"
  },
  loginCard: {
    width: "100%",
    maxWidth: 420,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: "#d0ddd0",
    backgroundColor: "#ffffff",
    padding: 16,
    gap: 12
  },
  loginTitle: {
    color: "#1d2e21",
    fontSize: 24,
    fontWeight: "700"
  },
  loginDescription: {
    color: "#4f6654",
    fontSize: 14
  },
  page: {
    minHeight: "100%",
    backgroundColor: "#e9f2ff",
    paddingHorizontal: 18,
    paddingVertical: 18
  },
  adminLayout: {
    flexDirection: "row",
    gap: 16,
    minHeight: "100%"
  },
  sidebarContainer: {
    width: 248,
    borderRadius: 18,
    borderWidth: 1,
    borderColor: "#b9cff3",
    backgroundColor: "#10243f",
    paddingHorizontal: 14,
    paddingTop: 18,
    paddingBottom: 16,
    gap: 14
  },
  sidebarBrandTitle: {
    color: "#f5faff",
    fontSize: 22,
    fontWeight: "800"
  },
  sidebarBrandDescription: {
    color: "#b9d0ef",
    fontSize: 13,
    lineHeight: 19
  },
  sidebarTabList: {
    gap: 8
  },
  mainPanel: {
    flex: 1,
    minWidth: 0
  },
  headerContainer: {
    marginBottom: 12,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: "#bfcef0",
    backgroundColor: "#ffffff",
    paddingHorizontal: 16,
    paddingVertical: 14,
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center"
  },
  headerActionRow: {
    flexDirection: "row",
    gap: 8
  },
  headline: {
    color: "#14356a",
    fontSize: 26,
    fontWeight: "800"
  },
  subtitle: {
    color: "#55739e",
    fontSize: 15,
    marginTop: 6,
    fontWeight: "600"
  },
  metricCardContainer: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: 10,
    marginBottom: 12
  },
  metricCard: {
    minWidth: 150,
    flexGrow: 1,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: "#c7d8f4",
    backgroundColor: "#ffffff",
    paddingHorizontal: 12,
    paddingVertical: 11,
    gap: 4
  },
  metricCardAccent: {
    width: 34,
    height: 4,
    borderRadius: 999
  },
  metricCardLabel: {
    color: "#6683ad",
    fontSize: 13,
    fontWeight: "600"
  },
  metricCardValue: {
    color: "#13386b",
    fontSize: 22,
    fontWeight: "800"
  },
  tabButtonDefault: {
    width: "100%",
    paddingHorizontal: 12,
    paddingVertical: 11,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: "#2b4669",
    backgroundColor: "#172f52"
  },
  tabButtonActive: {
    width: "100%",
    paddingHorizontal: 12,
    paddingVertical: 11,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: "#2c8af7",
    backgroundColor: "#164f93"
  },
  tabTextDefault: {
    color: "#d1e3fb",
    fontWeight: "700",
    fontSize: 14
  },
  tabTextActive: {
    color: "#f7fbff",
    fontWeight: "800",
    fontSize: 14
  },
  noticeText: {
    color: "#1a6a3e",
    marginBottom: 10,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: "#a6d6ba",
    backgroundColor: "#effaf2",
    paddingHorizontal: 10,
    paddingVertical: 8
  },
  errorText: {
    color: "#af2121",
    marginBottom: 10,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: "#e4b5b5",
    backgroundColor: "#fff4f4",
    paddingHorizontal: 10,
    paddingVertical: 8
  },
  uploadStatusCard: {
    marginBottom: 10,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: "#b7d0f5",
    backgroundColor: "#f3f8ff",
    paddingHorizontal: 10,
    paddingVertical: 10,
    flexDirection: "row",
    alignItems: "center",
    gap: 8
  },
  uploadStatusTextContainer: {
    gap: 3
  },
  uploadStatusTitle: {
    color: "#1f4f8a",
    fontSize: 14,
    fontWeight: "700"
  },
  uploadStatusDescription: {
    color: "#4f76a8",
    fontSize: 12
  },
  loadingContainer: {
    minHeight: 300,
    justifyContent: "center",
    alignItems: "center"
  },
  contentContainer: {
    flex: 1,
    minHeight: 0
  },
  contentInnerContainer: {
    paddingBottom: 120
  },
  destinationsWorkspace: {
    flexDirection: "row",
    alignItems: "flex-start",
    gap: 12
  },
  destinationsWorkspaceStacked: {
    flexDirection: "column"
  },
  destinationEditorColumn: {
    flex: 1,
    minWidth: 0
  },
  destinationListColumn: {
    width: 380,
    flexShrink: 0
  },
  destinationListColumnStacked: {
    width: "100%"
  },
  destinationListPanel: {
    gap: 10,
    maxHeight: 760,
    overflow: "hidden"
  },
  destinationListScrollArea: {
    flexGrow: 0
  },
  destinationListContainer: {
    gap: 10,
    paddingBottom: 4
  },
  sectionCard: {
    borderRadius: 18,
    borderWidth: 1,
    borderColor: "#c1d1ec",
    backgroundColor: "#ffffff",
    padding: 18,
    gap: 12
  },
  sectionTitle: {
    color: "#163b70",
    fontSize: 20,
    fontWeight: "800",
    marginTop: 4
  },
  row: {
    gap: 7
  },
  rowSplitContainer: {
    flexDirection: "row",
    gap: 12
  },
  fieldLabel: {
    color: "#305787",
    fontSize: 14,
    fontWeight: "700"
  },
  fieldLabelRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    gap: 8
  },
  formatButton: {
    borderWidth: 1,
    borderColor: "#b9ccef",
    borderRadius: 8,
    backgroundColor: "#f3f8ff",
    paddingHorizontal: 8,
    paddingVertical: 4
  },
  formatButtonText: {
    color: "#2a5b97",
    fontSize: 12,
    fontWeight: "700"
  },
  textInput: {
    borderWidth: 1,
    borderColor: "#bfd1ef",
    borderRadius: 10,
    backgroundColor: "#f8fbff",
    minHeight: 44,
    paddingHorizontal: 10,
    paddingVertical: 8
  },
  textInputMultiline: {
    borderWidth: 1,
    borderColor: "#bfd1ef",
    borderRadius: 10,
    backgroundColor: "#f8fbff",
    minHeight: 94,
    paddingHorizontal: 10,
    paddingVertical: 8,
    textAlignVertical: "top"
  },
  helperText: {
    color: "#5e7fa7"
  },
  uploadRow: {
    gap: 8
  },
  inlineUploadingBadge: {
    flexDirection: "row",
    alignItems: "center",
    gap: 6,
    borderRadius: 999,
    borderWidth: 1,
    borderColor: "#b7d0f5",
    backgroundColor: "#f4f8ff",
    paddingHorizontal: 10,
    paddingVertical: 6,
    alignSelf: "flex-start"
  },
  inlineUploadingBadgeText: {
    color: "#2a5e9f",
    fontSize: 12,
    fontWeight: "600"
  },
  imageListContainer: {
    gap: 8
  },
  imagePreviewContainer: {
    width: 160,
    height: 108,
    borderRadius: 8,
    overflow: "hidden",
    borderWidth: 1,
    borderColor: "#bfd1ef",
    backgroundColor: "#e9f2ff"
  },
  imageRow: {
    borderWidth: 1,
    borderColor: "#cfddf3",
    borderRadius: 10,
    padding: 8,
    gap: 8,
    backgroundColor: "#f6faff"
  },
  imageUrlText: {
    color: "#48678f",
    fontSize: 12
  },
  imageRowControls: {
    flexDirection: "row",
    gap: 8
  },
  badgePrimary: {
    backgroundColor: "#1d6bce",
    borderRadius: 8,
    paddingHorizontal: 10,
    paddingVertical: 6
  },
  badgeDefault: {
    backgroundColor: "#6e8db9",
    borderRadius: 8,
    paddingHorizontal: 10,
    paddingVertical: 6
  },
  badgeDanger: {
    backgroundColor: "#ad4040",
    borderRadius: 8,
    paddingHorizontal: 10,
    paddingVertical: 6
  },
  badgeText: {
    color: "#ffffff",
    fontSize: 12,
    fontWeight: "600"
  },
  rowButtonContainer: {
    flexDirection: "row",
    gap: 8,
    flexWrap: "wrap"
  },
  actionButtonPrimary: {
    borderRadius: 10,
    backgroundColor: "#1e6fd6",
    paddingHorizontal: 14,
    paddingVertical: 10
  },
  actionButtonSecondary: {
    borderRadius: 10,
    backgroundColor: "#5f7ca7",
    paddingHorizontal: 14,
    paddingVertical: 10
  },
  actionButtonDanger: {
    borderRadius: 10,
    backgroundColor: "#ad4040",
    paddingHorizontal: 14,
    paddingVertical: 10
  },
  actionButtonText: {
    color: "#ffffff",
    fontSize: 13,
    fontWeight: "700"
  },
  listItemCard: {
    borderWidth: 1,
    borderColor: "#cedcf2",
    borderRadius: 12,
    padding: 12,
    gap: 6,
    backgroundColor: "#f8fbff"
  },
  listItemTitle: {
    color: "#173f74",
    fontSize: 15,
    fontWeight: "800"
  },
  listItemDescription: {
    color: "#5a7fa7",
    fontSize: 13
  },
  cropModalBackdrop: {
    position: "absolute",
    top: 0,
    right: 0,
    bottom: 0,
    left: 0,
    backgroundColor: "rgba(6, 17, 30, 0.58)",
    justifyContent: "center",
    alignItems: "center",
    paddingHorizontal: 14,
    paddingVertical: 20
  },
  cropModalCard: {
    width: "100%",
    maxWidth: 860,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: "#bad0ef",
    backgroundColor: "#ffffff",
    padding: 16,
    gap: 10
  },
  cropModalTitle: {
    color: "#173e74",
    fontSize: 20,
    fontWeight: "800"
  }
})

export const htmlFieldStyle: React.CSSProperties = {
  minHeight: 40,
  borderColor: "#bfd1ef",
  borderRadius: 10,
  borderWidth: 1,
  padding: 8,
  backgroundColor: "#f8fbff"
}

export const htmlTextAreaFieldStyle: React.CSSProperties = {
  minHeight: 94,
  borderColor: "#bfd1ef",
  borderRadius: 10,
  borderWidth: 1,
  padding: 8,
  backgroundColor: "#f8fbff",
  resize: "vertical",
  fontFamily: "inherit",
  fontSize: 14
}

export const htmlImagePreviewStyle: React.CSSProperties = {
  width: "100%",
  height: "100%",
  objectFit: "cover",
  display: "block"
}

export const htmlCropStageStyle: React.CSSProperties = {
  position: "relative",
  alignSelf: "center",
  borderRadius: 10,
  overflow: "hidden",
  border: "1px solid #8fb4e9",
  backgroundColor: "#091625",
  userSelect: "none",
  touchAction: "none"
}

export const htmlCropBaseImageStyle: React.CSSProperties = {
  width: "100%",
  height: "100%",
  objectFit: "cover",
  display: "block"
}

export const htmlCropOutsideTopStyle: React.CSSProperties = {
  position: "absolute",
  top: 0,
  left: 0,
  right: 0,
  backgroundColor: "rgba(8, 24, 44, 0.28)",
  pointerEvents: "none"
}

export const htmlCropOutsideBottomStyle: React.CSSProperties = {
  position: "absolute",
  left: 0,
  right: 0,
  bottom: 0,
  backgroundColor: "rgba(8, 24, 44, 0.28)",
  pointerEvents: "none"
}

export const htmlCropOutsideLeftStyle: React.CSSProperties = {
  position: "absolute",
  left: 0,
  backgroundColor: "rgba(8, 24, 44, 0.28)",
  pointerEvents: "none"
}

export const htmlCropOutsideRightStyle: React.CSSProperties = {
  position: "absolute",
  right: 0,
  backgroundColor: "rgba(8, 24, 44, 0.28)",
  pointerEvents: "none"
}

export const htmlPrimaryCropAreaStyle: React.CSSProperties = {
  position: "absolute",
  border: "2px solid #4fa4ff",
  boxSizing: "border-box",
  overflow: "hidden",
  cursor: "move",
  zIndex: 3
}

export const htmlReferenceGuideStyle: React.CSSProperties = {
  position: "absolute",
  border: "2px solid #59a7ff",
  boxSizing: "border-box",
  pointerEvents: "none",
  zIndex: 2
}

export const htmlSecondaryCropGuideStyle: React.CSSProperties = {
  position: "absolute",
  border: "2px solid #00c8b5",
  boxSizing: "border-box",
  pointerEvents: "none",
  zIndex: 2
}

export const htmlCropResizeHandleStyle: React.CSSProperties = {
  position: "absolute",
  right: -8,
  bottom: -8,
  width: 16,
  height: 16,
  borderRadius: 999,
  backgroundColor: "#00c8b5",
  border: "2px solid #ffffff",
  cursor: "nwse-resize"
}

export const htmlImageUrlLinkStyle: React.CSSProperties = {
  color: "#48678f",
  fontSize: "12px",
  textDecoration: "underline",
  wordBreak: "break-all"
}
