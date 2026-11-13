# 오구리 Design System

<!-- design-md:section experience -->
## 1. Experience

<!-- design-md:claim scope kind=product-surface lang=en -->
### Scope

오구리는 연차 사용 대비 휴식 조합과 그 시기에 맞는 여행지를 추천하는 서비스다. 중심 경험은 **연차 설정 → 추천 기간 비교 → 여행지 탐색 → 저장·다시 보기**다. 여행 계획은 이 흐름을 실제 준비로 잇는 확장 방향이다.

**범위:** Android·iOS 모바일을 우선 기준으로 삼고, 공식 웹과 어드민의 차이를 함께 기록한다. 확인일은 2026. 11. 13.이며 로컬 커밋 `9c91736`과 현재 작업본을 대조했다. 앱·브라우저 실행과 배포 상태는 확인하지 않았다.

**표기:** `현재`는 코드에서 확인한 구현, `제안`은 여행 플래너 명세의 설계 방향, `보완 기준`은 구현·검증이 필요한 품질 조건이다. 제안과 보완 기준을 이미 제공되는 기능으로 홍보하지 않는다.

| 범위 | 주요 근거 |
| --- | --- |
| 제품·화면 | [MVP_WIREFRAME.md](./MVP_WIREFRAME.md), [TRAVEL_PLANNER_SPEC.md](./TRAVEL_PLANNER_SPEC.md) |
| 모바일 색·테마·서체 | [Color.kt](../composeApp/src/commonMain/kotlin/com/bigong/oguri/core/designsystem/Color.kt), [Theme.kt](../composeApp/src/commonMain/kotlin/com/bigong/oguri/core/designsystem/Theme.kt), [Typography.kt](../composeApp/src/commonMain/kotlin/com/bigong/oguri/core/designsystem/Typography.kt) |
| 모바일 구현 계약 | [MOBILE_CONVENTIONS.md](./MOBILE_CONVENTIONS.md), [MOBILE_CODE_STYLE.md](./MOBILE_CODE_STYLE.md), [MOBILE_PROJECT_STRUCTURE.md](./MOBILE_PROJECT_STRUCTURE.md) |
| 웹·서버 범위 | [WEB_OFFICIAL_SITE.md](./WEB_OFFICIAL_SITE.md), [WEB_ADMIN_SETUP.md](./WEB_ADMIN_SETUP.md), [GEMINI.md](./GEMINI.md) |
<!-- design-md:claim-end -->

<!-- design-md:claim primary-tasks kind=user-outcomes count=5 lang=en -->
### Primary tasks

- 현재: 홈에서 가장 좋은 휴가 전략을 먼저 보고 다른 순위와 비교한다.

- 현재: 캘린더에서 연차 일수와 기간을 바꾸며 추천 조합을 탐색한다.

- 현재: 기간에 맞는 도시를 살펴보고 휴가 일정·여행지를 저장하거나 다시 연다.

- 제안: 개인과 소규모 동행 그룹이 추천의 날짜·도시를 이어받아 실제 여행을 만들고 장소를 담는다.

- 제안: 같은 날짜의 지도·목록에서 방문 순서를 정리하고, 동행자와 편집한 계획을 다시 이어 본다.
<!-- design-md:claim-end -->

### 근거에서 도출한 디자인 결정

| 결정 | 기준 | 이유·근거 |
| --- | --- | --- |
| D01 · 추천을 먼저 | 홈의 최고 전략 1개에 가장 높은 위계 | 첫 결정을 줄인다. MVP 와이어프레임 |
| D02 · 기존 표현 유지 | 민트·주황, 밝은 중립색, Pretendard와 기존 캐릭터 활용 | 기존 화면과 연결된다. Color·Typography 및 공용 UI |
| D03 · 문맥 이어가기 | 추천 기간과 실제 여행 날짜를 구별하고 선택한 여행·일차를 보존 | 반복 입력과 잘못된 날짜 해석을 줄인다. 여행 플래너 명세 |
| D04 · 저장을 설명 | 저장 중·저장됨·실패·충돌을 구별 | 화면에 보이는 변경과 서버 확정을 혼동하지 않는다. 여행 플래너 명세 |
| D05 · 작업 공간 확보 | 지도·목록을 우선하고 캐릭터는 빈 상태·안내에 제한 | 장소·순서·메모가 잘 읽힌다. 여행 플래너 명세 3·6장 |

### Principles

- 제안은 짧게, 선택의 결과는 구체적으로 설명한다. 앱의 대표 표현은 “이번엔 이렇게 쉬어볼까요?”다.

- 저장한 추천 기간·저장한 도시·직접 만든 여행·여행 안의 방문 항목을 서로 다른 개념으로 다룬다.

- 현재 토큰을 재사용하되 접근성 미달 조합은 승인된 표준처럼 복제하지 않는다.

- 제안된 플래너는 지도 없이도 목록으로 조회·편집할 수 있게 하고, 빈 날짜를 미완료로 단정하지 않는다.

### Avoid

- 연차 조합 추천이라는 현재 포지셔닝을 흐리는 여행 커뮤니티·예약 중심 화면.

- 근거 없는 평점·방문자 수·리텐션 효과, 미구현 공동 편집·알림의 제공 약속.

- 여행 편집 목록의 장식 카드·캐릭터 반복, 모든 편집마다 뜨는 확인 팝업.

- 방문 순서 연결선을 실제 교통 경로나 이동시간으로 표현하는 방식.

<!-- design-md:section foundations -->
## 2. Foundations

<!-- design-md:claim foundations kind=rules-or-constraints lang=en -->
### Semantic tokens

- **color.mobile.dark.background-primary**: `#060B14` — D02 · DarkOguriColorPalette.backgroundPrimary의 현재 값. 팔레트 표의 역할·대비 제약을 함께 적용한다.
- **color.mobile.dark.mint**: `#5FB8A8` — D02 · DarkOguriColorPalette.mint의 현재 값. 팔레트 표의 역할·대비 제약을 함께 적용한다.
- **color.mobile.dark.mint-container**: `#14322D` — D02 · DarkOguriColorPalette.mintContainer의 현재 값. 팔레트 표의 역할·대비 제약을 함께 적용한다.
- **color.mobile.dark.orange**: `#F0AE67` — D02 · DarkOguriColorPalette.orange의 현재 값. 팔레트 표의 역할·대비 제약을 함께 적용한다.
- **color.mobile.dark.orange-container**: `#4A2C19` — D02 · DarkOguriColorPalette.orangeContainer의 현재 값. 팔레트 표의 역할·대비 제약을 함께 적용한다.
- **color.mobile.dark.text-on-mint**: `#FFFFFF` — D02 · DarkOguriColorPalette.textOnMint의 현재 값. 팔레트 표의 역할·대비 제약을 함께 적용한다.
- **color.mobile.dark.text-on-orange**: `#FFFFFF` — D02 · DarkOguriColorPalette.textOnOrange의 현재 값. 팔레트 표의 역할·대비 제약을 함께 적용한다.
- **color.mobile.dark.text-primary**: `#EDF3F8` — D02 · DarkOguriColorPalette.textPrimary의 현재 값. 팔레트 표의 역할·대비 제약을 함께 적용한다.
- **color.mobile.light.background-primary**: `#FFFFFF` — D02 · LightOguriColorPalette.backgroundPrimary의 현재 값. 팔레트 표의 역할·대비 제약을 함께 적용한다.
- **color.mobile.light.mint**: `#4FC3AF` — D02 · LightOguriColorPalette.mint의 현재 값. 팔레트 표의 역할·대비 제약을 함께 적용한다.
- **color.mobile.light.mint-container**: `#E6F7F4` — D02 · LightOguriColorPalette.mintContainer의 현재 값. 팔레트 표의 역할·대비 제약을 함께 적용한다.
- **color.mobile.light.orange**: `#FF6F3D` — D02 · LightOguriColorPalette.orange의 현재 값. 팔레트 표의 역할·대비 제약을 함께 적용한다.
- **color.mobile.light.orange-container**: `#FFE7DA` — D02 · LightOguriColorPalette.orangeContainer의 현재 값. 팔레트 표의 역할·대비 제약을 함께 적용한다.
- **color.mobile.light.text-on-mint**: `#FFFFFF` — D02 · LightOguriColorPalette.textOnMint의 현재 값. 팔레트 표의 역할·대비 제약을 함께 적용한다.
- **color.mobile.light.text-on-orange**: `#FFFFFF` — D02 · LightOguriColorPalette.textOnOrange의 현재 값. 팔레트 표의 역할·대비 제약을 함께 적용한다.
- **color.mobile.light.text-primary**: `#0F172A` — D02 · LightOguriColorPalette.textPrimary의 현재 값. 팔레트 표의 역할·대비 제약을 함께 적용한다.

### 토큰 읽는 법

`color.mobile.*`는 이 가이드에서 사용하는 문서용 경로다. 실제 Kotlin 코드는 아래 표의 속성을 `OguriTheme.colors`에서 읽고, 원본 값은 `LightOguriColorPalette`와 `DarkOguriColorPalette`에서 확인한다.

### 현재 모바일 의미 기반 팔레트

색상 원본은 [Color.kt](../composeApp/src/commonMain/kotlin/com/bigong/oguri/core/designsystem/Color.kt)다. 아래 HEX는 불투명 원본값이며 런타임의 투명도·합성 결과를 뜻하지 않는다. 현재 `OguriTheme.colors`와 기존 `Mint70`·`Neutral90` 계열 토큰이 함께 쓰인다.

| `OguriTheme.colors` 속성 | 라이트 | 다크 | 역할 |
| --- | --- | --- | --- |
| `mint` | #4FC3AF | #5FB8A8 | 주요 행동·선택 강조 |
| `mintContainer` | #E6F7F4 | #14322D | 민트 계열 보조 면 |
| `orange` | #FF6F3D | #F0AE67 | 보조 강조·휴일·주의가 필요한 정보. 오류 전용색과 구분 |
| `orangeContainer` | #FFE7DA | #4A2C19 | 주황 계열 보조 면 |
| `backgroundPrimary` | #FFFFFF | #060B14 | 화면 기본 배경 |
| `backgroundSecondary` | #F8FAFC | #0B1220 | 보조 배경 |
| `backgroundTertiary` | #F1F5F9 | #0F172A | 구분 영역 배경 |
| `surfaceCard` | #FFFFFF | #0D1626 | 카드 표면 |
| `surfaceRaised` | #FBFDFE | #121E31 | 높이가 구분되는 표면 |
| `borderSubtle` | #E2E8F0 | #172335 | 약한 경계 |
| `borderStrong` | #CBD5E1 | #223246 | 강한 경계. 조작 경계의 대비 충족 여부는 별도 검증 |
| `textPrimary` | #0F172A | #EDF3F8 | 제목·주요 정보 |
| `textSecondary` | #334155 | #C8D8E7 | 본문 보조 정보 |
| `textTertiary` | #64748B | #AFC3D7 | 부가 정보 |
| `textOnMint` | #FFFFFF | #FFFFFF | 현재 민트 위 텍스트. 대비 보완 대상 |
| `textOnOrange` | #FFFFFF | #FFFFFF | 현재 주황 위 텍스트. 대비 보완 대상 |
| `success` | #12B76A | #2ECC80 | 성공 상태 |
| `warning` | #F79009 | #FFBE5C | 주의 상태 |
| `error` | #D92D20 | #FF7B72 | 오류 상태 |
| `info` | #2E90FA | #82BEFF | 정보 상태 |

새 화면은 의미가 맞는 `OguriTheme.colors`를 우선 사용한다. 레거시 숫자 토큰도 테마에 따라 바뀌므로 라이트 HEX를 직접 복사하지 않는다. `Theme.kt`의 `dynamicColor` 인자만으로 시스템 동적 색상이 적용된다고 가정하지 않는다. 현재 함수는 자체 라이트·다크 팔레트를 선택한다.

### 색상 역할과 화면별 차이

- 민트는 주요 행동·선택에, 주황은 휴일·보조 강조에 사용한다. 성공·주의·오류·정보는 별도 의미 토큰과 문구로 전달한다.
- 캘린더의 추천 구간은 기존 기준인 비휴일 `Neutral70`, 휴일 `Orange50`, 일반 날짜 `Neutral50`를 따른다. 색만으로 날짜의 의미를 전달하지 않는다.
- 공식 웹은 네이비 히어로·푸른 강조·밝은 본문을 사용한다. 모바일 민트 팔레트와 동일한 구현으로 간주하지 않는다. 근거: [official.css](../web/src/features/official/official.css).

### 현재 색상 조합의 대비

sRGB 상대 휘도로 계산한 정적 토큰 비교다. 아래의 충족·미달은 일반 텍스트의 **4.5:1** 기준이며, 실제 모든 화면의 접근성 판정은 아니다. 수치는 소수 둘째 자리로 표시하지만 판정에는 반올림 전 값을 사용한다. 근거: [WCAG 텍스트 대비](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html).

| 테마 | 전경 / 배경 | 계산값 | 일반 텍스트 |
| --- | --- | --- | --- |
| 라이트 | `textPrimary` / `backgroundPrimary` | 17.85:1 | 충족 |
| 라이트 | `textSecondary` / `backgroundPrimary` | 10.35:1 | 충족 |
| 라이트 | `textTertiary` / `backgroundPrimary` | 4.76:1 | 충족 |
| 라이트 | `textOnMint` / `mint` | 2.15:1 | 미달 |
| 라이트 | `textOnOrange` / `orange` | 2.77:1 | 미달 |
| 다크 | `textPrimary` / `backgroundPrimary` | 17.62:1 | 충족 |
| 다크 | `textSecondary` / `backgroundPrimary` | 13.54:1 | 충족 |
| 다크 | `textTertiary` / `backgroundPrimary` | 10.89:1 | 충족 |
| 다크 | `textOnMint` / `mint` | 2.36:1 | 미달 |
| 다크 | `textOnOrange` / `orange` | 1.92:1 | 미달 |

**보완 기준:** 민트·주황 위 흰 글자 4개 조합은 모두 3:1에도 미달한다. 글자를 키우는 것만으로 해결하지 않는다. 버튼의 전경·배경을 한 쌍으로 조정한 뒤 라이트·다크에서 재검증한다. 이 문서의 현재 값 기록은 해당 조합의 사용 승인이 아니다.

반투명 스낵바·이미지 위 글자·경계·선택 표시·비활성 상태는 실제 합성 배경에서 별도 확인한다. 장식 경계와 조작에 꼭 필요한 경계를 같은 기준으로 판단하지 않는다.

### 현재 간격·모서리·깊이

| 용도 | 확인한 값 | 근거와 적용 |
| --- | --- | --- |
| 홈 좌우 여백 | 20dp | [HomeScreen.kt](../composeApp/src/commonMain/kotlin/com/bigong/oguri/feature/home/ui/HomeScreen.kt) |
| 홈 섹션 간격 | 주로 24·28dp, 내부 12·18dp | 현재 배치의 관찰값. 모든 화면의 공통 spacing 토큰으로 선언된 값은 아님 |
| 여행지 카드 | 모서리 8dp, 이미지 높이 134dp, 내부 좌우 12dp | [PlaceCard.kt](../composeApp/src/commonMain/kotlin/com/bigong/oguri/core/ui/component/PlaceCard.kt) |
| 입력 | 높이 48dp, 모서리 8dp, 경계 1dp, 라벨 아래 8dp | [LabeledTextField.kt](../composeApp/src/commonMain/kotlin/com/bigong/oguri/core/ui/component/LabeledTextField.kt) |
| 확인 팝업 | 모서리 8dp, 화면 좌우 16dp, 내부 20×24dp | [ConfirmAlertDialog.kt](../composeApp/src/commonMain/kotlin/com/bigong/oguri/core/ui/component/ConfirmAlertDialog.kt) |
| 스낵바 | 모서리 4dp, 내부 16×10dp | [OguriSnackBar.kt](../composeApp/src/commonMain/kotlin/com/bigong/oguri/core/ui/component/OguriSnackBar.kt) |
| 공통 그림자 | 기본 elevation 2dp, 다크에서 별도 그림자 계산 | [ModifierExtensions.kt](../composeApp/src/commonMain/kotlin/com/bigong/oguri/core/util/extension/ModifierExtensions.kt) |

### 모션과 피드백

- 현재 클릭은 `noRippleClickable`이 기본이며 ripple 표시가 없다. 저장 토글에는 선택 햅틱이 있다. 햅틱이나 색 변화만을 조작 결과의 유일한 신호로 사용하지 않는다.
- 현재 스켈레톤은 `SkeletonBox`와 1,100ms shimmer를 사용한다. 이는 로딩 표현이며 실제 진행률이 아니다.
- 보완 기준: 키보드 포커스·눌림·선택을 구별하고 시스템의 모션 감소 설정에서는 정적 로딩 표현을 제공한다. 기존 소스의 모션 감소 대응은 확인하지 못했다.
- 공통 모션 토큰과 플래너의 전환 시간은 확정하지 않았다. 이 문서에서 새 duration·easing 값을 만들지 않는다.
<!-- design-md:claim-end -->

<!-- design-md:section typography-assets -->
## 3. Typography & Assets

### Type roles

| Role | Usage | Family | Size | Weight | Line height | Tracking |
|---|---|---|---|---|---|---|
| heroTitle | 화면의 핵심 추천·주요 제목 | Pretendard | 28sp | 700 | 36sp | -0.01em |
| sectionTitle | 섹션 제목 | Pretendard | 22sp | 700 | 30sp | -0.01em |
| cardTitle | 카드 제목 | Pretendard | 18sp | 600 | 25sp | 0em |
| cardSubtitle | 카드 보조 제목 | Pretendard | 15sp | 500 | 22sp | -0.005em |
| bodyLarge | 기본 설명·입력 | Pretendard | 16sp | 400 | 24sp | 0em |
| bodyMedium | 보조 본문 | Pretendard | 14sp | 400 | 21sp | 0em |
| bodySmall | 짧은 보조 설명 | Pretendard | 13sp | 400 | 19sp | 0em |
| caption | 보조 수치·설명 | Pretendard | 12sp | 400 | 16sp | 0.01em |
| labelLarge | 주요 조작 라벨 | Pretendard | 14sp | 600 | 20sp | 0em |
| labelMedium | 짧은 라벨 | Pretendard | 12sp | 500 | 16sp | 0.01em |
| labelSmall | 부가 라벨 | Pretendard | 11sp | 400 | 14sp | 0.02em |

### 서체 적용

- 모바일 원본: [Typography.kt](../composeApp/src/commonMain/kotlin/com/bigong/oguri/core/designsystem/Typography.kt). 위 표의 크기·행간은 sp, 자간은 em이다. `OguriTheme.typography`의 역할을 선택하고 의미가 다른 스타일을 크기만 보고 재사용하지 않는다.
- 모바일에는 Pretendard Regular·Medium·SemiBold·Bold TTF가 포함되어 있다. 로드한 400·500·600·700 이외의 굵기와 합성 이탤릭을 기본 스타일로 추가하지 않는다.
- 공식 웹은 `Noto Sans KR`를 사용한다. 웹의 히어로는 `clamp(44px, 5.4vw, 78px)`·행간 1.04, 설명은 17~21px·행간 1.7이다. 모바일 typography 토큰을 웹의 현재 값으로 기재하지 않는다.
- `cardSubtitle`의 음수 자간, caption·작은 라벨의 양수 자간은 현재 구현값이다. 새 한글 본문에 관행처럼 복제하지 말고 실제 문장과 글자 확대에서 검토한다.

### 한글과 긴 문장

- 보완 기준: 제목·버튼·라벨은 어절 단위로 읽히게 하고 본문에는 충분한 행간을 둔다. 웹에서 `keep-all`을 선택하면 `overflow-wrap: anywhere`를 함께 적용한다. 한글 본문에 `break-all`을 일괄 적용하지 않는다.
- 모바일은 CSS 속성을 흉내 내지 않고 Compose의 줄바꿈·폭 제약과 큰 글자 설정으로 검증한다. 고정 높이 때문에 입력·오류·버튼 문구가 잘리면 높이가 늘어나는 구성을 검토한다.
- 핵심 날짜·연차·저장 상태·오류 문구는 말줄임으로 숨기지 않는다. 부가 요약의 말줄임은 상세 화면에서 전체 내용을 확인할 수 있을 때만 사용한다.
- 문자 크기와 화면 폭 검증은 실제 로캘·글자 확대를 기준으로 한다. CJK 글리프 fallback이 필요할 수 있으며 Pretendard만으로 일본어·중국어 전체 지원을 보장하지 않는다.

### 기존 에셋과 사용 위치

| 에셋 | 현재 위치 | 사용 기준 |
| --- | --- | --- |
| 앱 로고 | [OguriAppLogoImage.kt](../composeApp/src/commonMain/kotlin/com/bigong/oguri/core/ui/component/OguriAppLogoImage.kt) | 기존 리소스와 테마 분기를 재사용 |
| 캐릭터 | `composeResources/drawable/img_oguri_hello.png`, `img_oguri_empty.png`, `img_oguri_teacher.png` 등 | 인사·빈 상태·안내 중심. 작업 행마다 반복하지 않음 |
| 아이콘 | `composeResources/drawable/ic_*.xml`, `btn_*.xml` | 기존 벡터를 우선. 장식 아이콘과 조작의 접근성 이름을 구분 |
| 여행지 사진 | 기존 여행지 API의 썸네일·이미지 URL | 반환 데이터 사용. 임의 사진·좌표·후기·평점 생성 금지 |
| 공식 웹 화면 이미지 | [README.md](../web/public/assets/official/README.md) | 저장소의 소개용 이미지. 최신 실행 결과·운영 수치의 증거로 사용하지 않음 |

### 에셋 확인 범위

파일의 존재와 참조는 확인했다. 포함된 폰트의 정확한 버전, 이미지별 원권리자·허용 범위와 라이선스 고지의 완전성은 이 작업에서 검증하지 않았다. 새 에셋을 배포하거나 재가공할 때 해당 원본과 이용 조건을 확인한다.

<!-- design-md:section components-states -->
## 4. Components & States

### 공용 컴포넌트 재사용

모바일 공용 원본: [core/ui/component](../composeApp/src/commonMain/kotlin/com/bigong/oguri/core/ui/component). 화면 전용 카드는 각 feature의 `ui/component`에 두고 여러 화면에서 같은 책임을 맡을 때 공용화한다.

| 현재 컴포넌트 | 구성·값 | 확인한 동작과 보완점 |
| --- | --- | --- |
| `PlaceCard` | 사진 → 도시·국가 → 2줄 요약, 8dp 모서리 | 카드 클릭으로 상세 진입. 중첩 저장 조작은 별도 의미 제공 |
| `SaveToggleButton` | `checked`, `onCheckedChange`, 시각 크기 32dp | 선택·해제 아이콘과 햅틱. 저장 여부·행동 이름·실제 터치 영역을 보완 검증 |
| `LabeledTextField` | 라벨·값·placeholder·선택 단위, 48dp | 값과 변경 callback을 외부에서 전달. 오류·도움말과 label 연결은 별도 보완 |
| `ConfirmAlertDialog` | 아이콘·제목·설명·확정/취소, 8dp | 뒤로가기·바깥 누르기 닫힘. 파괴적 행동의 의미·포커스 복귀 검증 |
| `SkeletonBox` | 8dp 모서리·shimmer, 화면별 skeleton | 초기 로딩과 추가 페이지 로딩을 원래 콘텐츠 구조에 맞춤 |
| `NetworkErrorRetryContent` | 캐릭터·오류 문구·48dp 재시도 | 빈 결과와 요청 실패를 구분. 일부 영역 실패를 전체 화면 오류로 확대하지 않음 |
| `OguriSnackBarHost` | 단일 host, SUCCESS·ALERT·INFO | 탭·시스템 inset에 맞춘 위치. 현재 시각 구현은 message·type만 렌더링 |

**스낵바 제약:** 현재 `actionLabel`이 visuals에 존재하지만 `OguriSnackBar`에는 액션 버튼이 렌더링되지 않는다. 플래너의 `되돌리기`를 기존 스낵바가 지원한다고 가정하지 않는다. 링크: [OguriSnackBar.kt](../composeApp/src/commonMain/kotlin/com/bigong/oguri/core/ui/component/OguriSnackBar.kt).

### 상태 설계와 검증 기준

다음 표는 새 UI와 기존 UI 보완에 적용할 기준이다. 모든 상태가 현재 모든 컴포넌트에 구현되어 있다는 뜻은 아니다. 해당하지 않는 상태는 컴포넌트 책임과 함께 이유를 기록한다.

| 상태 | 적용할 기준 | 현재 확인 범위 |
| --- | --- | --- |
| default | 제목·주요 수치·행동을 먼저 읽고 선택된 항목을 식별 | 기본 레이아웃·토큰 구현 확인 |
| hover | 포인터를 지원하는 웹에서 조작 가능 여부를 알림. 터치 전용 상호작용에는 별도 hover를 강제하지 않음 | 공식 웹 스토어 버튼 hover 구현 확인 |
| focus-visible | 키보드·보조기기에서 현재 포커스를 구별하고 시트·팝업을 닫으면 호출 지점으로 복귀 | 전 화면 충족 여부는 검증 전 |
| disabled | 사유를 설명하고 활성·선택과 구별. 제출 중에는 같은 작업의 중복 실행을 방지 | 화면별 loading·submitting 관례 확인. 공용 버튼 상태 토큰은 미정 |
| loading | 초기는 skeleton, 다음 페이지는 append loading, 제출은 버튼·행동 단위 저장 중 | 초기·paging 로딩 관례 확인. 플래너 저장 중은 제안 |
| error | 실패한 영역·행동과 다음 행동을 제공. 입력과 기존 확정 데이터 보존 | 재시도 UI 구현 확인. 플래너 충돌·복구는 제안 |
| success | 서버 확인 뒤 결과를 표시하고 원래 목록·선택에도 반영 | 기존 저장 스낵바 있음. 플래너의 저장 확정·공동 반영은 제안 |

선택·눌림·비활성은 서로 다른 상태다. 선택은 색과 텍스트·아이콘·semantics로 함께 전달한다. 버튼의 이름은 결과를 설명하고, 장식 이미지는 별도 포커스 대상으로 만들지 않는다.

### 여행 플래너 컴포넌트 방향 · 제안

| 역할 | 표시·조작 | 지켜야 할 경계 |
| --- | --- | --- |
| 날짜 선택 | 전체 / 각 일차 / 날짜 없음 | 지도·목록이 같은 선택 상태 사용. 전체·날짜 없음의 선·번호 규칙 구분 |
| 방문 행·지도 핀 | 같은 방문 식별자, 방문 순서·장소·선택 시각·메모 유무 | 같은 장소 재방문은 별도 방문. 핀·목록 상호 선택 |
| 방문 편집 시트 | 시각·메모·날짜 이동·삭제, 적용 | 공유 메모임을 명시. 변경 중 입력과 저장 확정 상태 구분 |
| 순서 변경 | 손잡이 드래그 + 위로·아래로 메뉴 | 드래그 없이도 같은 결과. 시각만으로 자동 재정렬 금지 |
| 저장 상태 | 저장 중 / 저장됨 / 저장하지 못함 | 서버 확인 전 저장됨 금지. 충돌 시 최신 내용과 내 입력 보존 |
| 초대·변경 요약 | 멤버 버튼에서 참여·유효성 확인, 수정 시점·더보기에서 최근 변경 확인 | 링크 열기만으로 참여 처리 금지. 일정 내용은 권한 확인 뒤 노출. 요약 자동 팝업·미확인 개수 배지 없음 |
| 이어보기 | 여행명·기간과 계획을 여는 행동 | 타인의 수정 때문에 대상 여행을 계속 바꾸지 않음. 다음 할 일 자동 제안·빈 날짜 미완료 표시 없음 |

### 화면 상태와 동시성 계약

- `Route`는 lifecycle에 맞는 상태 수집·효과·내비게이션 연결을 맡고 `Screen`은 전달받은 상태와 callback으로 렌더링한다. 상태·이벤트의 흐름을 한 방향으로 유지한다.
- `UiState`는 불변 모델로 전달한다. 공용 컴포넌트 내부에 서버 저장이나 화면 이동을 숨기지 않는다. 목록에는 안정적인 방문 식별자를 사용하고, 재구성 최적화 annotation은 실제 불변·안정성 계약을 만족할 때만 붙인다.
- 현재 가이드의 `viewModelScope`, `collectAsStateWithLifecycle()` 관례를 유지한다. 취소를 일반 오류로 바꾸지 않고 관찰 작업은 화면·계정·여행 권한의 생명주기에 맞게 종료한다.
- 검색은 최신 입력 기준으로 오래된 요청·결과를 취소한다. 보기 상태 갱신과 유실되면 안 되는 편집 명령을 같은 버퍼·최신값 처리로 묶지 않는다. 저장·참여 확정 같은 중요한 결과를 일회성 이벤트만으로 보관하지 않는다.
- UI는 domain의 역할에 메시지를 보내고 data를 직접 참조하지 않는다. 지도 SDK·서버 응답 모델을 domain의 편집 규칙에 끌어들이지 않는다. 근거: 모바일 가이드와 여행 플래너 명세 13.3장.

<!-- design-md:section layout-platforms -->
## 5. Layout & Platforms

### 현재 화면 문법

| 화면 | 정보 위계·이동 | 상태·스크롤 기준 |
| --- | --- | --- |
| 홈 | 핵심 추천 → 순위 비교 → 여행지·인기 정보 → 캘린더 | 추천 1개 우선, 좌우 20dp, 하단 탭·광고·inset 영역 고려 |
| 캘린더 | 헤더·연차 선택 → 누적 추천 카드 | 한 번에 카드 1개 확장. 내부 달력 최소 3주. append loading·실패 분리 |
| 여행지·기간 상세 | 선택한 도시·기간의 정보 → 관련 항목·저장·공유 | 상세는 push. 기존 문맥 유지 |
| 마이페이지 | 회원/guest → 연차 설정·저장 목록 → 계정·문서 | 비회원 안내·빈 목록·불러오기 실패를 구분 |
| 공식 웹 | 다크 히어로 → 기능 → 화면 소개 → FAQ → 설치 CTA·푸터 | 모바일 UI와 별도 CSS. 섹션 앵커·스토어 연결 |
| 어드민 | 대상 목록·검색·선택 → 입력·이미지 관리 → 저장·삭제 | 업무용 밀도·정확성 우선. 소비자용 캐릭터를 폼마다 넣지 않음 |

### 여행 플래너 레이아웃 · 제안

- 기존 홈·캘린더·마이페이지 3개 탭을 유지한다. 플래너는 상세로 열고 편집 화면에 메인 하단 탭을 추가하지 않는다.
- 기본은 지도와 목록을 함께 보는 중간 높이 시트다. 접으면 지도, 펼치면 목록 중심으로 전환한다. 작은 화면·큰 글자에서는 목록을 우선한다. 시트의 구체 높이·snap 지점은 아직 확정하지 않았다.
- 날짜·저장 상태·장소 추가는 목록 중심 상태에서도 접근 가능하게 한다. 지도 장애 때도 방문 목록·메모를 유지한다.
- 로그인·초대·외부 지도에서 돌아오면 원래 여행·일차·입력을 복원한다. 다른 멤버의 변경 때문에 현재 편집 포커스·지도 시점을 빼앗지 않는다.
- 플래너 진입·내부 편집·초대·로그인 복귀에 작업을 끊는 광고를 넣지 않는 방향이다. 현재 공통 광고 트리거의 제외 처리는 신규 요구사항이다.
- 출발 전 확인 알림은 기본 꺼짐이며 더보기에서 멤버 본인이 선택한 예약당 1회다. 생성·초대·편집 완료 때 신청을 자동 제안하지 않는다. 거절해도 계획 기능을 사용할 수 있고, 잠금 화면에는 여행 제목·상세 일정·동행자·메모를 기본 노출하지 않는다. 기간 변경·나가기·삭제 시 기존 예약을 취소한다.

### 폭·글자 확대 검증

보완 기준: 모바일 320·390dp 너비와 플랫폼의 큰 글자 설정에서 주요 정보·조작이 잘리지 않아야 한다. 웹은 320·390·1440 CSS px, 200% 확대와 320 CSS px로의 reflow를 확인한다. dp·pt·CSS px를 같은 물리 크기로 간주하지 않는다. 이 작업에서는 해당 실행 검증을 하지 않았다.

### Platform: android

- Compose Multiplatform 공통 UI를 사용한다. TalkBack의 읽기 순서·선택·오류 설명을 확인한다.
- 보완 기준: 터치 영역은 48×48dp 이상을 확보하고 이웃 영역과 겹치지 않게 한다. SaveToggleButton의 32dp는 시각 크기이며 실제 확장된 터치 영역은 기기에서 확인해야 한다. 근거: [Compose 접근성 기본값](https://developer.android.com/develop/ui/compose/accessibility/api-defaults).
- 키보드·시스템 바 inset과 뒤로가기를 고려한다. 지도 제스처·시트 드래그·목록 스크롤 충돌을 검증한다.

### Platform: ios

- VoiceOver·큰 글자·safe area·가장자리 뒤로가기와 시트 제스처를 검증한다. 플랫폼별 하단 내비게이션 차이를 공통 높이로 덮어쓰지 않는다.
- 보완 기준: 조작 목표는 44×44pt를 기본으로 삼는다. 이는 이 문서의 제품 목표이며 Apple HIG가 안내하는 기본 control 크기를 따른다. 작은 예외를 자동 승인하지 않는다. 근거: [Apple 접근성](https://developer.apple.com/design/human-interface-guidelines/accessibility).

### Platform: web

- 현재 공식 웹 컨테이너는 최대 1,180px, 기본 좌우 합산 여백 48px이다. 720px 이하에서는 합산 24px이다.
- 현재 1,100px 이하에서 히어로·소개 영역과 feature·preview grid가 1열로 바뀌고 상단 섹션 링크가 숨겨진다. 720px 이하에서 내비게이션이 2열이 되고 기기 비주얼과 푸터가 조정된다. 문구를 임의 축소해 맞추지 않는다.
- 현재 어드민은 너비 1,360px 이상을 wide desktop 기준으로 사용한다. 공식 웹의 breakpoint를 그대로 가져오지 않는다.
- 보완 기준: 네이티브 링크·버튼·입력의 의미를 보존하고 키보드·focus-visible·필드 오류 연결·모션 감소를 검증한다. 기존 반응형 CSS가 있다는 이유만으로 검증 완료로 표시하지 않는다.

<!-- design-md:section content-locales -->
## 6. Content & Locales

### 한국어 문체

- 제품 UI는 짧은 해요체와 부담 없는 제안형을 사용한다. 기준 표현은 `home_greeting_question`의 “이번엔 이렇게 쉬어볼까요?”다. 행동 버튼은 “저장하기”, “여행 만들기”처럼 결과가 보이는 동사형으로 쓴다.
- 한 문장에 한 가지 내용을 담는다. 빈 상태는 비어 있는 대상을, 오류는 실패한 행동과 가능한 다음 행동을 설명한다.
- 버튼·제목에는 마침표를 붙이지 않는다. 안내 문장은 문장부호를 일관되게 사용한다. 불필요한 느낌표·과장·완성 압박을 줄인다.

### 상태별 문구

| 상황 | 문구·행동 | 근거·범위 |
| --- | --- | --- |
| 홈 제안 | 이번엔 이렇게 쉬어볼까요? | 현재 `home_greeting_question` |
| 추천 실패 | 추천 전략을 불러오지 못했어요. 눌러서 다시 시도해 주세요. | 현재 `home_error_retry` |
| 저장 완료 | 일정을 저장했어요. | 현재 `snackbar_home_saved` |
| 저장 여행지 없음 | 아직 담아둔 여행지가 없어요. | 현재 `mypage_empty_saved_place` |
| 플래너 첫 장소 | 가고 싶은 곳부터 담아볼까요? | 제안. 필수 장소 개수를 강요하지 않음 |
| 플래너 저장 상태 | 저장 중 / 저장됨 / 저장하지 못함 | 제안. 상태 표시와 실제 서버 확정을 일치시킴 |
| 방문 순서 | 방문 순서 연결선 | 제안. 실제 길찾기·시간 계산과 구분 |
| 공동 변경 | 지난 방문 이후 변경이 있어요 · 변경 보기 | 제안. 목록 자동 갱신만으로 읽음 처리하지 않음 |

### 리소스·숫자·시간

- 모바일 노출 문자열은 `composeResources`에 둔다. 기본 `values`는 영어이며 `values-ko`, `values-ja`, `values-zh`를 함께 유지한다. 새 문자열은 모든 기존 언어 폴더에 같은 키를 추가한다.
- 공식 웹 문구·링크는 [officialContent.ts](../web/src/features/official/data/officialContent.ts)에 있다. 모바일 문자열 리소스와 저장 위치가 다르므로 각각 관리한다.
- 날짜·숫자는 로캘에 맞게 포맷한다. 연차 일수·전체 휴식 기간·실제 여행 날짜는 서로 바꿔 쓰지 않는다. 여러 해의 여행이 함께 보이면 연도를 식별할 수 있게 한다.
- 제안된 방문 시각은 여행의 기준 도시 현지 시각이다. 표시 언어와 시간대를 분리하고, 판별 실패 시 기기 시간대로 몰래 대체하지 않는다.
- 외부 링크를 누른 사실을 예약·구매·방문 완료로 표현하지 않는다. 기획 문서의 예시 날짜·장소 수를 실제 추천 결과로 사용하지 않는다.

### Terminology

| Term | Preferred form |
|---|---|
| 날짜 없음 | 제안: 여행 안에서 방문일이 정해지지 않은 장소. 전역 북마크와 섞지 않는다. |
| 내 여행 | 제안: 실제 날짜·도시·방문 계획·멤버를 가진 작업 공간. |
| 담아둔 여행지 | 관심 도시 북마크. 여행 안의 방문 장소와 구별한다. |
| 방문 순서 연결선 | 제안: 목록 순서를 표시하는 선. 도로 경로·이동시간·이동 가능 여부를 보장하지 않는다. |
| 방문 항목 | 제안: 특정 여행에서 한 번 방문하려는 의도. 같은 장소에 여러 방문 항목이 생길 수 있다. |
| 연차 | 사용할 휴가 일수. 공동 여행 생성·참여로 개인의 남은 연차를 자동 차감하지 않는다. |
| 저장한 휴가 일정 | 다시 볼 추천 기간. 공동 편집 여행 계획을 뜻하지 않는다. |
| 추천 기간 | 연차·주말·등록 공휴일로 구성한 휴식 후보. 실제 여행 날짜와 구별한다. |

### Locale: ko (supported)

- `values-ko` 리소스가 존재한다. 문구의 기준은 기존 제안형 해요체이며 큰 글자·실제 줄바꿈은 실행 검증 대상이다.

### Locale: en (supported)

- 기본 `values` 리소스가 존재한다. 한국어 문장 길이에 맞춘 고정 폭·말줄임을 그대로 강제하지 않는다.

### Locale: ja (supported)

- `values-ja` 리소스가 존재한다. 글리프 fallback·읽기 순서·실제 번역 품질의 전수 검증은 하지 않았다.

### Locale: zh (supported)

- `values-zh` 리소스가 존재한다. 이 폴더만으로 간체·번체·지역별 로캘을 각각 지원한다고 단정하지 않는다.

<!-- design-md:section governance -->
## 7. Governance

<!-- design-md:claim authority kind=evidence-backed-reconstruction lang=en -->
### Authority

This document is an evidence-backed reconstruction, not authority for an unrelated target project.
<!-- design-md:claim-end -->

<!-- design-md:claim application-priority order=prompt-fact,repository-fact,system-contract,reference-inspiration lang=en -->
### Application priority

1. Direct user instructions for the requested scope.
2. Repository facts.
3. This system contract.
4. Reference inspiration.
<!-- design-md:claim-end -->

<!-- design-md:claim unknowns policy=absent-at-smallest-unresolved-boundary lang=en -->
### Unknowns

Omit only the smallest unresolved value or group. Do not replace it with a plausible default.
<!-- design-md:claim-end -->

<!-- design-md:claim changes policy=review-record-validate-before-adoption lang=en -->
### Changes

Record, review, and validate changes before adoption.
<!-- design-md:claim-end -->

### Project priority details

1. 요청 범위와 사용자 지침을 우선하고 기존 제품의 확인된 행동을 유지한다.

2. 현재 값은 디자인 시스템·공용 컴포넌트의 구현을 기준으로 확인한다. 기획 문서는 신규 방향의 근거이며 구현 완료의 증거가 아니다.

3. 웹과 모바일의 스타일 차이를 먼저 드러내고, 하나를 다른 플랫폼의 사실로 덮어쓰지 않는다.

4. 외부 브랜드의 토큰을 추가하지 않는다. 이 문서는 오구리 저장소에서 재구성한 가이드다.

### 문서와 구현 변경

- 색·서체·컴포넌트 변경은 원본 위치와 영향을 기록한 뒤 이 문서를 함께 갱신한다. 접근성 보완과 브랜드 변경을 구별해 검토한다.
- 공통 컴포넌트는 상태·이벤트·접근성 책임을 문서화한 뒤 수정한다. 화면 편의를 위해 내부 데이터를 꺼내 도메인 규칙을 다시 판단하지 않는다.
- 서버는 기존 단순 layered 구조와 JPA Entity 가이드를 따른다. 디자인 문서만으로 서버 전체의 domain 분리나 모듈 전환을 요구하지 않는다. 구체 충돌이 생기면 변경 범위와 대안을 별도 검토한다.
- 새 수치·정책·기능을 추가할 때 현재·제안·검증 완료를 구분한다. 실제 사용자 수·전환율·접근성 적합 판정은 근거 없이 기재하지 않는다.

### 남아 있는 결정과 보완

| 항목 | 확인한 상태·다음 판단 |
| --- | --- |
| 브랜드 대비 | 민트·주황 위 흰 글자 조합의 대비 미달. 전경·배경 쌍 수정과 실제 화면 검증 필요 |
| 공용 상태·모션 | 버튼 전체 상태·공통 duration/easing·모션 감소 대응은 통일된 구현으로 확인하지 못함 |
| 웹·모바일 차이 | 모바일 Pretendard·민트와 공식 웹 Noto Sans KR·네이비/푸른 강조가 공존. 통합 목표는 아직 없음 |
| 플래너 구체 값 | 지도 공급자 통합·출처 표시, 시트 높이·핀 색, 동기화 기술·지연 목표·운영 상한은 확정 전 |
| 권한·보관 | 초대·공동 편집·선택 알림은 신규 요구사항. 개인정보·기기 보관·로그아웃 정리 정책 검증 필요 |
| 에셋·실행 품질 | 라이선스 완전성·기기 접근성·320 폭·200% 확대·배포 동작은 미검증 |

### UI 변경 시 완료 확인

- 라이트·다크에서 텍스트·조작 경계·선택 표시를 실제 배경과 함께 확인한다.
- Android·iOS의 스크린리더, 터치 영역, 키보드·시트·뒤로가기, 큰 글자와 좁은 화면을 확인한다.
- 로딩·빈 상태·오류·성공·권한 종료를 분리하고 저장 결과와 표시를 일치시킨다.
- 제안된 플래너는 지도 없이 순서 변경·날짜 이동·메모 편집을 할 수 있고 공동 변경·충돌 때 입력과 권한을 보존해야 한다.
- 현재 문서 검증과 실제 UI 검증을 구별한다. 문서 구조·링크·토큰 대조가 통과해도 화면 접근성·폰트 라이선스·동시 편집 품질의 통과를 뜻하지 않는다.

### 공식 참고 자료

확인일: 2026. 11. 13.

- [Compose 접근성 기본값](https://developer.android.com/develop/ui/compose/accessibility/api-defaults): 48dp 터치 목표와 기본 semantics·터치 영역 확장.
- [WCAG 텍스트 대비](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html): 일반·큰 텍스트 대비와 전경·배경 조합 평가.
- [Apple 접근성](https://developer.apple.com/design/human-interface-guidelines/accessibility): 큰 글자·색 이외의 구별·제스처 대안·기본 조작 크기.
