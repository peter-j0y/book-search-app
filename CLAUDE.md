# CLAUDE.md

이 파일은 이 저장소에서 작업하는 AI 도구와 팀원을 위한 가이드입니다.

## 프로젝트 개요

카카오 도서 검색 API를 이용한 도서 검색 앱입니다. 지금은 기능이 몇 개 없지만, **큰 앱의 기능 하나이고 앞으로 기능과 개발자가 계속 늘어난다**고 보고 구조를 잡습니다.

- 화면: 하단 탭 2개(검색 리스트 / 즐겨찾기 리스트)와 도서 상세 화면
- 기술 스택: Kotlin, Coroutine, Flow, Jetpack Compose
- 아키텍처: 멀티 모듈 + MVVM + [Android 권장 아키텍처](https://developer.android.com/topic/architecture)
- DI: Hilt
- 네트워크: Retrofit2

## 빌드 조건 (변경 금지)

과제 제출 조건이므로 아래 버전을 바꾸지 않습니다.

- Gradle 9.4.1 / AGP 9.2.1 / Kotlin 2.4.0 / KSP 2.3.10
- JDK 17, compileSdk 37
- `./gradlew assembleDebug`가 별도 설정 없이 성공해야 합니다.
- API 키는 `local.properties`의 `KAKAO_API_KEY`에서 읽어 `BuildConfig`로 주입합니다. 키를 코드에 하드코딩하지 않습니다.
- 라이브러리 버전은 `gradle/libs.versions.toml`(Version Catalog)에서만 관리합니다.

## 아키텍처

[Android 권장 아키텍처](https://developer.android.com/topic/architecture)를 따릅니다. **클린 아키텍처가 아닙니다.** domain이 Repository 인터페이스를 갖고 data가 구현하는 의존성 역전 구조를 쓰지 않습니다.

```
UI Layer (presentation) ──▶ Domain Layer (선택) ──▶ Data Layer
         └──────────────────────────────────────────▲   # domain이 없으면 UI가 data에 직접 의존
```

- 의존 방향은 항상 **UI → Domain → Data**입니다. 반대 방향 의존은 없습니다.
- Repository는 Data Layer에 있으며, 앱의 나머지 부분에 데이터를 노출하는 진입점입니다.
- Domain Layer는 UI와 Data 사이에서 비즈니스 로직을 재사용하기 위한 선택 레이어입니다.

## 모듈 구조

기능 단위로 모듈을 나누고, 각 기능 안에서 다시 레이어별 모듈로 나눕니다.

```
:app                          # Application, MainActivity, 최상위 Navigation, Hilt 진입점
:core:network                 # Retrofit/OkHttp 설정, 공통 인터셉터(인증 헤더 등)
:core:designsystem            # 테마, 공통 Compose 컴포넌트
:core:...                     # 그 밖에 여러 기능이 같이 쓰는 인프라

:feature:book:presentation    # 검색 탭, 즐겨찾기 탭, 상세 화면 + ViewModel
:feature:book:domain          # UseCase (선택)
:feature:book:data            # BookRepository, Kakao API, 즐겨찾기 로컬 저장소
```

### 기능 모듈을 나누는 기준

기능 모듈은 **화면 단위가 아니라 제품 영역(한 팀이 맡는 영역) 단위**로 나눕니다.

- 검색·즐겨찾기·상세는 모두 "도서" 영역에 속하고, 같은 Book 모델·Repository·즐겨찾기 저장소를 씁니다. 그래서 `:feature:book` 하나로 묶습니다.
- 화면별로 나누면(`search` / `favorite` / `detail`) 검색 화면의 즐겨찾기 토글, 두 탭에서 들어가는 상세 화면 때문에 처음부터 기능 간 의존이 생깁니다.
- 즐겨찾기가 앱 전체 기능이 되면(숙소·도서 등을 한곳에서 찜하기) 그때 별도 기능 모듈이나 `:core`로 꺼냅니다.
- 새 제품 영역은 `:feature:<영역>:{presentation,domain,data}` 형태로 추가합니다.

### domain 모듈은 선택

모든 기능에 domain 모듈을 만들지 않습니다.

- **만드는 경우**: 여러 Repository를 조합하는 로직이 있거나, 같은 비즈니스 로직을 여러 ViewModel에서 재사용할 때
- **만들지 않는 경우**: ViewModel이 Repository를 호출하기만 하면 되는 단순한 경우. 이때 presentation이 data에 직접 의존합니다.
- domain 모듈이 있어도 단순 조회까지 UseCase로 감쌀 필요는 없습니다. ViewModel은 UseCase와 Repository를 함께 쓸 수 있습니다.

### 모듈 의존성 규칙

- `presentation` → `domain`(있으면), `data`
- `domain` → `data`
- `data` → `:core:*`
- 한 기능의 `presentation`은 다른 기능의 `presentation`에 의존하지 않습니다. 기능 간 화면 이동은 `:app`의 Navigation에서 조립합니다.
- 다른 기능의 데이터가 필요하면 그 기능의 `data`(또는 `domain`)가 공개한 API에만 의존합니다. 외부에 공개할 필요 없는 클래스는 `internal`로 숨깁니다.
- 순환 의존이 생기면 공통 부분을 `:core`로 내립니다.
- `:app`만 모든 모듈을 알 수 있습니다.

## 레이어별 규칙

### UI Layer (presentation, MVVM)

- ViewModel은 `@HiltViewModel`로 만들고, UI 상태를 `StateFlow<UiState>` 하나로 노출합니다.
- 일회성 이벤트(스낵바, 화면 이동 등)는 상태와 분리해서 전달합니다.
- Composable은 상태를 받고 이벤트를 콜백으로 올리는 형태(state hoisting)로 작성합니다. 화면 진입점 Composable만 ViewModel을 알고, 하위 Composable은 ViewModel을 모릅니다.
- 상태 수집은 `collectAsStateWithLifecycle()`을 사용합니다.
- 태블릿·폴더블을 고려해 화면 크기에 따라 레이아웃이 바뀌도록 작성합니다.

### Domain Layer (선택)

- UseCase는 하나의 동작만 담당하고 `operator fun invoke()`로 호출합니다.
- UseCase는 Repository나 다른 UseCase에 의존할 수 있고, 상태를 갖지 않습니다.

### Data Layer

- Repository가 해당 데이터의 단일 진실 공급원(SSOT) 역할을 하고, 원격·로컬 데이터소스를 조율합니다.
- 데이터소스(API 서비스, 로컬 저장소)는 Repository만 사용합니다. 상위 레이어는 데이터소스에 직접 접근하지 않습니다.
- 네트워크가 불안정한 환경(예: 지하철)을 고려해 예외를 그대로 던지지 않고 결과 타입으로 감싸서 올립니다. 재시도와 캐시 전략은 Repository에서 결정합니다.
- Repository는 데이터소스의 결과(API 응답 모델 등)를 변환 없이 내보냅니다. 화면에서 쓰는 모델(예: `Book`)로의 변환과 비즈니스 로직은 필요해질 때 domain 레이어에서 합니다.
- API 응답 DTO의 필드는 기본값 없이 모두 nullable로 선언합니다. 외부 서버는 필드를 항상 준다고 보장하지 않고, non-null 필드에 null 하나만 와도 응답 전체 파싱이 실패하기 때문입니다. 키가 빠진 경우도 null로 받도록 Json에 `explicitNulls = false`를 씁니다. null 처리(필수 값이 없는 항목 제외, 대체값 지정)는 앱 모델로 변환할 때 합니다.
- 비동기는 `suspend` 함수와 `Flow`로 처리하고, Dispatcher는 하드코딩하지 않고 주입받습니다.
- 구현이 하나뿐인 Repository와 데이터소스는 인터페이스로 추상화하지 않고 구현 클래스로 만듭니다. 테스트는 그 아래의 Retrofit 서비스 인터페이스를 가짜로 만들어 씁니다.

## 테스트

테스트는 깨질 수 있는 로직이 있는 곳에만 씁니다. 모든 클래스에 테스트를 넣지 않습니다.

- **쓰는 곳**: 분기·계산·변환 로직(예: 마지막 페이지 판단, DTO → 모델 변환, 정렬·필터), 여러 곳이 공유하는 인프라(예: `ApiResponse` CallAdapter), ViewModel의 상태 변화
- **쓰지 않는 곳**: 받은 인자를 그대로 넘기기만 하는 위임 코드(예: 로직 없는 Repository, 데이터소스). 이런 테스트는 구현에 묶여 시그니처가 바뀔 때마다 같이 고쳐야 하고, 잡아내는 버그가 없습니다.
- 라이브러리 자체의 동작(예: kotlinx.serialization의 기본값 처리)은 테스트하지 않습니다.
- 가짜 객체는 mock 라이브러리 대신 인터페이스를 직접 구현한 Fake 클래스로 만듭니다.

## 의존성 주입 (Hilt)

- Hilt 어노테이션 처리는 KSP를 사용합니다(kapt 사용 안 함).
- 각 모듈의 바인딩은 그 모듈 안의 `di` 패키지에 `@Module`로 둡니다.
  - 인터페이스 바인딩은 `@Binds`, 외부 라이브러리 객체 생성은 `@Provides`를 사용합니다.
- Retrofit, OkHttp 같은 싱글톤은 `:core:network`에서 `SingletonComponent`로 제공합니다.

## 네트워크 (Retrofit2)

- 카카오 API의 기본 URL과 인증 헤더(`Authorization: KakaoAK {KEY}`)는 `:core:network`의 인터셉터에서 붙입니다.
- API 서비스 인터페이스와 DTO는 해당 기능의 `data` 모듈에 둡니다.

## 문서화

- 이 코드를 이어받을 팀원이 "무엇을 왜 이렇게 만들었는지" 알 수 있어야 합니다. 구조나 기술을 결정하면 근거와, 고려했지만 하지 않은 대안을 README 등 저장소 안에 남깁니다.
- 공개 GitHub 저장소에 올릴 수 있으므로 지원 회사명을 코드, 문서, 커밋 메시지에 넣지 않습니다.
