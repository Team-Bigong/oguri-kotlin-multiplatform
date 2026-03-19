const TARGET_MAX_WIDTH_PX = 1280
const TARGET_MAX_BYTES = 800 * 1024
const MINIMUM_IMAGE_QUALITY = 0.4
const IMAGE_QUALITY_DECREMENT = 0.07
const IMAGE_WIDTH_DECREMENT_RATIO = 0.9

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

export const resizeAndCompressImage = async (file: File): Promise<Blob> => {
  const imageElement = await loadImageElement(file)

  const sourceWidth = imageElement.width
  const sourceHeight = imageElement.height
  const scaledWidth = Math.min(sourceWidth, TARGET_MAX_WIDTH_PX)
  const scaledHeight = Math.round((sourceHeight * scaledWidth) / sourceWidth)

  const canvasElement = document.createElement("canvas")
  canvasElement.width = scaledWidth
  canvasElement.height = scaledHeight

  const canvasContext = canvasElement.getContext("2d")
  if (canvasContext == null) {
    throw new Error("브라우저 canvas 컨텍스트를 가져올 수 없습니다.")
  }

  canvasContext.drawImage(imageElement, 0, 0, scaledWidth, scaledHeight)

  let imageQuality = 0.92
  let compressedBinary = await toBlob(canvasElement, imageQuality)

  while (compressedBinary.size > TARGET_MAX_BYTES && imageQuality > MINIMUM_IMAGE_QUALITY) {
    imageQuality -= IMAGE_QUALITY_DECREMENT
    compressedBinary = await toBlob(canvasElement, imageQuality)
  }

  while (compressedBinary.size > TARGET_MAX_BYTES && canvasElement.width > 320) {
    const reducedWidth = Math.round(canvasElement.width * IMAGE_WIDTH_DECREMENT_RATIO)
    const reducedHeight = Math.round((canvasElement.height * reducedWidth) / canvasElement.width)

    const nextCanvasElement = document.createElement("canvas")
    nextCanvasElement.width = reducedWidth
    nextCanvasElement.height = reducedHeight

    const nextCanvasContext = nextCanvasElement.getContext("2d")
    if (nextCanvasContext == null) {
      throw new Error("브라우저 canvas 컨텍스트를 가져올 수 없습니다.")
    }

    nextCanvasContext.drawImage(canvasElement, 0, 0, reducedWidth, reducedHeight)
    canvasElement.width = reducedWidth
    canvasElement.height = reducedHeight
    canvasContext.drawImage(nextCanvasElement, 0, 0, reducedWidth, reducedHeight)
    compressedBinary = await toBlob(canvasElement, imageQuality)
  }

  return compressedBinary
}
