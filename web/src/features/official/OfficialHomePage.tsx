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
    title: "연차 조합 추천",
    description: "공휴일과 주말 사이, 더 길게 쉬는 조합을 먼저 골라 보여줘요."
  },
  {
    title: "캘린더 탐색",
    description: "연차 일수를 바꿔보며 내 일정에 맞는 조합을 빠르게 비교할 수 있어요."
  },
  {
    title: "여행지 큐레이션",
    description: "지금 시기에 보기 좋은 여행지를 연휴 흐름에 맞춰 이어서 추천해요."
  }
]

const previewItems = [
  {
    title: "홈",
    subtitle: "추천 전략을 한눈에",
    imagePath: "/assets/official/screenshots/home-screen-placeholder.svg"
  },
  {
    title: "캘린더",
    subtitle: "연차별 조합 비교",
    imagePath: "/assets/official/screenshots/calendar-screen-placeholder.svg"
  },
  {
    title: "마이페이지",
    subtitle: "저장 일정 관리",
    imagePath: "/assets/official/screenshots/my-page-screen-placeholder.svg"
  }
]

const frequentlyAskedQuestions = [
  {
    question: "오구리는 어떤 앱인가요?",
    answer: "연차와 공휴일 조합, 그리고 시기 맞춤 여행지를 한 흐름으로 추천해주는 앱이에요."
  },
  {
    question: "공유 링크는 어떻게 열리나요?",
    answer: "앱이 설치되어 있으면 바로 앱으로 열리고, 설치 전이라면 스토어로 자연스럽게 이어질 수 있어요."
  },
  {
    question: "어디서 설치할 수 있나요?",
    answer: "Google Play와 App Store에서 바로 설치할 수 있어요."
  }
]

export const OfficialHomePage = (): React.JSX.Element => {
  return (
    <div className="landingPage">
      <header className="topNavigationBar">
        <div className="contentContainer topNavigationRow">
          <p className="brandTitle">OGURI</p>
          <nav className="topNavigationLinks">
            <a href="#features">기능</a>
            <a href="#preview">미리보기</a>
            <a href="#download">다운로드</a>
          </nav>
          <div className="topStoreButtons">
            <a href={playStoreUrl} target="_blank" rel="noreferrer">Google Play</a>
            <a href={appStoreUrl} target="_blank" rel="noreferrer">App Store</a>
          </div>
        </div>
      </header>

      <section className="heroSection">
        <div className="heroGlowBall heroGlowBallLeft" />
        <div className="heroGlowBall heroGlowBallRight" />
        <div className="contentContainer heroGrid">
          <div className="heroCopyWrap">
            <p className="heroEyebrow">연차 조합의 기술</p>
            <h1 className="heroTitle">
              이번엔 이렇게
              <br />
              쉬어볼까요?
            </h1>
            <p className="heroDescription">
              오구리는 연차를 가장 알차게 쓰는 방법부터
              지금 가기 좋은 여행지까지 부담 없이 이어서 보여줘요.
            </p>
            <div className="heroBulletWrap">
              <span>연차 조합 추천</span>
              <span>시기 맞춤 여행지</span>
              <span>저장 및 공유</span>
            </div>
            <div className="heroActionRow">
              <a href={appStoreUrl} target="_blank" rel="noreferrer" className="solidActionButton">App Store</a>
              <a href={playStoreUrl} target="_blank" rel="noreferrer" className="outlineActionButton">Google Play</a>
            </div>
          </div>

          <div className="heroVisualWrap">
            <img className="heroDeviceCard heroDeviceCardBack" src="/assets/official/screenshots/calendar-screen-placeholder.svg" alt="오구리 캘린더 화면" />
            <img className="heroDeviceCard heroDeviceCardFront" src="/assets/official/screenshots/home-screen-placeholder.svg" alt="오구리 홈 화면" />
            <div className="heroFloatingCard">연차 3일로 5일이나 쉴 수 있어요</div>
          </div>
        </div>
      </section>

      <section className="highlightBandSection">
        <div className="contentContainer">
          <p>이 조합, 오구리만 알고 있었어요. 이번 연차, 더 아깝지 않게 쉬어보세요.</p>
        </div>
      </section>

      <section className="featureSection" id="features">
        <div className="contentContainer">
          <div className="sectionHeader">
            <p className="sectionEyebrow">CORE FLOW</p>
            <h2 className="sectionTitle">복잡하지 않게, 한 흐름으로</h2>
            <p className="sectionDescription">추천 확인부터 저장까지, 필요한 순서대로 이어져요.</p>
          </div>
          <div className="featureGrid">
            {featureItems.map((featureItem, index) => (
              <article className="featureCard" key={featureItem.title}>
                <p className="featureIndex">0{index + 1}</p>
                <h3>{featureItem.title}</h3>
                <p>{featureItem.description}</p>
              </article>
            ))}
          </div>
        </div>
      </section>

      <section className="previewSection" id="preview">
        <div className="contentContainer previewSectionInner">
          <div className="previewCopyWrap">
            <p className="sectionEyebrow">APP PREVIEW</p>
            <h2 className="sectionTitle">오구리 화면을 먼저 구경해볼까요?</h2>
            <p className="sectionDescription">
              현재는 더미 이미지가 들어가 있고, 같은 경로에 실제 캡처만 바꾸면
              바로 소개용 페이지로 사용할 수 있어요.
            </p>
          </div>
          <div className="previewGrid">
            {previewItems.map((previewItem) => (
              <article className="previewCard" key={previewItem.title}>
                <img src={previewItem.imagePath} alt={`${previewItem.title} 화면`} />
                <div className="previewMetaWrap">
                  <p className="previewTitle">{previewItem.title}</p>
                  <p className="previewSubtitle">{previewItem.subtitle}</p>
                </div>
              </article>
            ))}
          </div>
        </div>
      </section>

      <section className="questionSection">
        <div className="contentContainer questionSectionInner">
          <div className="questionCopyWrap">
            <p className="sectionEyebrow">FAQ</p>
            <h2 className="sectionTitle">자주 묻는 질문</h2>
          </div>
          <div className="questionListWrap">
            {frequentlyAskedQuestions.map((frequentlyAskedQuestion) => (
              <details key={frequentlyAskedQuestion.question}>
                <summary>{frequentlyAskedQuestion.question}</summary>
                <p>{frequentlyAskedQuestion.answer}</p>
              </details>
            ))}
          </div>
        </div>
      </section>

      <section className="callToActionSection" id="download">
        <div className="contentContainer">
          <div className="callToActionPanel">
            <h2>딱 맞는 휴식 조합, 오구리에서 시작해볼까요?</h2>
            <p>앱에서 더 자세한 추천과 저장 기능을 바로 확인해보세요.</p>
            <div className="heroActionRow">
              <a href={appStoreUrl} target="_blank" rel="noreferrer" className="solidActionButton">App Store</a>
              <a href={playStoreUrl} target="_blank" rel="noreferrer" className="outlineActionButton">Google Play</a>
            </div>
          </div>
        </div>
      </section>

      <footer className="footerSection">
        <div className="contentContainer footerRow">
          <div>
            <p className="footerBrandTitle">오구리</p>
            <p className="footerBrandSubTitle">연차 조합의 기술</p>
          </div>
          <div className="footerLinkRow">
            <a href="#">서비스 이용약관</a>
            <a href="#">개인정보 처리방침</a>
            <a href="#">건의하기</a>
          </div>
        </div>
      </footer>
    </div>
  )
}
