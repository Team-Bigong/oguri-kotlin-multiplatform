import React from "react"
import { webEnvironment } from "../../config/env"
import "./official.css"

const playStoreUrl = webEnvironment.playStoreUrl.trim().length > 0
  ? webEnvironment.playStoreUrl
  : "https://play.google.com/store/apps"

const appStoreUrl = webEnvironment.appStoreUrl.trim().length > 0
  ? webEnvironment.appStoreUrl
  : "https://apps.apple.com"

const featureItems = [
  {
    title: "연차 조합을 먼저 찾아줘요",
    description: "연차 몇 일만 바꿔도 얼마나 더 길게 쉬는지, 한 번에 비교해볼 수 있어요."
  },
  {
    title: "지금 가기 딱 좋은 곳을 보여줘요",
    description: "시기와 이동 부담까지 고려해서 지금 보기 좋은 여행지를 먼저 골라드려요."
  },
  {
    title: "마음에 드는 일정은 저장해둘 수 있어요",
    description: "다시 볼 일정과 여행지를 따로 담아두고, 필요할 때 바로 꺼내볼 수 있어요."
  }
]

const trustItems = [
  "지금 가기 좋은 추천 흐름을 홈에서 바로 확인",
  "캘린더에서 연차를 바꿔보며 조건별 비교",
  "저장한 일정/여행지를 마이페이지에서 한 번에 관리"
]

const promoAssetItems = [
  {
    title: "메인 히어로 사진",
    path: "/assets/official/hero-travel-collage.svg",
    concept: "연차 달력 + 여행 감성을 동시에 전달하는 대표 비주얼"
  },
  {
    title: "추천 카드 소개 이미지",
    path: "/assets/official/recommendation-cards-preview.svg",
    concept: "오구리 카드형 추천 UI를 한눈에 보여주는 스크린 목업"
  },
  {
    title: "공유하기 소개 이미지",
    path: "/assets/official/share-flow-preview.svg",
    concept: "링크 공유 이후 앱으로 이어지는 경험을 설명하는 플로우 그래픽"
  }
]

export const OfficialHomePage = (): React.JSX.Element => {
  return (
    <main className="officialPage">
      <section className="heroSection">
        <div className="heroCopyContainer">
          <p className="brandEyebrow">OGURI</p>
          <h1 className="heroTitle">이번엔 이렇게 쉬어볼까요?</h1>
          <p className="heroDescription">
            오구리는 연차를 더 알차게 쓰는 방법부터, 지금 가기 좋은 여행지까지
            부담 없이 이어서 살펴보게 도와줘요.
          </p>
          <div className="heroButtonRow">
            <a className="primaryCtaButton" href={playStoreUrl} target="_blank" rel="noreferrer">
              Play Store에서 보기
            </a>
            <a className="secondaryCtaButton" href={appStoreUrl} target="_blank" rel="noreferrer">
              App Store에서 보기
            </a>
          </div>
        </div>
        <img
          className="heroVisualImage"
          src="/assets/official/hero-travel-collage.svg"
          alt="오구리 메인 소개 비주얼"
        />
      </section>

      <section className="highlightStripSection">
        <p className="highlightStripText">
          이 조합, 오구리만 알고 있었어요. 이번 연차, 더 아깝지 않게 쉬어보세요.
        </p>
      </section>

      <section className="contentSection">
        <div className="sectionHeadingContainer">
          <p className="sectionLabel">How Oguri Works</p>
          <h2 className="sectionTitle">처음부터 복잡하지 않게 만들었어요</h2>
        </div>
        <div className="featureGrid">
          {featureItems.map((featureItem, index) => (
            <article key={featureItem.title} className="featureCard">
              <p className="featureStep">STEP {index + 1}</p>
              <h3 className="featureTitle">{featureItem.title}</h3>
              <p className="featureDescription">{featureItem.description}</p>
            </article>
          ))}
        </div>
      </section>

      <section className="contentSection splitSection">
        <div className="splitSectionCopy">
          <p className="sectionLabel">Trust Signals</p>
          <h2 className="sectionTitle">고를 때 망설이지 않도록</h2>
          <p className="sectionDescription">
            오구리는 “어디서부터 봐야 하지?” 같은 고민이 덜 생기도록,
            필요한 순서대로 정보를 이어서 보여주는 데 집중했어요.
          </p>
          <ul className="trustChecklist">
            {trustItems.map((trustItem) => (
              <li key={trustItem}>{trustItem}</li>
            ))}
          </ul>
          <a className="textLinkButton" href={playStoreUrl} target="_blank" rel="noreferrer">
            지금 앱에서 구경해볼까요?
          </a>
        </div>
        <img
          className="splitSectionImage"
          src="/assets/official/recommendation-cards-preview.svg"
          alt="오구리 추천 카드 소개 이미지"
        />
      </section>

      <section className="contentSection">
        <div className="sectionHeadingContainer">
          <p className="sectionLabel">Share</p>
          <h2 className="sectionTitle">좋은 일정은 바로 공유해요</h2>
          <p className="sectionDescription">
            공유 링크를 받으면 앱이 설치된 경우 바로 열리고, 없으면 스토어로 자연스럽게 이어질 수 있게 준비 중이에요.
          </p>
        </div>
        <div className="sharePreviewCard">
          <img
            className="sharePreviewImage"
            src="/assets/official/share-flow-preview.svg"
            alt="오구리 공유 플로우 소개 이미지"
          />
          <div className="sharePreviewCopy">
            <h3 className="sharePreviewTitle">공유 문구 톤 예시</h3>
            <p className="sharePreviewQuote">“연차 3일로 5일이나 쉴 수 있어요. 한번 구경해볼까요?”</p>
          </div>
        </div>
      </section>

      <section className="contentSection">
        <div className="sectionHeadingContainer">
          <p className="sectionLabel">Asset Plan</p>
          <h2 className="sectionTitle">홍보용 에셋이 들어갈 위치</h2>
          <p className="sectionDescription">
            현재는 더미 에셋으로 구성되어 있고, 아래 경로의 파일만 실제 홍보 이미지로 교체하면 됩니다.
          </p>
        </div>
        <div className="assetPlanGrid">
          {promoAssetItems.map((promoAssetItem) => (
            <article key={promoAssetItem.path} className="assetPlanCard">
              <h3 className="assetPlanTitle">{promoAssetItem.title}</h3>
              <p className="assetPlanPath">{promoAssetItem.path}</p>
              <p className="assetPlanConcept">{promoAssetItem.concept}</p>
            </article>
          ))}
        </div>
      </section>

      <section className="finalCtaSection">
        <h2 className="finalCtaTitle">이번엔 이렇게 쉬어볼까요?</h2>
        <p className="finalCtaDescription">딱 맞는 연차 조합과 여행지, 오구리에서 바로 구경해보세요.</p>
        <div className="heroButtonRow">
          <a className="primaryCtaButton" href={playStoreUrl} target="_blank" rel="noreferrer">
            Google Play
          </a>
          <a className="secondaryCtaButton" href={appStoreUrl} target="_blank" rel="noreferrer">
            App Store
          </a>
        </div>
      </section>
    </main>
  )
}
