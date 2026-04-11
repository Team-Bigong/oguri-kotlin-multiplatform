import React, { useCallback, useRef } from "react"
import { Pressable, Text, TextInput, View } from "react-native"
import { htmlFieldStyle, htmlTextAreaFieldStyle, styles } from "../styles/adminStyles"

type LabelInputProps = {
  label: string
  value: string
  onChangeText: (value: string) => void
  multiline?: boolean
  keyboardType?: "default" | "numeric"
  secureTextEntry?: boolean
  enableBoldFormatting?: boolean
}

export const LabelInput = ({
  label,
  value,
  onChangeText,
  multiline = false,
  keyboardType = "default",
  secureTextEntry = false,
  enableBoldFormatting = false
}: LabelInputProps): React.JSX.Element => {
  const inputElementReference = useRef<HTMLInputElement | HTMLTextAreaElement | null>(null)

  const applyBoldFormat = useCallback((): void => {
    const inputElement = inputElementReference.current
    if (inputElement == null) {
      return
    }

    const selectionStart = inputElement.selectionStart ?? value.length
    const selectionEnd = inputElement.selectionEnd ?? selectionStart
    const selectedText = value.slice(selectionStart, selectionEnd)
    if (selectedText.length === 0) {
      return
    }

    const hasWrappedBoldMarker =
      selectionStart >= 2 &&
      value.slice(selectionStart - 2, selectionStart) === "**" &&
      value.slice(selectionEnd, selectionEnd + 2) === "**"

    const hasBoldMarkerInsideSelection = selectedText.includes("**")

    if (hasWrappedBoldMarker) {
      const unwrappedValue = `${value.slice(0, selectionStart - 2)}${selectedText}${value.slice(selectionEnd + 2)}`
      onChangeText(unwrappedValue)

      requestAnimationFrame(() => {
        const updatedInputElement = inputElementReference.current
        if (updatedInputElement == null) {
          return
        }
        const nextSelectionStart = selectionStart - 2
        const nextSelectionEnd = selectionEnd - 2
        updatedInputElement.focus()
        updatedInputElement.setSelectionRange(nextSelectionStart, nextSelectionEnd)
      })
      return
    }

    if (hasBoldMarkerInsideSelection) {
      const unboldedSelection = selectedText.replace(/\*\*/g, "")
      const updatedValue = `${value.slice(0, selectionStart)}${unboldedSelection}${value.slice(selectionEnd)}`
      onChangeText(updatedValue)

      requestAnimationFrame(() => {
        const updatedInputElement = inputElementReference.current
        if (updatedInputElement == null) {
          return
        }
        const nextSelectionEnd = selectionStart + unboldedSelection.length
        updatedInputElement.focus()
        updatedInputElement.setSelectionRange(selectionStart, nextSelectionEnd)
      })
      return
    }

    const formattedValue = `${value.slice(0, selectionStart)}**${selectedText}**${value.slice(selectionEnd)}`
    onChangeText(formattedValue)

    requestAnimationFrame(() => {
      const updatedInputElement = inputElementReference.current
      if (updatedInputElement == null) {
        return
      }
      const caretStart = selectionStart + 2
      const caretEnd = selectionStart + 2 + selectedText.length
      updatedInputElement.focus()
      updatedInputElement.setSelectionRange(caretStart, caretEnd)
    })
  }, [onChangeText, value])

  return (
    <View style={styles.row}>
      <View style={styles.fieldLabelRow}>
        <Text style={styles.fieldLabel}>{label}</Text>
        {enableBoldFormatting && (
          <Pressable style={styles.formatButton} onPress={applyBoldFormat}>
            <Text style={styles.formatButtonText}>B</Text>
          </Pressable>
        )}
      </View>
      {enableBoldFormatting ? (
        multiline ? (
          <textarea
            ref={(element) => {
              inputElementReference.current = element
            }}
            value={value}
            onChange={(event) => onChangeText(event.target.value)}
            style={htmlTextAreaFieldStyle}
          />
        ) : (
          <input
            ref={(element) => {
              inputElementReference.current = element
            }}
            value={value}
            onChange={(event) => onChangeText(event.target.value)}
            style={htmlFieldStyle}
            type={secureTextEntry ? "password" : "text"}
            inputMode={keyboardType === "numeric" ? "numeric" : "text"}
          />
        )
      ) : (
        <TextInput
          value={value}
          multiline={multiline}
          keyboardType={keyboardType}
          secureTextEntry={secureTextEntry}
          onChangeText={onChangeText}
          style={multiline ? styles.textInputMultiline : styles.textInput}
        />
      )}
    </View>
  )
}
