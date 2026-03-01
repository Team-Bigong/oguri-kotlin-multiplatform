# MOBILE_CODE_STYLE

## 핵심 원칙
- 코드 가독성, 예측 가능성, 변경 용이성을 우선한다.
- UI 문자열은 리소스로 관리한다.
- 하드코딩 매직 넘버는 상수화한다.

## Kotlin 네이밍
- 클래스/인터페이스: `PascalCase`
- 함수/변수: `camelCase`
- 상수: `UPPER_SNAKE_CASE`
- 축약어 사용 금지 (`msg`, `cnt`, `btn` 지양)
- `XxxList` 네이밍 지양
: 예) `rankLabels`, `hotelImageUrls` 사용
- 단, UseCase 이름은 `List` 접미사 허용
: 예) `GetRecommendPeriodListUseCase`

## 타입 선언 규칙
- 프로퍼티/파라미터/반환 타입은 명시한다.
- 같은 함수 내부의 지역 변수는 타입 추론을 사용한다.

## 문자열/상수
- 사용자 노출 문자열은 `composeResources` 문자열 리소스로 관리한다.
- 상수는 `const val`을 사용한다.

## Compose UI 규칙
- `Route`와 `Screen`은 반드시 별도 파일로 분리한다.
- `Route`는 상태 연결/이벤트 위임 담당.
- `Screen`은 순수 UI 렌더링 담당.
- `Screen` 파일은 200줄 이상이 되지 않도록 유지하고, 복잡한 UI는 `ui/component`로 분리한다.
- `Screen`은 레이아웃 조합과 상태 분기만 담당하고, 카드/섹션/버튼은 컴포넌트로 위임한다.
- 각 `Screen`과 각 `ui/component` 컴포넌트는 프리뷰를 제공한다.
- 공통 소스셋 제한으로 `commonMain` 프리뷰가 어려운 경우 `androidMain` 프리뷰 파일로 대체한다.
- 클릭은 기본적으로 `noRippleClickable`을 사용한다.
- 혼합 스타일 텍스트는 `getStyledText`만 사용한다. (`getColoredText` 사용 금지)

## 패키지 규칙
- `presentation` 패키지를 사용하지 않는다.
- 화면 코드는 `ui` 하위에 둔다.
- `UiState`는 `ui/model`에 둔다.
- 화면 내부 컴포넌트는 `ui/component`에 둔다.
- `data` 레이어 네트워크 모델은 `model/request`, `model/response`로 분리한다.
- 광고 모델의 `url`은 목적지 링크로 사용하고, 광고 이미지 선택은 UI 레이어에서 `platform` 기준으로 처리한다.
