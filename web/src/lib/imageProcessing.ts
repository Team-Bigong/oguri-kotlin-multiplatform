const TARGET_MAX_WIDTH_PX = 1280
const TARGET_MAX_BYTES = 800 * 1024
const MINIMUM_IMAGE_QUALITY = 0.45
const MAXIMUM_IMAGE_QUALITY = 0.92
const IMAGE_QUALITY_SEARCH_ITERATION_COUNT = 7
const IMAGE_WIDTH_DECREMENT_RATIO = 0.9
const MINIMUM_IMAGE_WIDTH_PX = 480
const DEFAULT_CROP_OUTPUT_WIDTH_PX = 1280

const loadImageElement = async (file: File): Promise<HTMLImageElement> => {
  const objectUrl = URL.createObjectURL(file)
  try {
    const imageElement = new Image()
    imageElement.src = objectUrl
    await imageElement.decode()
    return imageElement
  } finally {
    URL.revokeObjectURL(objectUrl)
  }
}

const toBlob = async (canvas: HTMLCanvasElement, quality: number): Promise<Blob> => {
  return new Promise((resolve, reject) => {
    canvas.toBlob(
      (binary) => {
        if (binary == null) {
          reject(new Error("이미지 인코딩에 실패했습니다."))
          return
        }
        resolve(binary)
      },
      "image/jpeg",
      quality
    )
  })
}

const createResizedCanvas = (
  sourceImageElement: HTMLImageElement,
  targetWidth: number,
  targetHeight: number
): HTMLCanvasElement => {
  const canvasElement = document.createElement("canvas")
  canvasElement.width = targetWidth
  canvasElement.height = targetHeight

  const canvasContext = canvasElement.getContext("2d")
  if (canvasContext == null) {
    throw new Error("브라우저 canvas 컨텍스트를 가져올 수 없습니다.")
  }

  canvasContext.imageSmoothingEnabled = true
  canvasContext.imageSmoothingQuality = "high"
  canvasContext.drawImage(sourceImageElement, 0, 0, targetWidth, targetHeight)
  return canvasElement
}

const compressByTargetSize = async (canvasElement: HTMLCanvasElement): Promise<Blob> => {
  const bestQualityBlob = await toBlob(canvasElement, MAXIMUM_IMAGE_QUALITY)
  if (bestQualityBlob.size <= TARGET_MAX_BYTES) {
    return bestQualityBlob
  }

  const minimumQualityBlob = await toBlob(canvasElement, MINIMUM_IMAGE_QUALITY)
  if (minimumQualityBlob.size > TARGET_MAX_BYTES) {
    return minimumQualityBlob
  }

  let minimumQuality = MINIMUM_IMAGE_QUALITY
  let maximumQuality = MAXIMUM_IMAGE_QUALITY
  let selectedBlob = minimumQualityBlob

  for (let iteration = 0; iteration < IMAGE_QUALITY_SEARCH_ITERATION_COUNT; iteration += 1) {
    const quality = (minimumQuality + maximumQuality) / 2
    const binary = await toBlob(canvasElement, quality)

    if (binary.size > TARGET_MAX_BYTES) {
      maximumQuality = quality
    } else {
      minimumQuality = quality
      selectedBlob = binary
    }
  }

  return selectedBlob
}

export const resizeAndCompressImage = async (file: File): Promise<Blob> => {
  const imageElement = await loadImageElement(file)

  const sourceWidth = imageElement.width
  const sourceHeight = imageElement.height
  const sourceAspectRatio = sourceHeight / sourceWidth

  let targetWidth = Math.min(sourceWidth, TARGET_MAX_WIDTH_PX)
  let targetHeight = Math.round(targetWidth * sourceAspectRatio)
  let compressedBinary = await compressByTargetSize(
    createResizedCanvas(imageElement, targetWidth, targetHeight)
  )

  while (compressedBinary.size > TARGET_MAX_BYTES && targetWidth > MINIMUM_IMAGE_WIDTH_PX) {
    targetWidth = Math.max(MINIMUM_IMAGE_WIDTH_PX, Math.round(targetWidth * IMAGE_WIDTH_DECREMENT_RATIO))
    targetHeight = Math.round(targetWidth * sourceAspectRatio)
    compressedBinary = await compressByTargetSize(
      createResizedCanvas(imageElement, targetWidth, targetHeight)
    )
  }

  return compressedBinary
}

type CropAndCompressImageOptions = {
  sourceX: number
  sourceY: number
  sourceWidth: number
  sourceHeight: number
  outputWidth?: number
}

export const cropAndCompressImage = async (
  file: File,
  options: CropAndCompressImageOptions
): Promise<Blob> => {
  const imageElement = await loadImageElement(file)

  const clampedSourceX = Math.min(
    Math.max(0, options.sourceX),
    Math.max(0, imageElement.width - 1)
  )
  const clampedSourceY = Math.min(
    Math.max(0, options.sourceY),
    Math.max(0, imageElement.height - 1)
  )
  const clampedSourceWidth = Math.min(
    Math.max(1, options.sourceWidth),
    imageElement.width - clampedSourceX
  )
  const clampedSourceHeight = Math.min(
    Math.max(1, options.sourceHeight),
    imageElement.height - clampedSourceY
  )

  let targetWidth = Math.min(
    options.outputWidth ?? DEFAULT_CROP_OUTPUT_WIDTH_PX,
    TARGET_MAX_WIDTH_PX
  )
  let targetHeight = Math.max(1, Math.round(targetWidth * (clampedSourceHeight / clampedSourceWidth)))

  let canvasElement = createResizedCanvas(imageElement, targetWidth, targetHeight)
  let canvasContext = canvasElement.getContext("2d")
  if (canvasContext == null) {
    throw new Error("브라우저 canvas 컨텍스트를 가져올 수 없습니다.")
  }
  canvasContext.drawImage(
    imageElement,
    clampedSourceX,
    clampedSourceY,
    clampedSourceWidth,
    clampedSourceHeight,
    0,
    0,
    targetWidth,
    targetHeight
  )
  let compressedBinary = await compressByTargetSize(canvasElement)

  while (compressedBinary.size > TARGET_MAX_BYTES && targetWidth > MINIMUM_IMAGE_WIDTH_PX) {
    targetWidth = Math.max(MINIMUM_IMAGE_WIDTH_PX, Math.round(targetWidth * IMAGE_WIDTH_DECREMENT_RATIO))
    targetHeight = Math.max(1, Math.round(targetWidth * (clampedSourceHeight / clampedSourceWidth)))
    canvasElement = createResizedCanvas(imageElement, targetWidth, targetHeight)
    canvasContext = canvasElement.getContext("2d")
    if (canvasContext == null) {
      throw new Error("브라우저 canvas 컨텍스트를 가져올 수 없습니다.")
    }
    canvasContext.drawImage(
      imageElement,
      clampedSourceX,
      clampedSourceY,
      clampedSourceWidth,
      clampedSourceHeight,
      0,
      0,
      targetWidth,
      targetHeight
    )
    compressedBinary = await compressByTargetSize(canvasElement)
  }

  return compressedBinary
}
