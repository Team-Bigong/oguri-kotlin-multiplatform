import React, { useCallback } from "react"
import { deleteImageFromFirebaseStorageByUrl } from "../../../lib/firebase"
import { CropTargetType, DestinationFormState } from "../types/adminLocalTypes"

type UseDestinationImageFormActionsParams = {
  destinationFormState: DestinationFormState
  setDestinationFormState: React.Dispatch<React.SetStateAction<DestinationFormState>>
  setErrorMessage: React.Dispatch<React.SetStateAction<string>>
  openCropSession: (
    targetType: CropTargetType,
    files: File[],
    options: {
      applyMode: "append" | "replace"
      destinationImageIndex: number | null
      experienceIndex: number | null
    }
  ) => Promise<void>
  openCropSessionFromUploadedImage: (
    targetType: CropTargetType,
    imageUrl: string,
    options: {
      destinationImageIndex: number | null
      experienceIndex: number | null
    }
  ) => Promise<void>
}

type UseDestinationImageFormActionsResult = {
  handleImageFileSelection: (event: React.ChangeEvent<HTMLInputElement>) => void
  handleExperienceThumbnailFileSelection: (
    experienceIndex: number,
    event: React.ChangeEvent<HTMLInputElement>
  ) => void
  handleOpenDestinationImageCrop: (imageUrl: string, destinationImageIndex: number) => void
  handleOpenExperienceImageCrop: (imageUrl: string, experienceIndex: number) => void
  handleRemoveDestinationImage: (destinationImageIndex: number) => void
  addExperienceItem: () => void
  moveExperienceItem: (experienceIndex: number, direction: "up" | "down") => void
  removeExperienceItem: (experienceIndex: number) => void
}

export const useDestinationImageFormActions = ({
  destinationFormState,
  setDestinationFormState,
  setErrorMessage,
  openCropSession,
  openCropSessionFromUploadedImage
}: UseDestinationImageFormActionsParams): UseDestinationImageFormActionsResult => {
  const handleImageFileSelection = useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
    const fileList = event.target.files
    const selectedFiles = fileList == null ? [] : Array.from(fileList)
    event.target.value = ""
    if (selectedFiles.length === 0) {
      return
    }
    void openCropSession("destination", selectedFiles, {
      applyMode: "append",
      destinationImageIndex: null,
      experienceIndex: null
    })
  }, [openCropSession])

  const handleExperienceThumbnailFileSelection = useCallback((
    experienceIndex: number,
    event: React.ChangeEvent<HTMLInputElement>
  ): void => {
    const selectedFile = event.target.files?.[0]
    event.target.value = ""
    if (selectedFile == null) {
      return
    }
    void openCropSession("experience", [selectedFile], {
      applyMode: "replace",
      destinationImageIndex: null,
      experienceIndex
    })
  }, [openCropSession])

  const handleOpenDestinationImageCrop = useCallback((imageUrl: string, destinationImageIndex: number): void => {
    void openCropSessionFromUploadedImage("destination", imageUrl, {
      destinationImageIndex,
      experienceIndex: null
    })
  }, [openCropSessionFromUploadedImage])

  const handleOpenExperienceImageCrop = useCallback((imageUrl: string, experienceIndex: number): void => {
    void openCropSessionFromUploadedImage("experience", imageUrl, {
      destinationImageIndex: null,
      experienceIndex
    })
  }, [openCropSessionFromUploadedImage])

  const addExperienceItem = useCallback(() => {
    setDestinationFormState((previousState) => ({
      ...previousState,
      experiences: [
        ...previousState.experiences,
        {
          title: "",
          description: "",
          thumbnailUrl: "",
          link: "",
          sortOrder: previousState.experiences.length + 1
        }
      ]
    }))
  }, [setDestinationFormState])

  const moveExperienceItem = useCallback((experienceIndex: number, direction: "up" | "down") => {
    setDestinationFormState((previousState) => {
      const targetIndex = direction === "up" ? experienceIndex - 1 : experienceIndex + 1
      if (targetIndex < 0 || targetIndex >= previousState.experiences.length) {
        return previousState
      }

      const nextExperienceItems = [...previousState.experiences]
      const temporaryExperience = nextExperienceItems[experienceIndex]
      nextExperienceItems[experienceIndex] = nextExperienceItems[targetIndex]
      nextExperienceItems[targetIndex] = temporaryExperience

      return {
        ...previousState,
        experiences: nextExperienceItems.map((experience, sequence) => ({
          ...experience,
          sortOrder: sequence + 1
        }))
      }
    })
  }, [setDestinationFormState])

  const removeExperienceItem = useCallback((experienceIndex: number) => {
    void (async () => {
      const targetExperience = destinationFormState.experiences[experienceIndex]
      if (targetExperience == null) {
        return
      }

      const shouldDeleteImmediately = targetExperience.thumbnailUrl.length > 0 &&
        destinationFormState.newlyUploadedExperienceThumbnailUrls.includes(targetExperience.thumbnailUrl)
      if (shouldDeleteImmediately) {
        try {
          await deleteImageFromFirebaseStorageByUrl(targetExperience.thumbnailUrl)
        } catch (error) {
          setErrorMessage("체험 썸네일 삭제 중 오류가 발생했습니다. 다시 시도해주세요.")
          return
        }
      }

      setDestinationFormState((previousState) => ({
        ...previousState,
        experiences: previousState.experiences
          .filter((_, targetIndex) => targetIndex !== experienceIndex)
          .map((experience, sequence) => ({ ...experience, sortOrder: sequence + 1 })),
        newlyUploadedExperienceThumbnailUrls: previousState.newlyUploadedExperienceThumbnailUrls
          .filter((thumbnailUrl) => thumbnailUrl !== targetExperience.thumbnailUrl)
      }))
    })()
  }, [destinationFormState.experiences, destinationFormState.newlyUploadedExperienceThumbnailUrls, setDestinationFormState, setErrorMessage])

  const handleRemoveDestinationImage = useCallback((destinationImageIndex: number): void => {
    void (async () => {
      const targetImage = destinationFormState.images[destinationImageIndex]
      const shouldDeleteImmediately = targetImage != null &&
        destinationFormState.newlyUploadedImageUrls.includes(targetImage.imageUrl)

      if (shouldDeleteImmediately) {
        try {
          await deleteImageFromFirebaseStorageByUrl(targetImage.imageUrl)
        } catch (error) {
          setErrorMessage("이미지 삭제 중 오류가 발생했습니다. 다시 시도해주세요.")
          return
        }
      }

      setDestinationFormState((previousState) => ({
        ...previousState,
        images: previousState.images
          .filter((_, targetIndex) => targetIndex !== destinationImageIndex)
          .map((targetImageInList, sequence) => ({ ...targetImageInList, sortOrder: sequence + 1 })),
        newlyUploadedImageUrls: previousState.newlyUploadedImageUrls
          .filter((imageUrl) => imageUrl !== (targetImage?.imageUrl ?? ""))
      }))
    })()
  }, [destinationFormState.images, destinationFormState.newlyUploadedImageUrls, setDestinationFormState, setErrorMessage])

  return {
    handleImageFileSelection,
    handleExperienceThumbnailFileSelection,
    handleOpenDestinationImageCrop,
    handleOpenExperienceImageCrop,
    handleRemoveDestinationImage,
    addExperienceItem,
    moveExperienceItem,
    removeExperienceItem
  }
}
