import React from "react"
import { webEnvironment } from "../../config/env"
import "./official.css"

const playStoreUrl = webEnvironment.playStoreUrl.trim().length > 0
  ? webEnvironment.playStoreUrl
  : "https://play.google.com/store/apps"

const appStoreUrl = webEnvironment.appStoreUrl.trim().length > 0
  ? webEnvironment.appStoreUrl
  : "https://apps.apple.com"

const experienceItems = [
  {
    index: "01",
    title: "연차 조합을 먼저 보여줘요",
    description: "이번 달부터 1년 뒤까지, 더 길게 쉴 수 있는 날을 먼저 살펴볼 수 있어요."
  },
  {
    index: "02",
    title: "지금 가기 좋은 여행지로 이어져요",
    description: "시기와 이동 부담까지 함께 고려해서 지금 보기 좋은 여행지를 추천해요."
  },
  {
    index: "03",
    title: "마음에 들면 저장하고 공유해요",
    description: "좋은 일정은 저장해두고, 공유 링크로 바로 같이 볼 수 있어요."
  }
]

const questionAnswerItems = [
  {
    question: "오구리는 어떤 앱인가요?",
    answer: "연차 조합과 여행지 추천을 한 흐름으로 이어주는 앱이에요."
  },
  {
    question: "공유 링크는 어떻게 동작하나요?",
    answer: "앱이 설치되어 있으면 바로 열리고, 없으면 스토어로 이동하도록 연결할 수 있어요."
  },
  {
    question: "어디에서 설치할 수 있나요?",
    answer: "Google Play와 App Store에서 바로 설치할 수 있어요."
  }
]

export const OfficialHomePage = (): React.JSX.Element => {
  return (
    <main className="officialPage">
      <header className="topNavigationBar">
        <p className="brandTitle">OGURI</p>
        <div className="topNavigationActionRow">
          <a href={playStoreUrl} target="_blank" rel="noreferrer">Google Play</a>
          <a href={appStoreUrl} target="_blank" rel="noreferrer">App Store</a>
        </div>
      </header>

      <section className="heroSection">
        <div className="heroBackdropShape" />
        <div className="heroTextPanel">
          <p className="heroLabel">연차 추천 · 여행지 큐레이션 · 공유</p>
          <h1 className="heroHeadline">이번엔 이렇게 쉬어볼까요?</h1>
          <p className="heroBodyText">
            오구리는 연차를 어떻게 쓰면 좋을지부터
            지금 가기 좋은 여행지까지 부담 없이 이어서 보여줘요.
          </p>
          <div className="heroPrimaryActionRow">
            <a className="actionButtonPrimary" href={playStoreUrl} target="_blank" rel="noreferrer">Play Store에서 보기</a>
            <a className="actionButtonGhost" href={appStoreUrl} target="_blank" rel="noreferrer">App Store에서 보기</a>
          </div>
        </div>

        <div className="heroVisualPanel">
          <img src="/assets/official/hero-travel-collage.svg" alt="오구리 메인 소개 이미지" />
        </div>
      </section>

      <section className="messageBandSection">
        <p>이 조합, 오구리만 알고 있어요. 이번 연차, 더 아깝지 않게 쉬어보세요.</p>
      </section>

      <section className="immersivePreviewSection">
        <div className="immersivePreviewTextWrap">
          <p className="sectionSmallLabel">APP PREVIEW</p>
          <h2 className="sectionMainTitle">앱 화면을 먼저 구경해볼까요?</h2>
          <p className="sectionMainDescription">
            추천을 보는 흐름이 어떻게 이어지는지, 실제 화면 느낌으로 먼저 살펴보세요.
            지금은 더미 이미지가 들어가 있고, 같은 위치에 실제 캡처를 넣으면 바로 완성됩니다.
          </p>
        </div>
        <div className="phoneShowcaseRow">
          <figure className="phoneShowcaseFigure">
            <img src="/assets/official/screenshots/home-screen-placeholder.svg" alt="오구리 홈 화면" />
            <figcaption>홈 추천</figcaption>
          </figure>
          <figure className="phoneShowcaseFigure phoneShowcaseFigureCenter">
            <img src="/assets/official/screenshots/calendar-screen-placeholder.svg" alt="오구리 캘린더 화면" />
            <figcaption>캘린더 탐색</figcaption>
          </figure>
          <figure className="phoneShowcaseFigure">
            <img src="/assets/official/screenshots/my-page-screen-placeholder.svg" alt="오구리 마이페이지 화면" />
            <figcaption>저장 관리</figcaption>
          </figure>
        </div>
      </section>

      <section className="experienceSection">
        <div className="experienceHeaderWrap">
          <p className="sectionSmallLabel">HOW OGURI WORKS</p>
          <h2 className="sectionMainTitle">처음부터 복잡하지 않게 설계했어요</h2>
        </div>
        <ol className="experienceTimelineList">
          {experienceItems.map((experienceItem) => (
            <li key={experienceItem.index}>
              <p className="timelineIndex">{experienceItem.index}</p>
              <h3>{experienceItem.title}</h3>
              <p>{experienceItem.description}</p>
            </li>
          ))}
        </ol>
      </section>

      <section className="shareFlowSection">
        <div className="shareFlowVisualWrap">
          <img src="/assets/official/share-flow-preview.svg" alt="오구리 공유 흐름 이미지" />
        </div>
        <div className="shareFlowTextWrap">
          <p className="sectionSmallLabel">SHARE FLOW</p>
          <h2 className="sectionMainTitle">좋은 일정은 바로 공유해요</h2>
          <p className="sectionMainDescription">
            공유 링크를 받으면 앱 설치 여부에 맞춰 바로 열리거나 스토어로 이어지는 흐름을 만들 수 있어요.
          </p>
          <p className="shareFlowQuote">“연차 3일로 5일이나 쉴 수 있어요. 한번 구경해볼까요?”</p>
        </div>
      </section>

      <section className="questionSection">
        <div className="questionHeaderWrap">
          <p className="sectionSmallLabel">FAQ</p>
          <h2 className="sectionMainTitle">자주 묻는 질문</h2>
        </div>
        <div className="questionAccordionWrap">
          {questionAnswerItems.map((questionAnswerItem) => (
            <details key={questionAnswerItem.question}>
              <summary>{questionAnswerItem.question}</summary>
              <p>{questionAnswerItem.answer}</p>
            </details>
          ))}
        </div>
      </section>

      <section className="finalCallSection">
        <h2>딱 맞는 휴식 조합, 오구리에서 시작해볼까요?</h2>
        <p>앱에서 더 자세한 추천과 저장 기능을 바로 확인해보세요.</p>
        <div className="heroPrimaryActionRow">
          <a className="actionButtonPrimary" href={playStoreUrl} target="_blank" rel="noreferrer">Google Play</a>
          <a className="actionButtonGhost" href={appStoreUrl} target="_blank" rel="noreferrer">App Store</a>
        </div>
      </section>

      <aside className="mobileInstallBar">
        <p>오구리 앱에서 바로 볼까요?</p>
        <div>
          <a href={playStoreUrl} target="_blank" rel="noreferrer">Play</a>
          <a href={appStoreUrl} target="_blank" rel="noreferrer">App</a>
        </div>
      </aside>
    </main>
  )
}
