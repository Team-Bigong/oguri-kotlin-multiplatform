import React from "react"
import { appStoreUrl, playStoreUrl } from "../data/officialContent"

type StoreButtonsProps = {
  className?: string
}

export const StoreButtons = ({ className }: StoreButtonsProps): React.JSX.Element => {
  const unreleasedStoreNoticeMessage = "조금만 기다려주세요! 4월 이내 출시 예정이에요."
  const defaultPlayStoreListingUrl = "https://play.google.com/store/apps"

  const handlePlayStoreButtonClick = (event: React.MouseEvent<HTMLAnchorElement>): void => {
    if (playStoreUrl !== defaultPlayStoreListingUrl) {
      return
    }
    event.preventDefault()
    window.alert(unreleasedStoreNoticeMessage)
  }

  return (
    <div className={`storeButtons ${className ?? ""}`.trim()}>
      <a href={playStoreUrl} target="_blank" rel="noreferrer" className="storeButton" onClick={handlePlayStoreButtonClick}>
        <img src="/assets/official/store/play-store-logo.png" alt="" className="storeButtonIcon" />
        <span className="storeButtonLabel">Google Play</span>
      </a>
      <a href={appStoreUrl} target="_blank" rel="noreferrer" className="storeButton">
        <img src="/assets/official/store/app-store-logo.png" alt="" className="storeButtonIcon" />
        <span className="storeButtonLabel">App Store</span>
      </a>
    </div>
  )
}
