import React from "react"
import { Text, View } from "react-native"
import {
  htmlCropBaseImageStyle,
  htmlCropOutsideBottomStyle,
  htmlCropOutsideLeftStyle,
  htmlCropOutsideRightStyle,
  htmlCropOutsideTopStyle,
  htmlCropResizeHandleStyle,
  htmlCropStageStyle,
  htmlPrimaryCropAreaStyle,
  htmlReferenceGuideStyle,
  htmlSecondaryCropGuideStyle,
  styles
} from "../styles/adminStyles"
import { CropArea, CropImageRenderMetrics, CropQueueItem, CropSessionState } from "../types/adminLocalTypes"
import { ActionButton } from "./ActionButton"

type AdminCropModalProps = {
  cropSessionState: CropSessionState
  cropSessionItem: CropQueueItem
  cropImageRenderMetrics: CropImageRenderMetrics
  primaryGuideRectInCropArea: CropArea | null
  secondaryGuideRectInCropArea: CropArea | null
  onBeginMoveCropArea: (event: React.MouseEvent<HTMLDivElement>) => void
  onBeginResizeCropArea: (event: React.MouseEvent<HTMLDivElement>) => void
  onResetCropArea: () => void
  onClose: () => void
  onApply: () => void
}

export const AdminCropModal = ({
  cropSessionState,
  cropSessionItem,
  cropImageRenderMetrics,
  primaryGuideRectInCropArea,
  secondaryGuideRectInCropArea,
  onBeginMoveCropArea,
  onBeginResizeCropArea,
  onResetCropArea,
  onClose,
  onApply
}: AdminCropModalProps): React.JSX.Element => {
  return (
    <View style={styles.cropModalBackdrop}>
      <View style={styles.cropModalCard}>
        <Text style={styles.cropModalTitle}>
          {cropSessionState.targetType === "destination" ? "장소 사진 크롭" : "체험 썸네일 크롭"}
        </Text>
        <Text style={styles.helperText}>
          {cropSessionState.currentIndex + 1} / {cropSessionState.queueItems.length} · 사각형 내부만 저장됩니다.
        </Text>
        <div
          style={{
            ...htmlCropStageStyle,
            width: cropImageRenderMetrics.displayWidth,
            height: cropImageRenderMetrics.displayHeight
          }}
        >
          <img
            src={cropSessionItem.previewUrl}
            alt="crop-preview"
            draggable={false}
            style={htmlCropBaseImageStyle}
          />
          <div
            style={{
              ...htmlCropOutsideTopStyle,
              height: cropSessionState.cropArea.y * cropImageRenderMetrics.displayScale
            }}
          />
          <div
            style={{
              ...htmlCropOutsideBottomStyle,
              top: (cropSessionState.cropArea.y + cropSessionState.cropArea.height) * cropImageRenderMetrics.displayScale
            }}
          />
          <div
            style={{
              ...htmlCropOutsideLeftStyle,
              top: cropSessionState.cropArea.y * cropImageRenderMetrics.displayScale,
              height: cropSessionState.cropArea.height * cropImageRenderMetrics.displayScale,
              width: cropSessionState.cropArea.x * cropImageRenderMetrics.displayScale
            }}
          />
          <div
            style={{
              ...htmlCropOutsideRightStyle,
              top: cropSessionState.cropArea.y * cropImageRenderMetrics.displayScale,
              left: (cropSessionState.cropArea.x + cropSessionState.cropArea.width) * cropImageRenderMetrics.displayScale,
              height: cropSessionState.cropArea.height * cropImageRenderMetrics.displayScale
            }}
          />
          <div
            style={{
              ...htmlPrimaryCropAreaStyle,
              left: cropSessionState.cropArea.x * cropImageRenderMetrics.displayScale,
              top: cropSessionState.cropArea.y * cropImageRenderMetrics.displayScale,
              width: cropSessionState.cropArea.width * cropImageRenderMetrics.displayScale,
              height: cropSessionState.cropArea.height * cropImageRenderMetrics.displayScale
            }}
            onMouseDown={onBeginMoveCropArea}
          >
            {secondaryGuideRectInCropArea != null && (
              <div
                style={{
                  ...htmlSecondaryCropGuideStyle,
                  left: secondaryGuideRectInCropArea.x,
                  top: secondaryGuideRectInCropArea.y,
                  width: secondaryGuideRectInCropArea.width,
                  height: secondaryGuideRectInCropArea.height
                }}
              />
            )}
            {primaryGuideRectInCropArea != null && (
              <div
                style={{
                  ...htmlReferenceGuideStyle,
                  left: primaryGuideRectInCropArea.x,
                  top: primaryGuideRectInCropArea.y,
                  width: primaryGuideRectInCropArea.width,
                  height: primaryGuideRectInCropArea.height
                }}
              />
            )}
            <div
              style={htmlCropResizeHandleStyle}
              onMouseDown={onBeginResizeCropArea}
            />
          </div>
        </div>
        <Text style={styles.helperText}>
          파랑/민트 가이드는 참고용입니다. 크롭 사각형은 자유 비율로 이동·조절할 수 있습니다.
        </Text>
        <View style={styles.rowButtonContainer}>
          <ActionButton label="초기화" variant="secondary" onPress={onResetCropArea} />
        </View>
        <View style={styles.rowButtonContainer}>
          <ActionButton label="취소" variant="secondary" onPress={onClose} />
          <ActionButton label={cropSessionState.isProcessing ? "처리 중..." : "크롭 적용"} onPress={onApply} />
        </View>
      </View>
    </View>
  )
}
