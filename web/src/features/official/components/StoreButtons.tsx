import React from "react"
import { appStoreUrl, playStoreUrl } from "../data/officialContent"

type StoreButtonsProps = {
  className?: string
}

export const StoreButtons = ({ className }: StoreButtonsProps): React.JSX.Element => {
  return (
    <div className={`storeButtons ${className ?? ""}`.trim()}>
      <a href={playStoreUrl} target="_blank" rel="noreferrer" className="storeButton">
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
