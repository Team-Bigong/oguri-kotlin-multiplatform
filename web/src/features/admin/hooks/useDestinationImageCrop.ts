import React, { useCallback, useEffect, useMemo, useRef, useState } from "react"
import { cropAndCompressImage } from "../../../lib/imageProcessing"
import { deleteImageFromFirebaseStorageByUrl, uploadImageToFirebaseStorage } from "../../../lib/firebase"
import {
  CropDragState,
  CropSessionState,
  CropTargetType,
  DestinationFormState,
  UploadStage
} from "../types/adminLocalTypes"
import {
  CROP_MINIMUM_SIZE_PX,
  EXPERIENCE_IMAGE_ASPECT_RATIO,
  EXPERIENCE_IMAGE_OUTPUT_WIDTH_PX,
  PLACE_IMAGE_OUTPUT_WIDTH_PX,
  PLACE_IMAGE_PRIMARY_ASPECT_RATIO,
  PLACE_IMAGE_SECONDARY_ASPECT_RATIO,
  clampGuideRectWithinCropArea,
  createCropQueueItems,
  createGuideRect,
  createInitialCropArea,
  parseExperienceSequenceNumberFromThumbnailUrl,
  parseImageSequenceNumberFromImageUrl,
  validateStoragePathSegment
} from "../utils/adminHelpers"

type UseDestinationImageCropParams = {
  destinationFormState: DestinationFormState
  setDestinationFormState: React.Dispatch<React.SetStateAction<DestinationFormState>>
  windowWidth: number
  windowHeight: number
  setErrorMessage: React.Dispatch<React.SetStateAction<string>>
  setNoticeMessage: React.Dispatch<React.SetStateAction<string>>
}

type UseDestinationImageCropResult = {
  activeUploadTaskCount: number
  currentUploadStage: UploadStage | null
  uploadingDestinationImageCount: number
  uploadingExperienceIndexes: number[]
  cropSessionState: CropSessionState | null
  cropSessionItem: CropSessionState["queueItems"][number] | null
  cropImageRenderMetrics: {
    displayScale: number
    displayWidth: number
    displayHeight: number
  } | null
  primaryGuideRectInCropArea: {
    x: number
    y: number
    width: number
    height: number
  } | null
  secondaryGuideRectInCropArea: {
    x: number
    y: number
    width: number
    height: number
  } | null
  openCropSession: (
    targetType: CropTargetType,
    files: File[],
    options: {
      applyMode: "append" | "replace"
      destinationImageIndex: number | null
      experienceIndex: number | null
    }
  ) => Promise<void>
  beginMoveCropArea: (event: React.MouseEvent<HTMLDivElement>) => void
  beginResizeCropArea: (event: React.MouseEvent<HTMLDivElement>) => void
  resetCurrentCropArea: () => void
  closeCurrentCropSession: () => void
  applyCurrentCrop: () => Promise<void>
}

export const useDestinationImageCrop = ({
  destinationFormState,
  setDestinationFormState,
  windowWidth,
  windowHeight,
  setErrorMessage,
  setNoticeMessage
}: UseDestinationImageCropParams): UseDestinationImageCropResult => {
  const [activeUploadTaskCount, setActiveUploadTaskCount] = useState<number>(0)
  const [currentUploadStage, setCurrentUploadStage] = useState<UploadStage | null>(null)
  const [uploadingDestinationImageCount, setUploadingDestinationImageCount] = useState<number>(0)
  const [uploadingExperienceIndexes, setUploadingExperienceIndexes] = useState<number[]>([])
  const [cropSessionState, setCropSessionState] = useState<CropSessionState | null>(null)

  const cropSessionRef = useRef<CropSessionState | null>(null)
  const cropDragState = useRef<CropDragState>({
    mode: null,
    pointerStartX: 0,
    pointerStartY: 0,
    cropAreaAtStart: null
  })

  const cropSessionItem = cropSessionState == null ? null : cropSessionState.queueItems[cropSessionState.currentIndex]

  const cropImageRenderMetrics = useMemo(() => {
    if (cropSessionItem == null) {
      return null
    }
    const containerMaximumWidth = Math.min(Math.max(360, windowWidth * 0.72), 860)
    const containerMaximumHeight = Math.min(Math.max(260, windowHeight * 0.62), 560)
    const fitScale = Math.min(
      containerMaximumWidth / cropSessionItem.naturalWidth,
      containerMaximumHeight / cropSessionItem.naturalHeight
    )
    const displayScale = Number.isFinite(fitScale) && fitScale > 0 ? fitScale : 1
    return {
      displayScale,
      displayWidth: cropSessionItem.naturalWidth * displayScale,
      displayHeight: cropSessionItem.naturalHeight * displayScale
    }
  }, [cropSessionItem, windowHeight, windowWidth])

  const primaryGuideRectInCropArea = useMemo(() => {
    if (cropImageRenderMetrics == null || cropSessionState == null) {
      return null
    }
    const cropDisplayWidth = cropSessionState.cropArea.width * cropImageRenderMetrics.displayScale
    const cropDisplayHeight = cropSessionState.cropArea.height * cropImageRenderMetrics.displayScale
    const aspectRatio = cropSessionState.targetType === "experience"
      ? EXPERIENCE_IMAGE_ASPECT_RATIO
      : PLACE_IMAGE_PRIMARY_ASPECT_RATIO
    const guideRect = createGuideRect(
      cropDisplayWidth,
      cropDisplayHeight,
      aspectRatio
    )
    return clampGuideRectWithinCropArea(guideRect, cropDisplayWidth, cropDisplayHeight)
  }, [cropImageRenderMetrics, cropSessionState])

  const secondaryGuideRectInCropArea = useMemo(() => {
    if (cropImageRenderMetrics == null || cropSessionState?.targetType !== "destination") {
      return null
    }
    const cropDisplayWidth = cropSessionState.cropArea.width * cropImageRenderMetrics.displayScale
    const cropDisplayHeight = cropSessionState.cropArea.height * cropImageRenderMetrics.displayScale
    const guideRect = createGuideRect(
      cropDisplayWidth,
      cropDisplayHeight,
      PLACE_IMAGE_SECONDARY_ASPECT_RATIO
    )
    return clampGuideRectWithinCropArea(guideRect, cropDisplayWidth, cropDisplayHeight)
  }, [cropImageRenderMetrics, cropSessionState])

  useEffect(() => {
    cropSessionRef.current = cropSessionState
  }, [cropSessionState])

  useEffect(() => {
    return () => {
      const activeCropSession = cropSessionRef.current
      if (activeCropSession == null) {
        return
      }
      activeCropSession.queueItems.forEach((queueItem) => {
        URL.revokeObjectURL(queueItem.previewUrl)
      })
    }
  }, [])

  const closeCropSession = useCallback((session: CropSessionState | null): void => {
    if (session == null) {
      setCropSessionState(null)
      return
    }
    session.queueItems.forEach((queueItem) => {
      URL.revokeObjectURL(queueItem.previewUrl)
    })
    setCropSessionState(null)
    cropDragState.current = {
      mode: null,
      pointerStartX: 0,
      pointerStartY: 0,
      cropAreaAtStart: null
    }
  }, [])

  const closeCurrentCropSession = useCallback((): void => {
    closeCropSession(cropSessionState)
  }, [closeCropSession, cropSessionState])

  const beginUploadTask = useCallback((stage: UploadStage): void => {
    setCurrentUploadStage(stage)
    setActiveUploadTaskCount((previousCount) => previousCount + 1)
  }, [])

  const endUploadTask = useCallback((): void => {
    setActiveUploadTaskCount((previousCount) => Math.max(0, previousCount - 1))
    setCurrentUploadStage(null)
  }, [])

  const openCropSession = useCallback(async (
    targetType: CropTargetType,
    files: File[],
    options: {
      applyMode: "append" | "replace"
      destinationImageIndex: number | null
      experienceIndex: number | null
    }
  ): Promise<void> => {
    if (files.length === 0) {
      return
    }

    setErrorMessage("")
    setNoticeMessage("")

    try {
      const queueItems = await createCropQueueItems(files)
      const currentQueueItem = queueItems[0]
      if (currentQueueItem == null) {
        return
      }
      setCropSessionState({
        targetType,
        applyMode: options.applyMode,
        queueItems,
        currentIndex: 0,
        destinationImageIndex: options.destinationImageIndex,
        experienceIndex: options.experienceIndex,
        cropArea: createInitialCropArea(currentQueueItem),
        isProcessing: false
      })
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "이미지 크롭 준비에 실패했습니다.")
    }
  }, [setErrorMessage, setNoticeMessage])

  const uploadCroppedDestinationImage = useCallback(async (binary: Blob): Promise<void> => {
    const countryPathSegment = validateStoragePathSegment(destinationFormState.storageCountrySlug, "Storage 국가 경로")
    const cityPathSegment = validateStoragePathSegment(destinationFormState.storageCitySlug, "Storage 도시 경로")
    const currentMaximumImageSequenceNumber = destinationFormState.images.reduce(
      (maximumSequenceNumber, image) => {
        const parsedSequenceNumber = parseImageSequenceNumberFromImageUrl(image.imageUrl)
        return parsedSequenceNumber == null
          ? maximumSequenceNumber
          : Math.max(maximumSequenceNumber, parsedSequenceNumber)
      },
      0
    )
    const imageSequenceNumber = currentMaximumImageSequenceNumber + 1
    const objectPath = `places/${countryPathSegment}/${cityPathSegment}/${imageSequenceNumber}.jpg`
    const imageUrl = await uploadImageToFirebaseStorage(binary, objectPath)

    setDestinationFormState((previousState) => {
      const hasThumbnail = previousState.images.some((image) => image.isThumbnail)
      return {
        ...previousState,
        images: [
          ...previousState.images,
          {
            imageUrl,
            isThumbnail: !hasThumbnail,
            sortOrder: previousState.images.length + 1
          }
        ],
        newlyUploadedImageUrls: [...previousState.newlyUploadedImageUrls, imageUrl]
      }
    })
  }, [destinationFormState.images, destinationFormState.storageCitySlug, destinationFormState.storageCountrySlug, setDestinationFormState])

  const replaceCroppedDestinationImage = useCallback(async (
    destinationImageIndex: number,
    binary: Blob
  ): Promise<void> => {
    const countryPathSegment = validateStoragePathSegment(destinationFormState.storageCountrySlug, "Storage 국가 경로")
    const cityPathSegment = validateStoragePathSegment(destinationFormState.storageCitySlug, "Storage 도시 경로")
    const currentMaximumImageSequenceNumber = destinationFormState.images.reduce(
      (maximumSequenceNumber, image) => {
        const parsedSequenceNumber = parseImageSequenceNumberFromImageUrl(image.imageUrl)
        return parsedSequenceNumber == null
          ? maximumSequenceNumber
          : Math.max(maximumSequenceNumber, parsedSequenceNumber)
      },
      0
    )
    const imageSequenceNumber = currentMaximumImageSequenceNumber + 1
    const objectPath = `places/${countryPathSegment}/${cityPathSegment}/${imageSequenceNumber}.jpg`
    const replacedImageUrl = await uploadImageToFirebaseStorage(binary, objectPath)
    const currentImageUrl = destinationFormState.images[destinationImageIndex]?.imageUrl ?? ""

    if (currentImageUrl.length > 0 && destinationFormState.newlyUploadedImageUrls.includes(currentImageUrl)) {
      await deleteImageFromFirebaseStorageByUrl(currentImageUrl)
    }

    setDestinationFormState((previousState) => ({
      ...previousState,
      images: previousState.images.map((image, index) => {
        if (index !== destinationImageIndex) {
          return image
        }
        return {
          ...image,
          imageUrl: replacedImageUrl
        }
      }),
      newlyUploadedImageUrls: [
        ...previousState.newlyUploadedImageUrls.filter((imageUrl) => imageUrl !== currentImageUrl),
        replacedImageUrl
      ]
    }))
  }, [destinationFormState.images, destinationFormState.newlyUploadedImageUrls, destinationFormState.storageCitySlug, destinationFormState.storageCountrySlug, setDestinationFormState])

  const uploadCroppedExperienceThumbnail = useCallback(async (
    experienceIndex: number,
    binary: Blob
  ): Promise<void> => {
    const countryPathSegment = validateStoragePathSegment(destinationFormState.storageCountrySlug, "Storage 국가 경로")
    const cityPathSegment = validateStoragePathSegment(destinationFormState.storageCitySlug, "Storage 도시 경로")

    const currentMaximumExperienceSequenceNumber = destinationFormState.experiences.reduce(
      (maximumSequenceNumber, experience) => {
        const parsedSequenceNumber = parseExperienceSequenceNumberFromThumbnailUrl(experience.thumbnailUrl)
        return parsedSequenceNumber == null
          ? maximumSequenceNumber
          : Math.max(maximumSequenceNumber, parsedSequenceNumber)
      },
      0
    )

    const currentThumbnailUrl = destinationFormState.experiences[experienceIndex]?.thumbnailUrl ?? ""
    const experienceSequenceNumber = currentMaximumExperienceSequenceNumber + 1
    const objectPath = `places/${countryPathSegment}/${cityPathSegment}/experiences/${experienceSequenceNumber}.jpg`
    const uploadedThumbnailUrl = await uploadImageToFirebaseStorage(binary, objectPath)

    if (currentThumbnailUrl.length > 0 && destinationFormState.newlyUploadedExperienceThumbnailUrls.includes(currentThumbnailUrl)) {
      await deleteImageFromFirebaseStorageByUrl(currentThumbnailUrl)
    }

    setDestinationFormState((previousState) => {
      const nextExperienceItems = previousState.experiences.map((experience, targetIndex) => {
        if (targetIndex !== experienceIndex) {
          return experience
        }
        return {
          ...experience,
          thumbnailUrl: uploadedThumbnailUrl
        }
      })

      return {
        ...previousState,
        experiences: nextExperienceItems,
        newlyUploadedExperienceThumbnailUrls: [
          ...previousState.newlyUploadedExperienceThumbnailUrls.filter((thumbnailUrl) => thumbnailUrl !== currentThumbnailUrl),
          uploadedThumbnailUrl
        ]
      }
    })
  }, [destinationFormState.experiences, destinationFormState.newlyUploadedExperienceThumbnailUrls, destinationFormState.storageCitySlug, destinationFormState.storageCountrySlug, setDestinationFormState])

  const applyCurrentCrop = useCallback(async (): Promise<void> => {
    if (cropSessionState == null || cropSessionItem == null) {
      return
    }
    if (cropSessionState.isProcessing) {
      return
    }

    setCropSessionState((previousSession) => previousSession == null ? null : { ...previousSession, isProcessing: true })
    beginUploadTask("preparing")

    try {
      const outputWidth = cropSessionState.targetType === "experience"
        ? EXPERIENCE_IMAGE_OUTPUT_WIDTH_PX
        : PLACE_IMAGE_OUTPUT_WIDTH_PX
      const croppedBinary = await cropAndCompressImage(cropSessionItem.file, {
        sourceX: cropSessionState.cropArea.x,
        sourceY: cropSessionState.cropArea.y,
        sourceWidth: cropSessionState.cropArea.width,
        sourceHeight: cropSessionState.cropArea.height,
        outputWidth
      })

      setCurrentUploadStage("uploading")
      if (cropSessionState.targetType === "destination") {
        setUploadingDestinationImageCount((previousCount) => previousCount + 1)
        if (cropSessionState.applyMode === "replace") {
          const destinationImageIndex = cropSessionState.destinationImageIndex
          if (destinationImageIndex == null) {
            throw new Error("수정할 장소 사진 인덱스를 확인할 수 없습니다.")
          }
          await replaceCroppedDestinationImage(destinationImageIndex, croppedBinary)
        } else {
          await uploadCroppedDestinationImage(croppedBinary)
        }
        setUploadingDestinationImageCount((previousCount) => Math.max(0, previousCount - 1))
      } else {
        const experienceIndex = cropSessionState.experienceIndex
        if (experienceIndex == null) {
          throw new Error("체험 인덱스를 확인할 수 없습니다.")
        }
        setUploadingExperienceIndexes((previousIndexes) => {
          if (previousIndexes.includes(experienceIndex)) {
            return previousIndexes
          }
          return [...previousIndexes, experienceIndex]
        })
        await uploadCroppedExperienceThumbnail(experienceIndex, croppedBinary)
        setUploadingExperienceIndexes((previousIndexes) => previousIndexes.filter((index) => index !== experienceIndex))
      }

      const hasNextImage = cropSessionState.currentIndex < cropSessionState.queueItems.length - 1
      if (hasNextImage) {
        setCropSessionState((previousSession) => {
          if (previousSession == null) {
            return null
          }
          const nextIndex = previousSession.currentIndex + 1
          const nextQueueItem = previousSession.queueItems[nextIndex]
          if (nextQueueItem == null) {
            return null
          }
          return {
            ...previousSession,
            currentIndex: nextIndex,
            cropArea: createInitialCropArea(nextQueueItem),
            isProcessing: false
          }
        })
      } else {
        closeCropSession(cropSessionState)
        if (cropSessionState.targetType === "destination") {
          setNoticeMessage("장소 사진 업로드가 완료되었습니다.")
        } else {
          setNoticeMessage("체험 썸네일 업로드가 완료되었습니다.")
        }
      }
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "이미지 업로드에 실패했습니다.")
      setCropSessionState((previousSession) => previousSession == null ? null : { ...previousSession, isProcessing: false })
    } finally {
      endUploadTask()
    }
  }, [beginUploadTask, closeCropSession, cropSessionItem, cropSessionState, endUploadTask, replaceCroppedDestinationImage, setErrorMessage, setNoticeMessage, uploadCroppedDestinationImage, uploadCroppedExperienceThumbnail])

  const beginMoveCropArea = useCallback((event: React.MouseEvent<HTMLDivElement>): void => {
    if (cropSessionState == null) {
      return
    }
    event.preventDefault()
    cropDragState.current = {
      mode: "move",
      pointerStartX: event.clientX,
      pointerStartY: event.clientY,
      cropAreaAtStart: cropSessionState.cropArea
    }
  }, [cropSessionState])

  const beginResizeCropArea = useCallback((event: React.MouseEvent<HTMLDivElement>): void => {
    if (cropSessionState == null) {
      return
    }
    event.preventDefault()
    event.stopPropagation()
    cropDragState.current = {
      mode: "resize",
      pointerStartX: event.clientX,
      pointerStartY: event.clientY,
      cropAreaAtStart: cropSessionState.cropArea
    }
  }, [cropSessionState])

  const moveCropAreaByPointerPosition = useCallback((pointerX: number, pointerY: number): void => {
    if (cropSessionState == null || cropSessionItem == null || cropImageRenderMetrics == null) {
      return
    }
    const dragMode = cropDragState.current.mode
    const cropAreaAtStart = cropDragState.current.cropAreaAtStart
    if (dragMode == null || cropAreaAtStart == null) {
      return
    }

    const deltaX = (pointerX - cropDragState.current.pointerStartX) / cropImageRenderMetrics.displayScale
    const deltaY = (pointerY - cropDragState.current.pointerStartY) / cropImageRenderMetrics.displayScale

    setCropSessionState((previousSession) => {
      if (previousSession == null) {
        return null
      }

      if (dragMode === "move") {
        const clampedX = Math.max(0, Math.min(cropSessionItem.naturalWidth - cropAreaAtStart.width, cropAreaAtStart.x + deltaX))
        const clampedY = Math.max(0, Math.min(cropSessionItem.naturalHeight - cropAreaAtStart.height, cropAreaAtStart.y + deltaY))
        return {
          ...previousSession,
          cropArea: {
            ...previousSession.cropArea,
            x: clampedX,
            y: clampedY
          }
        }
      }

      const minimumCropWidth = Math.min(CROP_MINIMUM_SIZE_PX, cropSessionItem.naturalWidth)
      const minimumCropHeight = Math.min(CROP_MINIMUM_SIZE_PX, cropSessionItem.naturalHeight)
      const maximumCropWidth = cropSessionItem.naturalWidth - cropAreaAtStart.x
      const maximumCropHeight = cropSessionItem.naturalHeight - cropAreaAtStart.y
      const nextWidth = Math.max(minimumCropWidth, Math.min(maximumCropWidth, cropAreaAtStart.width + deltaX))
      const nextHeight = Math.max(minimumCropHeight, Math.min(maximumCropHeight, cropAreaAtStart.height + deltaY))

      return {
        ...previousSession,
        cropArea: {
          ...previousSession.cropArea,
          width: nextWidth,
          height: nextHeight
        }
      }
    })
  }, [cropImageRenderMetrics, cropSessionItem, cropSessionState])

  const endCropAreaPointer = useCallback((): void => {
    cropDragState.current = {
      mode: null,
      pointerStartX: 0,
      pointerStartY: 0,
      cropAreaAtStart: null
    }
  }, [])

  useEffect(() => {
    const handleWindowMouseMove = (event: MouseEvent): void => {
      if (cropDragState.current.mode == null) {
        return
      }
      moveCropAreaByPointerPosition(event.clientX, event.clientY)
    }

    const handleWindowMouseUp = (): void => {
      if (cropDragState.current.mode == null) {
        return
      }
      endCropAreaPointer()
    }

    window.addEventListener("mousemove", handleWindowMouseMove)
    window.addEventListener("mouseup", handleWindowMouseUp)
    return () => {
      window.removeEventListener("mousemove", handleWindowMouseMove)
      window.removeEventListener("mouseup", handleWindowMouseUp)
    }
  }, [endCropAreaPointer, moveCropAreaByPointerPosition])

  const resetCurrentCropArea = useCallback((): void => {
    setCropSessionState((previousSession) => {
      if (previousSession == null) {
        return null
      }
      const currentQueueItem = previousSession.queueItems[previousSession.currentIndex]
      if (currentQueueItem == null) {
        return previousSession
      }
      return {
        ...previousSession,
        cropArea: createInitialCropArea(currentQueueItem)
      }
    })
  }, [])

  return {
    activeUploadTaskCount,
    currentUploadStage,
    uploadingDestinationImageCount,
    uploadingExperienceIndexes,
    cropSessionState,
    cropSessionItem,
    cropImageRenderMetrics,
    primaryGuideRectInCropArea,
    secondaryGuideRectInCropArea,
    openCropSession,
    beginMoveCropArea,
    beginResizeCropArea,
    resetCurrentCropArea,
    closeCurrentCropSession,
    applyCurrentCrop
  }
}
