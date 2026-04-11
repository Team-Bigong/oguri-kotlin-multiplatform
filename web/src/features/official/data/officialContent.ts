import { webEnvironment } from "../../../config/env"

export const playStoreUrl = webEnvironment.playStoreUrl.trim().length > 0
  ? webEnvironment.playStoreUrl
  : "https://play.google.com/store/apps"

export const appStoreUrl = webEnvironment.appStoreUrl.trim().length > 0
  ? webEnvironment.appStoreUrl
  : "https://apps.apple.com"

export const suggestionFormUrl = "https://docs.google.com/forms/d/e/1FAIpQLSdIebTYVf7Gy4XMCJtGaWO26UXMiBImWTDQbCVB2zywYMGV4g/viewform?usp=sharing&ouid=103643341635895040668"
export const termsOfServiceUrl = "https://wealthy-clematis-4a5.notion.site/31d5bca0ac22806aa8c4f6b0374c3a55?source=copy_link"
export const privacyPolicyUrl = "https://wealthy-clematis-4a5.notion.site/31d5bca0ac2280269bbcd799cdc73997?source=copy_link"

export const featureItems = [
  {
    title: "연차 조합 추천",
    description: "공휴일과 주말 사이\n더 길게 쉴 수 있는 조합을 먼저 보여줘요"
  },
  {
    title: "지금 가기 좋은 여행지",
    description: "연차를 바꿔보면서\n내 일정에 맞는 조합을 쉽게 비교할 수 있어요"
  },
  {
    title: "저장하고 다시 보기",
    description: "지금 가기 좋은 여행지를\n연휴 흐름에 맞춰 이어서 보여줘요"
  }
]

export const previewItems = [
  {
    title: "홈",
    subtitle: "추천 전략을 한눈에",
    imagePath: "/assets/official/screenshots/home-screen.png"
  },
  {
    title: "캘린더",
    subtitle: "연차별 조합 비교",
    imagePath: "/assets/official/screenshots/calendar-screen.png"
  },
  {
    title: "마이페이지",
    subtitle: "저장 일정 관리",
    imagePath: "/assets/official/screenshots/my-page-screen.png"
  }
]

export const frequentlyAskedQuestions = [
  {
    question: "오구리는 어떤 앱인가요?",
    answer: "연차와 공휴일 조합, 그리고 시기 맞춤 여행지를 한 흐름으로 추천해주는 앱이에요."
  },
  {
    question: "어떤 상황에서 오구리를 쓰면 좋아요?",
    answer: "연차 일수를 정하기 어렵거나, 지금 시기에 맞는 여행지를 빠르게 찾고 싶을 때 가장 유용해요."
  },
  {
    question: "어디서 설치할 수 있나요?",
    answer: "아직 출시 전이며, 2026년 4월 이내 출시를 목표로 준비하고 있어요."
  }
]
