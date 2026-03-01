# MOBILE_PROJECT_STRUCTURE

## 현재 기준 디렉터리 원칙

```text
composeApp/src/commonMain/kotlin/com/bigong/oguri
├─ core
│  ├─ di
│  ├─ navigation
│  ├─ designsystem
│  ├─ network
│  ├─ platform
│  ├─ ui
│  │  └─ component
│  └─ util
├─ data
│  ├─ remote
│  │  └─ model
│  │     ├─ request
│  │     └─ response
│  └─ repository
├─ domain
│  ├─ model
│  ├─ repository
│  └─ usecase
└─ feature
   ├─ splash
   │  └─ ui
   ├─ login
   │  └─ ui
   ├─ home
   │  ├─ navigation
   │  └─ ui
   │     ├─ component
   │     └─ model
   ├─ calendar
   │  ├─ navigation
   │  └─ ui
   └─ mypage
      ├─ navigation
      └─ ui
```

## 레이어 규칙
- `data`는 `ui`를 import하지 않는다.
- `ui`는 `data`를 import하지 않는다.
- `domain`은 외부 레이어에 의존하지 않는다.

## UI 파일 구성
- `Route` 파일과 `Screen` 파일은 분리한다.
- `ViewModel`은 `ui` 패키지에 별도 파일로 둔다.
- `UiState`는 `ui/model`에 둔다.
- 재사용 컴포넌트는 `ui/component`에 둔다.
- `Screen` 파일은 가능한 가볍게 유지하고, 복잡한 UI는 `ui/component`로 이동한다.
- 각 컴포넌트는 개별 프리뷰를 제공한다. (`commonMain` 불가 시 `androidMain` 프리뷰 파일 사용)

## 리소스
- 문자열/이미지는 `composeResources`를 사용한다.
- 원격 이미지 URL도 리소스(`urls.xml`) 키 기반으로 관리한다.
