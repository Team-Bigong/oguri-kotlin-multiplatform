import React from "react"
import { webEnvironment } from "../../config/env"
import "./official.css"

const playStoreUrl = webEnvironment.playStoreUrl.trim().length > 0
  ? webEnvironment.playStoreUrl
  : "https://play.google.com/store/apps"

const appStoreUrl = webEnvironment.appStoreUrl.trim().length > 0
  ? webEnvironment.appStoreUrl
  : "https://apps.apple.com"

const valueCardItems = [
  {
    title: "연차 조합 추천",
    description: "이번 달부터 1년 뒤까지, 더 길게 쉴 수 있는 조합을 먼저 보여줘요."
  },
  {
    title: "시기 맞춤 여행지",
    description: "지금 가기 좋은 여행지를 시기와 이동 부담까지 고려해 추천해요."
  },
  {
    title: "공유 링크",
    description: "좋은 일정은 바로 공유하고, 앱이 없으면 스토어로 자연스럽게 이어져요."
  }
]

const appPreviewItems = [
  {
    title: "홈 추천",
    description: "이번에 쉬기 좋은 연차 조합과 여행지를 한눈에 봐요.",
    imagePath: "/assets/official/screenshots/home-screen-placeholder.svg",
    imageAlt: "오구리 홈 추천 화면"
  },
  {
    title: "캘린더 탐색",
    description: "연차 일수를 바꿔보며 가장 알찬 조합을 빠르게 비교해요.",
    imagePath: "/assets/official/screenshots/calendar-screen-placeholder.svg",
    imageAlt: "오구리 캘린더 화면"
  },
  {
    title: "마이페이지",
    description: "저장한 일정과 여행지를 다시 보고 이어서 준비해요.",
    imagePath: "/assets/official/screenshots/my-page-screen-placeholder.svg",
    imageAlt: "오구리 마이페이지 화면"
  }
]

const reviewCardItems = [
  {
    quote: "여행 준비를 미루던 습관이 줄었어요. 연차 조합을 먼저 보여주니까 바로 결정하게 돼요.",
    author: "일본 여행 준비 중 · iPhone"
  },
  {
    quote: "복잡한 캘린더를 직접 맞추지 않아도 돼서 좋았어요. 가볍게 켜서 비교해보게 돼요.",
    author: "직장인 사용자 · Galaxy"
  },
  {
    quote: "저장해둔 일정이랑 여행지를 다시 보기 쉬워서 실제 예약까지 이어졌어요.",
    author: "주말 여행 선호 · Android"
  }
]

const questionAnswerItems = [
  {
    question: "오구리는 어떤 앱인가요?",
    answer: "연차 일수와 시기를 기준으로 쉬기 좋은 조합과 여행지를 함께 추천해주는 앱이에요."
  },
  {
    question: "공유 링크를 받으면 어떻게 열리나요?",
    answer: "앱이 설치되어 있으면 바로 앱으로 열리고, 설치 전이라면 스토어 페이지로 이동하게 만들 수 있어요."
  },
  {
    question: "어디서 다운로드할 수 있나요?",
    answer: "Play Store와 App Store에서 바로 설치할 수 있어요."
  }
]

export const OfficialHomePage = (): React.JSX.Element => {
  return (
    <main className="officialPage">
      <header className="floatingHeader">
        <div className="headerBrand">OGURI</div>
        <nav className="headerActionRow">
          <a className="headerActionButton" href={playStoreUrl} target="_blank" rel="noreferrer">Google Play</a>
          <a className="headerActionButton headerActionButtonAccent" href={appStoreUrl} target="_blank" rel="noreferrer">App Store</a>
        </nav>
      </header>

      <section className="heroSection">
        <div className="heroTextContainer">
          <p className="heroEyebrow">연차 추천 · 여행지 큐레이션 · 공유</p>
          <h1 className="heroTitle">이번엔 이렇게 쉬어볼까요?</h1>
          <p className="heroDescription">
            오구리는 연차를 어떻게 쓰면 좋은지부터,
            지금 가기 좋은 여행지까지 부담 없이 이어서 보여줘요.
          </p>
          <div className="heroButtonRow">
            <a className="primaryCtaButton" href={playStoreUrl} target="_blank" rel="noreferrer">Play Store에서 보기</a>
            <a className="secondaryCtaButton" href={appStoreUrl} target="_blank" rel="noreferrer">App Store에서 보기</a>
          </div>
          <div className="heroStatRow">
            <article className="heroStatCard">
              <p className="heroStatValue">3 STEP</p>
              <p className="heroStatLabel">추천 확인까지 복잡하지 않게</p>
            </article>
            <article className="heroStatCard">
              <p className="heroStatValue">추천 + 저장</p>
              <p className="heroStatLabel">한 번에 이어지는 여행 준비 흐름</p>
            </article>
          </div>
        </div>

        <div className="heroVisualContainer">
          <img className="heroMainImage" src="/assets/official/hero-travel-collage.svg" alt="오구리 공식 웹 히어로 이미지" />
          <img className="heroSubImage heroSubImageLeft" src="/assets/official/recommendation-cards-preview.svg" alt="추천 화면 미리보기" />
          <img className="heroSubImage heroSubImageRight" src="/assets/official/share-flow-preview.svg" alt="공유 흐름 미리보기" />
        </div>
      </section>

      <section className="valueCardSection">
        {valueCardItems.map((valueCardItem) => (
          <article className="valueCard" key={valueCardItem.title}>
            <h2 className="valueCardTitle">{valueCardItem.title}</h2>
            <p className="valueCardDescription">{valueCardItem.description}</p>
          </article>
        ))}
      </section>

      <section className="momentSection">
        <p className="momentLabel">TRUSTED FLOW</p>
        <p className="momentText">이 조합, 오구리만 알고 있어요. 이번 연차, 더 아깝지 않게 쉬어보세요.</p>
      </section>

      <section className="appPreviewSection">
        <div className="appPreviewHeader">
          <p className="sectionEyebrow">APP PREVIEW</p>
          <h2 className="sectionTitle">오구리 화면을 미리 볼까요?</h2>
          <p className="sectionDescription">
            아래 이미지는 현재 더미 화면입니다. 실제 앱 스크린샷으로 교체하면 바로 완성형 소개 페이지로 사용할 수 있어요.
          </p>
        </div>
        <div className="appPreviewGrid">
          {appPreviewItems.map((appPreviewItem) => (
            <article className="appPreviewCard" key={appPreviewItem.title}>
              <div className="phoneMockupFrame">
                <img src={appPreviewItem.imagePath} alt={appPreviewItem.imageAlt} />
              </div>
              <h3 className="appPreviewTitle">{appPreviewItem.title}</h3>
              <p className="appPreviewDescription">{appPreviewItem.description}</p>
            </article>
          ))}
        </div>
      </section>

      <section className="storySection">
        <div className="storyCard">
          <p className="sectionEyebrow">WHY OGURI</p>
          <h2 className="sectionTitle">처음부터 복잡하지 않게 만들었어요</h2>
          <p className="sectionDescription">
            “어디서부터 보지?” 같은 고민이 덜 생기도록,
            오구리는 필요한 순서대로 정보를 보여주는 데 집중했어요.
          </p>
          <ul className="storyChecklist">
            <li>연차 일수별로 쉬기 좋은 조합 먼저 보기</li>
            <li>시기 맞는 여행지 추천까지 이어서 확인</li>
            <li>마음에 든 일정과 장소는 저장해서 다시 보기</li>
          </ul>
        </div>
        <img className="storyImage" src="/assets/official/recommendation-cards-preview.svg" alt="오구리 추천 카드 섹션 이미지" />
      </section>

      <section className="reviewSection">
        <div className="reviewSectionHeader">
          <p className="sectionEyebrow">REVIEW</p>
          <h2 className="sectionTitle">이미 많은 분들이 사용 중이에요</h2>
        </div>
        <div className="reviewGrid">
          {reviewCardItems.map((reviewCardItem, index) => (
            <article className="reviewCard" key={index}>
              <p className="reviewQuote">“{reviewCardItem.quote}”</p>
              <p className="reviewAuthor">{reviewCardItem.author}</p>
            </article>
          ))}
        </div>
      </section>

      <section className="questionSection">
        <div className="questionSectionHeader">
          <p className="sectionEyebrow">FAQ</p>
          <h2 className="sectionTitle">자주 묻는 질문</h2>
        </div>
        <div className="questionList">
          {questionAnswerItems.map((questionAnswerItem) => (
            <details className="questionItem" key={questionAnswerItem.question}>
              <summary>{questionAnswerItem.question}</summary>
              <p>{questionAnswerItem.answer}</p>
            </details>
          ))}
        </div>
      </section>

      <section className="assetGuideSection">
        <div className="assetGuideHeader">
          <p className="sectionEyebrow">ASSET GUIDE</p>
          <h2 className="sectionTitle">홍보 이미지 교체 위치</h2>
        </div>
        <div className="assetGuideGrid">
          <article className="assetGuideCard">
            <h3>히어로 비주얼</h3>
            <p className="assetGuidePath">/assets/official/hero-travel-collage.svg</p>
            <p>첫 화면의 핵심 감성을 보여주는 대표 이미지</p>
          </article>
          <article className="assetGuideCard">
            <h3>추천/공유 소개</h3>
            <p className="assetGuidePath">/assets/official/recommendation-cards-preview.svg</p>
            <p className="assetGuidePath">/assets/official/share-flow-preview.svg</p>
            <p>추천 흐름과 공유 흐름을 보여주는 보조 이미지</p>
          </article>
          <article className="assetGuideCard">
            <h3>앱 스크린샷 3종</h3>
            <p className="assetGuidePath">/assets/official/screenshots/*.svg</p>
            <p>홈/캘린더/마이페이지 화면 위주로 실제 앱 캡처 권장</p>
          </article>
        </div>
      </section>

      <section className="finalCtaSection">
        <h2 className="finalCtaTitle">딱 맞는 휴식 조합, 오구리에서 시작해볼까요?</h2>
        <p className="finalCtaDescription">앱에서 더 자세한 추천과 저장 기능을 바로 확인해보세요.</p>
        <div className="heroButtonRow">
          <a className="primaryCtaButton" href={playStoreUrl} target="_blank" rel="noreferrer">Google Play</a>
          <a className="secondaryCtaButton" href={appStoreUrl} target="_blank" rel="noreferrer">App Store</a>
        </div>
      </section>

      <aside className="mobileStickyCta">
        <p className="mobileStickyCtaTitle">오구리 앱에서 바로 볼까요?</p>
        <div className="mobileStickyCtaButtonRow">
          <a href={playStoreUrl} target="_blank" rel="noreferrer">Play</a>
          <a href={appStoreUrl} target="_blank" rel="noreferrer">App</a>
        </div>
      </aside>
    </main>
  )
}
