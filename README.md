# 도서 검색 앱

카카오 도서 검색 API로 책을 검색하고, 마음에 드는 책을 즐겨찾기에 저장해 다시 찾아볼 수 있는 Android 앱입니다.

- **검색 탭**: 검색어로 도서를 찾고 정확도순 · 발간일순으로 정렬합니다. 스크롤하면 다음 페이지를 이어서 불러옵니다.
- **즐겨찾기 탭**: 저장한 도서를 검색어 · 제목 정렬 · 금액 범위로 찾아봅니다. 네트워크가 없어도 동작합니다.
- **상세 화면**: 도서 정보와 소개를 보고 즐겨찾기를 추가 · 해제합니다.

이 앱은 "큰 앱의 기능 하나이고, 앞으로 기능과 개발자가 계속 늘어난다"는 전제로 구조를 잡았습니다. 이 저장소에서 작업할 때 지키는 규칙은 [CLAUDE.md](CLAUDE.md)에 정리되어 있습니다.

## 목차
1. [빌드 방법](#빌드-방법)
2. [사용 기술](#사용-기술)
3. [프로젝트 구조](#프로젝트-구조)
4. [주요 구현 포인트](#주요-구현-포인트)
   - [아키텍처](#1-아키텍처)
   - [Compose 리컴포지션 최적화](#2-compose-리컴포지션-최적화)
   - [페이징 처리](#3-페이징-처리)
   - [큰 화면 대응](#4-큰-화면-대응)
5. [테스트](#테스트)
6. [AI 활용](#ai-활용)
7. [고려했지만 반영하지 않은 것](#고려했지만-반영하지-않은-것)

---

## 빌드 방법

### 요구 환경
| 항목 | 버전 |
|---|---|
| JDK | 17 |
| Gradle | 9.4.1 (Wrapper 포함) |
| Android Gradle Plugin | 9.2.1 |
| compileSdk / targetSdk | 37 |
| minSdk | 24 |

Gradle 데몬은 `gradle/gradle-daemon-jvm.properties` 설정에 따라 JDK 17로 실행됩니다.

### 1. 카카오 REST API 키 설정
프로젝트 루트의 `local.properties`에 키를 추가합니다. 키는 코드에 넣지 않고, 빌드할 때 `:core:network`의 `BuildConfig.KAKAO_API_KEY`로 들어갑니다. `local.properties`는 git에 올라가지 않습니다.

```properties
KAKAO_API_KEY=발급받은_REST_API_키
```

키가 없어도 빌드는 성공하지만, 검색 요청이 인증 오류(401)로 실패합니다.

### 2. 빌드 · 실행
```bash
./gradlew assembleDebug        # 디버그 APK 빌드 → app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug         # 연결된 기기에 설치
./gradlew testDebugUnitTest    # 단위 테스트 실행
```

---

## 사용 기술

| 분류 | 기술 | 버전 | 용도 |
|---|---|---|---|
| 언어 | Kotlin | 2.4.0 | |
| 비동기 | Coroutines · Flow | 1.10.2 | 비동기 처리, 화면 상태(`StateFlow`) |
| UI | Jetpack Compose (BOM) | 2026.02.01 | Material3, `material3-adaptive-navigation-suite`(하단 탭 바 ↔ 레일) |
| 내비게이션 | Navigation3 | 1.2.0 | 탭별 백스택, 화면 이동 |
| | Material3 Adaptive Navigation3 | 1.3.0 | 넓은 화면의 목록 · 상세 두 칸(`ListDetailSceneStrategy`) |
| 생명주기 | Lifecycle | 2.11.0 | `lifecycle-viewmodel-navigation3`(화면별 ViewModel 범위), `collectAsStateWithLifecycle` |
| DI | Hilt (KSP) | 2.60.1 | 의존성 주입. kapt 대신 KSP 사용 |
| | AndroidX Hilt | 1.3.0 | `hiltViewModel`, 상세 화면 AssistedInject |
| 네트워크 | Retrofit2 · OkHttp | 2.12.0 · 4.12.0 | 카카오 도서 검색 API 호출 |
| 직렬화 | kotlinx.serialization | 1.11.0 | JSON 파싱, Navigation3 키 직렬화 |
| 로컬 DB | Room | 2.8.5 | 즐겨찾기 저장 |
| 이미지 | Coil 3 | 3.3.0 | 표지 이미지 로딩 · 캐시 |
| 테스트 | JUnit4, kotlinx-coroutines-test, testFixtures | | 단위 테스트, 모듈 간 Fake 공유 |

라이브러리 버전은 모두 `gradle/libs.versions.toml`(Version Catalog)에서 관리합니다.

---

## 프로젝트 구조

### 모듈 구조
기능은 **제품 영역 단위**(`:feature:book`)로 모듈을 나누고, 그 안을 다시 레이어(presentation / domain / data)로 나눴습니다. 여러 기능이 함께 쓰는 인프라는 `:core`에 둡니다.

```mermaid
flowchart TD
    app[":app<br/>Application · MainActivity<br/>탭 · 내비게이션 조립"]

    subgraph feature_book [":feature:book (도서 영역)"]
        presentation[":presentation<br/>검색 · 즐겨찾기 · 상세 화면<br/>ViewModel · Paginator"]
        domain[":domain<br/>UseCase · Book 모델<br/>DTO / Entity → Book 변환"]
        data[":data<br/>BookRepository<br/>Remote · Local 데이터소스 · BookApi"]
    end

    subgraph core [":core (공통 인프라)"]
        network[":core:network<br/>Retrofit · OkHttp<br/>인증 인터셉터 · ApiResponse"]
        database[":core:database<br/>Room DB · DAO · Entity"]
    end

    app --> presentation
    app --> network
    presentation --> domain
    domain --> data
    data --> network
    data --> database
```

- 화살표는 의존 방향입니다. 항상 **UI → Domain → Data** 한 방향이고, 반대 방향 의존은 없습니다.
- 다른 기능이 생기면 `:feature:<영역>:{presentation, domain, data}` 형태로 추가합니다. 기능 간 화면 이동은 `:app`의 내비게이션에서 조립하고, 한 기능의 presentation이 다른 기능의 presentation을 직접 알지 않습니다.

### 모듈별 역할
```
app/                              Application, MainActivity, 탭 정의(TopLevelDestination),
                                  탭별 백스택(BookSearchNavigationState), NavDisplay · 탭 바 조립(BookSearchApp)
core/
├── network/                      Retrofit · OkHttp 설정, KakaoAK 인증 헤더 인터셉터,
│                                 ApiResponse(성공 / HttpError / NetworkError / UnknownError)와 CallAdapter
└── database/                     Room DB, FavoriteBookEntity · FavoriteBookDao,
                                  즐겨찾기 테이블 변경 신호(@FavoriteTableChanges)
    └── testFixtures/             FakeFavoriteBookDao
feature/book/
├── data/                         BookRepository, BookRemoteDataSource(BookApi), BookLocalDataSource, DTO
│   └── testFixtures/             FakeBookApi, testBookRepository()
├── domain/                       Book 모델, DTO · Entity → Book 변환(ISBN13 정규화),
│                                 SearchBooks · GetFavoriteBooks · ObserveFavoriteIsbns · SetFavorite UseCase
└── presentation/
    ├── search/ favorite/ detail/ 화면별 Route(ViewModel 연결) · Screen(상태만 받는 UI) · ViewModel · UiState
    ├── navigation/               NavKey, bookEntries()
    ├── paging/                   Paginator, PagingState, PageLoader
    ├── component/                BookCard · BookList · LoadMoreEffect 등 공통 UI
    └── model/                    BookUiModel, 공용 UiState, 정렬 · 필터
```

---

## 주요 구현 포인트

### 1. 아키텍처

#### Android 권장 아키텍처 + MVVM
[Android 권장 아키텍처](https://developer.android.com/topic/architecture)를 따릅니다.
- **근거**: 이 앱의 Repository는 구현이 하나뿐입니다. 인터페이스를 두면 파일과 바인딩이 늘어나고 이점이 없다고 판단했습니다. Android 공식 가이드도 Repository를 Data Layer에 두고, Domain Layer는 "선택"으로 둡니다.
- **domain은 필요할 때만 둡니다**: 비즈니스 로직(ISBN 정규화, 마지막 페이지 판단, 빈 검색어 처리)이 있는 곳만 UseCase로 만들었습니다. 로직 없이 넘기기만 하는 조회(예: 즐겨찾기 테이블 변경 신호)는 ViewModel이 Repository를 직접 씁니다.

#### 기능 모듈을 화면이 아니라 제품 영역 단위로
검색 · 즐겨찾기 · 상세를 화면별 모듈 3개로 나누지 않고 `:feature:book` 하나로 묶었습니다. 추후 여러 기능이 확장될 것을 고려하여 단위를 정했습니다.
- **근거**: 세 화면이 같은 `Book` 모델, 같은 Repository, 같은 즐겨찾기 저장소를 씁니다. 화면별로 나누면 검색 화면의 즐겨찾기 토글, 두 탭에서 들어가는 상세 화면 때문에 처음부터 기능끼리 서로 의존하게 됩니다.

#### 불안정한 네트워크를 전제로 한 결과 타입
지하철처럼 연결이 자주 끊기는 환경을 고려해, Retrofit 호출이 예외를 던지지 않고 `ApiResponse`(Success / HttpError / NetworkError / UnknownError)를 돌려주도록 CallAdapter를 만들었습니다.
- 화면은 실패 종류에 따라 문구를 고르고 "다시 시도"를 보여줍니다. 서버가 준 에러 메시지는 디버깅용으로만 `HttpError`에 남깁니다.
- 연결 · 읽기 타임아웃은 10초입니다. 즐겨찾기와 상세 화면은 로컬 DB와 내비게이션 키만 쓰므로 오프라인에서도 동작합니다.

#### DTO는 너그럽게, 앱 모델은 정규화하여 명확하게 구현
- **DTO**: API 문서에 required 여부가 명시되어 있지 않아 모든 필드를 기본값 없는 nullable로 두고, JSON 설정에 `explicitNulls = false`를 씁니다. 외부 서버는 필드를 항상 준다고 보장하지 않고, non-null 필드에 null이 하나만 와도 **응답 전체** 파싱이 실패하기 때문입니다.
- **앱 모델(`Book`)**: 변환할 때 필수 값(ISBN, 제목)이 없는 항목은 버리고, 가격 `-1` · 빈 썸네일은 null로 바꿉니다.
- **ISBN13 정규화**: 카카오 API는 `isbn`에 "ISBN10 ISBN13"을 공백으로 이어 주거나 하나만 줍니다. 어떤 조합이 와도 같은 책이 같은 키를 갖도록 ISBN13 하나로 맞춥니다(ISBN10은 978 + 체크 숫자 재계산). 이 값을 화면 키, 즐겨찾기 DB 기본키, 중복 제거 기준으로 함께 씁니다.

#### 화면 상태와 생명주기
- ViewModel은 화면 상태를 `StateFlow<UiState>` 하나로 내보내고, 화면은 `collectAsStateWithLifecycle()`로 받습니다.
- 화면 진입점(`*Route`)만 ViewModel을 알고, `*Screen`부터 그 아래 Composable은 상태와 콜백만 받습니다(state hoisting). 그래서 Preview를 상태별로 그릴 수 있습니다.
- **화면이 보일 때만 동작**: 입력 debounce 후 자동 검색과 즐겨찾기 다시 불러오기를 `init`에서 시작하지 않고 `uiState`(`SharingStarted.WhileSubscribed(5_000)`)에 묶었습니다. 다른 탭에 있으면 멈추고, 돌아오면 최신 상태로 한 번 맞춥니다.

#### 화면 간 즐겨찾기 동기화
즐겨찾기는 Room을 단일 진실 공급원(SSOT)으로 두고, 각 화면이 필요한 만큼만 구독합니다.
- **검색 · 상세**: 화면에 있는 책의 ISBN만 `isbn IN (...)`으로 구독합니다. SQLite 쿼리 인자 한도(3.32.0 미만은 999개) 때문에 500개씩 나눠 묻습니다.
- **즐겨찾기 목록**: 커서 페이지 조회는 한 번 읽는 함수라 변경을 알려주지 않습니다. 그래서 Room `invalidationTracker`로 테이블 변경 신호를 받아 다시 불러옵니다. 어느 화면에서 바꾸든(Repository를 거치지 않는 변경이 생기더라도) 반영됩니다.

#### 내비게이션 (Navigation3)
- 탭마다 백스택을 따로 둬서, 탭을 오가도 각 탭에서 연 상세 화면과 스크롤 위치가 남습니다.
- 상세 화면 키(`BookDetailNavKey`)에 `Book`을 담아 넘깁니다. **근거**: 카카오 도서 API에는 단건 조회가 없어 ISBN만으로는 상세 정보를 다시 가져올 수 없습니다. 키가 직렬화되므로 프로세스가 종료돼도 백스택과 함께 복원됩니다. 즐겨찾기 여부는 바뀔 수 있어 키에 담지 않고 상세 화면이 구독합니다.
- 화면을 열 때 정해지는 `Book`은 Hilt가 알 수 없어서 AssistedInject로 ViewModel에 전달합니다. Navigation3는 키를 `SavedStateHandle`에 넣어 주지 않기 때문입니다.

### 2. Compose 리컴포지션 최적화

**검증 방법**: release 빌드에서 Compose 컴파일러 리포트를 켜고 결과를 확인했습니다(확인 후 설정은 되돌림).

| 항목 | 결과 |
|---|---|
| Strong Skipping | 켜짐 |
| 다시 시작할 수 있는 Composable 중 건너뛸 수 없는(non-skippable) 것 | **0개** |
| 람다 | 124개 모두 자동 memoize |
| 불안정한 파라미터 | `BookDetailRoute(book: Book)` 1개. 다른 모듈의 domain 모델이라 불안정으로 보지만, Strong Skipping에서는 같은 객체인지로 비교하고 NavKey의 같은 객체가 계속 들어오므로 실제로는 건너뜀 |

**구조로 막은 불필요한 리컴포지션**
- **UI 모델 안정성**: `BookUiModel`, `SearchUiState`, `FavoriteUiState`를 `@Immutable`로 표시해 stable로 다룹니다.
- **검색어 입력 중 목록은 다시 그리지 않음**: 입력할 때 바뀌는 건 `query`뿐이고 결과(`result`)는 같은 객체라, 목록(`BookList`)은 건너뜁니다.
- **하트를 누르면 그 카드만 다시 그림**: 즐겨찾기가 바뀌면 목록은 새 리스트가 되지만, 카드는 `BookUiModel`을 `equals`로 비교해 바뀐 카드만 다시 그립니다.
- **즐겨찾기 재구독 최소화**: 검색 결과의 ISBN 목록에 `distinctUntilChanged()`를 걸어, 다음 페이지 로딩 표시처럼 책 목록이 그대로인 상태 변화에는 DB 구독을 다시 맺지 않습니다.
- **하트 깜빡임 방지**: 새 페이지가 먼저 도착하고 그 책들의 즐겨찾기 여부가 아직 없을 때 화면을 갱신하면 하트가 꺼졌다 켜집니다. `combineTransform`으로 두 값이 맞을 때만 상태를 내보냅니다.
- **라이브러리 동작 확인**: Navigation3는 백스택이 바뀔 때만 항목을 다시 만든다는 것(소스 확인), 썸네일 placeholder의 `ColorPainter`가 `equals`를 구현한다는 것(바이트코드 확인)을 확인해 추가 `remember`가 필요 없다고 판단했습니다.

### 3. 페이징 처리

#### 검색: 페이지 번호 방식
- 카카오 API에 한 페이지 20권씩 요청합니다.
- 실제 API를 호출해 보며 문서에 없는 동작을 찾아 반영했습니다.
  - 결과가 0건이어도 `is_end`가 `false`로 옵니다 → `is_end`, 결과 개수, 요청한 페이지를 함께 보고 마지막 페이지를 판단합니다.
  - 50페이지를 넘어도 200으로 응답합니다 → 50페이지에서 직접 멈춥니다.
  - 빈 검색어는 항상 0건입니다 → 요청하지 않고, 검색 전 화면에 추천 검색어를 보여줍니다.
- **ISBN 기준 중복 제거**: 실제 API를 검색어 6개 × 정렬 2개 × 최대 5페이지로 조회해 보니, 같은 책이 판매처별로 따로 등록돼(ISBN 같음, 출판사 · 가격 다름) 같은 페이지 안이나 페이지 사이에 중복으로 왔습니다. 목록이 ISBN을 키로 쓰기 때문에 걸러내지 않으면 앱이 종료됩니다.
- **요청 관리**: 입력이 0.5초 멈추면 자동 검색하고, 같은 조건은 다시 요청하지 않으며, 새 검색을 시작하면 진행 중인 요청을 취소합니다. 실패한 검색은 사용자가 직접(키보드 검색 키 · 추천 검색어 · 다시 시도) 요청할 때만 다시 보냅니다.

#### 즐겨찾기: 커서(keyset) 방식
- `OFFSET`은 페이지가 뒤로 갈수록 앞의 행을 다시 세야 하고, 페이지를 넘기는 사이 추가 · 삭제가 일어나면 항목이 빠지거나 중복됩니다. 그래서 직전 페이지의 마지막 책 `(title, isbn)` 다음부터 읽는 커서 방식으로 구현했습니다.
- **인덱스 `(title, isbn)`**: `EXPLAIN QUERY PLAN`으로 확인했습니다. `title` 인덱스만 있으면 같은 제목끼리 따로 정렬하는 단계(`USE TEMP B-TREE`)가 생기고, `(title, isbn)` 인덱스에서는 이 단계가 사라집니다.

#### 페이징 로직 추상화: `Paginator` + `LoadMoreEffect`
처음에는 두 ViewModel이 페이지 관리를 각자 구현했고, "목록 끝에서 다음 페이지 불러오기"가 `BookList` 안에 묶여 있어 다른 목록에서 다시 쓸 수 없었습니다. 이를 두 층으로 나눴습니다.

- **`Paginator<Key, Item>`** (ViewModel 쪽)
  - 목록 누적, 다음 페이지 키, 중복 제거, 마지막 페이지, 진행 중 요청 취소, 실패 후 재시도를 한 곳에서 처리합니다.
  - 키를 제네릭으로 둬서 검색(페이지 번호)과 즐겨찾기(커서)가 같은 클래스를 씁니다. 무엇을 불러올지(검색어 · 조건)는 `PageLoader`로 받고, 언제 첫 페이지를 불러올지는 ViewModel이 `refresh()`로 정합니다.
- **`LazyGridState.LoadMoreEffect`** (UI 쪽)
  - 어떤 `LazyVerticalGrid`에도 붙일 수 있습니다.
  - 항목 수가 바뀔 때마다 다시 판단해, 다음 페이지를 붙인 뒤에도 끝 근처면 이어서 불러옵니다. 이전에는 검색만 우연히 이렇게 동작하고 즐겨찾기는 멈출 수 있었습니다.
- 리팩터링 전에 작성한 ViewModel 테스트 20개가 수정 없이 통과해, 화면 동작이 그대로임을 확인했습니다.

### 4. 큰 화면 대응

| 화면 | 휴대폰 | 펼친 폴더블 (두 칸) | 태블릿 가로 |
|---|---|---|---|
| 검색 · 상세 | 📷 *스크린샷 추가 예정*<br>`docs/screenshots/phone_search.png` | 📷 *스크린샷 추가 예정*<br>`docs/screenshots/foldable_list_detail.png` | 📷 *스크린샷 추가 예정*<br>`docs/screenshots/tablet_list_detail.png` |
| 즐겨찾기 | 📷 *스크린샷 추가 예정*<br>`docs/screenshots/phone_favorite.png` | 📷 *스크린샷 추가 예정*<br>`docs/screenshots/foldable_favorite.png` | 📷 *스크린샷 추가 예정*<br>`docs/screenshots/tablet_favorite.png` |

<!-- 스크린샷을 추가하면 위 셀의 안내 문구를 <img src="docs/screenshots/파일명.png" width="240"/> 형태로 바꿔 주세요. -->

#### 화면 너비에 따라 바뀌는 내비게이션
`NavigationSuiteScaffold`로 휴대폰은 하단 탭 바, 펼친 폴더블 · 태블릿은 왼쪽 레일을 씁니다.

#### 목록 · 상세 두 칸 (List-Detail)
Navigation3의 `ListDetailSceneStrategy`로, 넓은 화면(600dp 이상)에서는 왼쪽에 목록, 오른쪽에 상세를 함께 보여줍니다.
- **상세는 교체**: 두 칸에서 다른 책을 누를 때마다 상세가 쌓이면 뒤로 가기를 여러 번 눌러야 합니다. 그래서 상세 위에서 다른 책을 열면 교체합니다. 휴대폰에서는 상세가 목록을 덮어 상세 위에서 다른 책을 누를 수 없으므로 동작이 그대로입니다.
- **현재 탭의 스택만 표시**: 라이브러리 소스를 확인해 보니, 이 전략은 백스택 끝에서부터 칸 정보가 있는 항목을 모두 모읍니다. 두 탭의 스택을 이어 붙이면 즐겨찾기 목록 옆에 검색 탭에서 연 상세가 묶일 수 있어, 현재 탭의 스택만 넘기고 "즐겨찾기 첫 화면에서 뒤로 가기 → 검색 탭"은 `BackHandler`로 처리했습니다.
- 두 칸으로 보일 때는 상세가 열려 있어도 탭 바 · 레일을 유지하고, 아직 고른 책이 없으면 오른쪽 칸에 안내 문구를 보여줍니다.

#### 화면 너비에 맞추는 목록 · 상세 레이아웃
- 목록은 카드 최소 너비 340dp의 `GridCells.Adaptive`라, 너비에 따라 열 수가 바뀝니다.
- 상세 화면은 자기 영역 너비가 600dp 이상이면 표지와 정보를 좌우 두 단으로 나눕니다.
- 검색창 영역은 최대 너비를 840dp로 제한해, 넓은 화면에서 지나치게 길어지지 않게 했습니다.

#### 그 밖의 UX 다듬기
- **탭 전환 애니메이션 제거**: 전환 전후 화면이 속한 탭의 첫 화면이 다르면 탭 전환으로 보고 애니메이션 없이 바꿉니다. 같은 탭 안의 목록 ↔ 상세 전환은 기본 애니메이션을 유지합니다.
- **헤더 그림자**: Surface의 elevation 그림자는 페이드 전환 중에 그려지지 않다가 늦게 나타나고, 좌우로 번져 레일 옆에 선처럼 보였습니다. 그래서 헤더 아래 그라데이션을 직접 그립니다.

---

## 테스트

테스트는 깨질 수 있는 로직이 있는 곳에만 씁니다. 받은 인자를 그대로 넘기기만 하는 코드(로직 없는 Repository · 데이터소스)는 테스트하지 않습니다.

| 테스트 | 개수 | 확인하는 것 |
|---|---|---|
| `SearchViewModelTest` | 12 | debounce 후 자동 검색, 같은 조건 재요청 안 함, 실패 후 재시도, 실패해도 debounce로는 재요청 안 함, 새 검색 시 이전 결과 버림, 다음 페이지 · 중복 제거 · 마지막 페이지, 다음 페이지 실패 후 재시도, 검색어를 지워도 결과 유지, 즐겨찾기 반영 |
| `FavoriteViewModelTest` | 6 | 제목순 로딩, 조건 변경, 다른 화면의 추가 반영, 다음 페이지, 변경 시 불러온 개수만큼 다시 불러오기, 하트를 끄면 목록에서 제거 |
| `BookDetailViewModelTest` | 2 | 즐겨찾기 여부 반영, 추가 · 해제 |
| `PaginatorTest` | 6 | 페이지 누적 · 중복 제거, 중복 요청 방지, 첫 페이지 · 다음 페이지 실패 후 재시도, 다시 불러올 때 이전 요청 결과 버림, `keepItems` |
| `BookMapperTest` | 14 | 마지막 페이지 판단, 가격 · 썸네일 정리, ISBN13 정규화, 커서 생성 |
| `ApiResponseCallAdapterFactoryTest` | 5 | 성공, 실제 카카오 401 에러 본문, 읽을 수 없는 에러 본문, 응답 없음, 파싱 실패 (MockWebServer) |

**ViewModel 테스트 방식**: 실제 UseCase · Repository · 데이터소스를 그대로 쓰고, 맨 아래의 `BookApi`와 DAO만 Fake로 바꿉니다.
- Repository와 데이터소스는 `internal`이라 다른 모듈에서 만들 수 없습니다. 그래서 같은 모듈의 internal에 접근할 수 있는 **testFixtures**에 `FakeBookApi`, `FakeFavoriteBookDao`, `testBookRepository()`를 두고 presentation 테스트가 가져다 씁니다. 운영 코드의 공개 범위는 넓히지 않았습니다.
- RoomDatabase는 JVM 단위 테스트에서 만들 수 없어서, 데이터소스가 DB 대신 테이블 변경 신호(`@FavoriteTableChanges Flow<Unit>`)만 주입받도록 바꿨습니다.

---

## AI 활용

### 사용한 도구와 방식
- **Claude Code**로 설계 검토, 코드 작성, 리팩터링, 테스트 작성, PR 본문 작성을 함께 했습니다.
- **Notion MCP**로 과제 요구사항을 읽어 구현 체크리스트를 만들고, 작업마다 "AI가 한 일 / 검증 방법 / 내가 바꾼 부분"을 기록했습니다. 이 섹션은 그 기록을 정리한 것입니다.
- 작업 규칙(아키텍처 · 모듈 · 테스트 기준)은 [CLAUDE.md](CLAUDE.md)에 적어 두고, AI가 매번 같은 기준으로 작업하게 했습니다. 작업하면서 정한 규칙도 이 문서에 계속 추가했습니다.

### AI 결과를 검증한 방법
AI의 답을 그대로 받아들이지 않고, 아래 방법으로 직접 확인하거나 확인하게 했습니다.

| 방법 | 예 |
|---|---|
| 실제 API 호출 | 문서에 없는 동작(0건인데 `is_end=false`, 50페이지 초과 200 응답, 판매가 `-1`), 빈 검색어 0건, 판매처별 중복 등록 확인 |
| 실제 SQL 실행 | 생성된 스키마로 sqlite3 DB를 만들어 커서 페이징의 누락 · 중복, `EXPLAIN QUERY PLAN`, 다시 불러오기 비용 측정 |
| 라이브러리 소스 · 생성 코드 확인 | Navigation3 · adaptive-navigation3 · NavigationSuiteScaffold 소스, Hilt 생성 코드, Compose 컴파일러 리포트 |
| 빌드 · 테스트 | 커밋마다 그 커밋 내용만으로 빌드 · 테스트 확인. 버그 수정은 수정을 되돌렸을 때 테스트가 실제로 실패하는지까지 확인 |
| 실제 기기 | Galaxy Z Fold6에서 두 칸 화면, 시스템 바 가림, 키보드, 탭 전환 확인 |

### 작업별 AI 활용과 내가 추가로 요청 · 변경한 부분
"AI가 한 일"은 AI가 제안하거나 구현한 내용이고, "추가 요청 · 변경"은 AI의 답을 그대로 쓰지 않고 내가 판단해 바꾸거나 더 요청한 내용입니다.

| 작업 | AI가 한 일 | 추가 요청 · 변경 |
|---|---|---|
| **과제 분석 · 빌드 환경** | 과제 요구사항을 체크리스트로 정리. 요구 버전(Gradle 9.4.1 · AGP 9.2.1 · Kotlin 2.4.0 · JDK 17)과 다른 설정을 찾아 맞추고, AGP를 낮추며 생긴 빌드 오류 해결 | 체크리스트 검토 · 수정. `.idea`를 초기 커밋 히스토리에서도 빼도록 요청하고, 모든 커밋은 사전 확인을 받도록 작업 규칙을 정함. API 키 이름을 `KAKAO_API_KEY`로 변경 |
| **모듈 구조 · 아키텍처** | 멀티 모듈 · MVVM · Hilt 방향으로 CLAUDE.md 초안 작성 | ① AI가 화면 단위 모듈 3개(search / favorite / detail)를 제안 → 같은 모델 · 저장소를 쓰는 **제품 영역 `:feature:book` 하나**로 변경 ② AI가 클린 아키텍처식 의존성 역전(domain이 Repository 인터페이스 소유)으로 작성 → **Android 권장 아키텍처**에 맞게 domain이 data에 의존하도록 수정 |
| **네트워크 · API 응답** | Retrofit · OkHttp · 인증 인터셉터 구성, `ApiResponse`와 CallAdapter 구현 | ① API 결과를 `ApiResponse` sealed interface로 감싸도록 설계 변경 지시 ② 서버 에러 메시지 대신 실패 타입으로 UI 문구를 정하고, 서버 메시지는 디버깅용으로만 남기도록 결정 ③ 구현이 하나뿐인 데이터소스 · Repository의 인터페이스를 없애도록 결정 ④ 필수로 둔 `sort · page · size`가 API 스펙상 선택값임을 짚어 nullable로 변경 ⑤ "모든 클래스에 테스트를 넣는 게 좋은 코드인가"를 문제 제기해 위임 코드 테스트를 삭제하고 테스트 기준을 CLAUDE.md에 추가 |
| **DTO 타입** | 실제 응답 600건을 조사(키 누락 · null 0건)해 non-null을 추천 | "null이 오지 않는다는 보장이 없다"는 점을 짚어 **전부 nullable 유지**로 결정. 기본값 남발을 지적해 기본값을 없애고 `explicitNulls = false` 적용 |
| **즐겨찾기 저장소 · 페이징** | Room 엔티티 · DAO 작성. OFFSET과 커서 페이징의 차이 설명, LIKE 이스케이프 추가 | ① DB를 `:core:database`에 두기로 결정(AI 추천은 data 모듈) ② AI가 Paging 3로 구현하려던 것을 중단시키고 **직접 구현**하기로 결정 ③ 즐겨찾기 전체를 Flow로 관찰하자는 제안에 "대규모 앱이면 수천 건이 넘을 수 있다"는 점을 짚어 **DB 페이징 유지** ④ 커서 페이징 방식 선택 |
| **domain · ISBN** | DTO · 엔티티 → `Book` 변환, ISBN13 정규화 구현. SQLite 인자 한도 근거(공식 문서) 조사 | ① 클래스 이름(`BookSearchPage`) 변경 요청 ② `ApiResponse`를 풀었다 다시 감싸는 코드가 어색하다고 지적 → `map` 추가 ③ AI가 추가한 한 줄짜리 헬퍼 테스트를 과하다고 보고 삭제 ④ ISBN10/13 처리 방식을 질문해 ISBN13 정규화로 결정 |
| **UI · 공용 UiState** | 검색 · 즐겨찾기 · 상세 화면, 공통 컴포넌트, Preview 28개 작성 | ① 화면별 상태 대신 Idle · Loading · Success · Error **공용 `UiState`**를 쓰도록 요청 ② AI가 `:core:ui`에 둔 `UiState`를 지금은 쓰는 기능이 하나뿐이라 feature 모듈로 옮기도록 결정 ③ 하트 버튼의 원형 배경 제거 요청 |
| **내비게이션 (Navigation3)** | 탭별 백스택, 하단 탭 바 ↔ 레일, 상세 이동 구현. Navigation 2와의 차이 설명 | ① 상세 데이터는 ISBN 재조회 대신 NavKey에 담는 방식 선택 ② Route를 Screen 파일과 분리하도록 요청 ③ 탭 전환 애니메이션을 없애도록 요청 |
| **ViewModel 연결** | 세 화면의 ViewModel 작성. 즐겨찾기 테이블 변경 감지, AssistedInject, `combineTransform` 등 설계 근거 설명 | ① presentation의 data 직접 의존이 필요 없다고 지적 ② 숫자 상수화, 정렬 변환을 enum으로 이동, `AppendState.canLoadMore` 추출 등 가독성 개선 제안 ③ 초기 화면이 비어 보인다고 보고 **추천 검색어** 요청 ④ 검색어를 지우면 이전 결과를 **유지**하도록 제안 ⑤ `init`에서 collect해도 되는지 문제 제기 → **화면이 보일 때만 동작**하도록 변경 ⑥ 재시도 시 화면이 덜컹거린다고 보고, AI가 제안한 최소 로딩 시간은 빼고 푸터 높이만 맞추도록 범위 지정 ⑦ 함수 이름(`observeFavoriteTableChanges`), 주석을 쉽게 쓰도록 여러 번 요청 |
| **큰 화면 · Edge-to-edge** | 리스트-상세 두 칸 구현. 라이브러리 소스로 다른 탭 상세가 묶이는 문제를 찾아 현재 탭 스택만 표시. 연결된 기기의 화면 크기를 읽어 Fold6이 두 칸이 안 되는 원인 발견. inset 처리 | ① 공식 문서처럼 목록 · 상세를 나란히 볼 수 있는지 먼저 질문 ② 상세는 **교체** 방식 선택 ③ Fold6에서 두 칸이 안 나오는 것과 펼쳤을 때 내비게이션 바가 가려지는 것을 **직접 발견해 보고** ④ 두 곳에서 여백을 주는 이유를 물어 **한 곳으로 모으도록** 결정 ⑤ 키보드가 올라올 때 화면이 움직이는 게 어색하다고 보고 → 키보드를 여백에서 제외 |
| **ViewModel 테스트** | 테스트를 막는 지점 3가지를 짚고 testFixtures 설계 제안. 작은 실험으로 가능 여부를 먼저 확인한 뒤 구현 | 설계안을 검토 · 승인. 테스트가 찾은 "실패하면 debounce가 한 번 더 요청" 문제에서 AI가 "검색 버튼"이라고 잘못 설명한 것을 짚어 **이 앱에는 검색 버튼이 없고 키보드 검색 키**라고 바로잡음. 수정 후 동작을 확인하고 승인 |
| **즐겨찾기 키 (id → isbn)** | id의 쓰임을 정리하고 isbn 기본키 추천. `EXPLAIN QUERY PLAN` 비교로 `(title, isbn)` 인덱스가 필요한 이유 설명. 처음엔 Room 마이그레이션까지 구현 | ① **자동 증가 id가 필요 없지 않냐고 직접 제안** ② 인덱스 · 문자열 커서 비교 · 넣는 순서 문제를 질문하며 검증 ③ 배포 전이라 마이그레이션은 필요 없다고 판단해 제거 ④ Fake DAO를 필터 → 정렬 순서로 정리 |
| **성능 · 페이징 추상화** | Compose 컴파일러 리포트 · 라이브러리 소스로 리컴포지션 검증. 페이징 트리거 문제 2가지 발견. `Paginator` · `LoadMoreEffect` 구현, 다시 불러오기 비용 측정 | ① 리컴포지션 · 페이징 효율 · **페이징 로직을 UI까지 건드리지 않고 재사용하는 방법**을 철저히 검토하도록 요청 ② 공용 모듈을 만들지 않고 feature 모듈에 두기로 결정 ③ `PagingState` 필드 이름 직접 변경 ④ 다시 불러오기 성능을 문제 제기 → 측정 결과를 보고 최적화는 넣지 않기로 결정 |
| **문서 · 코드 스타일** | 모든 주석을 KDoc으로 통일. PR 본문 작성 | 주석은 KDoc으로만, 쉬운 말로 쓰도록 규칙을 정함. 불필요하다고 본 주석은 직접 삭제. PR 본문에서 AI 생성 문구 제거. 커밋 · PR 메시지는 팀원이 읽기 쉬운 한국어로 |

### AI가 틀렸거나 부정확했던 부분
AI의 답이 틀렸던 경우도 그대로 남깁니다. 대부분 직접 확인하거나 질문하는 과정에서 드러났습니다.
- **중복 원인 추정**: AI가 주석에 "페이지 사이 검색 결과가 바뀌어 중복이 생긴다"고 적었습니다. 근거를 묻자 실제 API를 조회했고, 원인은 판매처별 중복 등록이었습니다. 주석을 고쳤습니다.
- **"검색 버튼" 설명**: 앱에 없는 검색 버튼을 기준으로 설명해, 키보드 검색 키로 바로잡았습니다.
- **인덱스 영향 과장**: isbn 기본키로 바꿀 때 "성능이 떨어지는 함정"이라고 설명했다가, 실제로는 같은 제목인 책끼리만 영향이 있는 작은 차이라고 정정했습니다.
- **휴대폰 뒤로 가기 영향**: 상세 교체 방식이 휴대폰 뒤로 가기에도 영향을 준다고 했다가, 휴대폰에서는 상세 위에서 다른 책을 누를 수 없어 영향이 없다고 정정했습니다.
- **테스트 기대값 · 설정 오류**: AI가 쓴 테스트가 처음에 실패한 일부는 동작 문제가 아니라 테스트 기대값이나 테스트 설정(`advanceUntilIdle`과 `backgroundScope`) 문제였습니다.

---

## 고려했지만 반영하지 않은 것

| 고려한 것 | 반영하지 않은 이유 | 다시 검토할 시점 |
|---|---|---|
| 화면 단위 기능 모듈(search / favorite / detail) | 세 화면이 같은 모델 · 저장소를 써서 처음부터 기능끼리 서로 의존하게 됨 | 즐겨찾기가 여러 영역(숙소 · 도서 등)을 함께 다루는 앱 전체 기능이 될 때 별도 모듈이나 `:core`로 분리 |
| 클린 아키텍처식 의존성 역전(domain이 Repository 인터페이스 소유) | Android 권장 아키텍처를 따르기로 함. 구현이 하나뿐인 Repository에 인터페이스를 두면 파일만 늘어남 | Repository 구현이 둘 이상 필요해질 때 |
| Repository · UseCase 인터페이스 추상화, mock 라이브러리 | 테스트만을 위한 추상화가 됨. Retrofit 서비스 · DAO를 Fake로 바꾸고 testFixtures로 공유하는 방식으로 충분 | — |
| Paging 3 (+ RemoteMediator) | 페이징은 라이브러리 없이 직접 구현하기로 결정. 이후 페이지 번호(검색)와 커서(즐겨찾기)를 함께 다루는 `Paginator`로 정리함. 지금 바꾸려면 즐겨찾기 상태 결합 · 테스트 방식도 다시 짜야 함 | 검색 결과 로컬 캐시 · 오프라인 열람이 필요해질 때 |
| 검색 결과 로컬 캐시(오프라인에서 마지막 결과 열람) | 즐겨찾기 · 상세는 이미 오프라인에서 동작. 검색 결과 캐시는 캐시 무효화 정책까지 함께 설계해야 해서 범위에서 제외 | 지하철처럼 오프라인 사용이 잦다는 근거가 생길 때 (Paging 3 RemoteMediator와 함께 검토) |
| 즐겨찾기 전체를 Flow로 관찰 / 저장소에서 인메모리로 관리 | 즐겨찾기가 수천 건이 되면 매번 전체를 읽게 됨. Room을 기준 저장소로 두고 필요한 ISBN만 구독하는 쪽이 확장에 유리 | — |
| 즐겨찾기 해제를 메모리에서 처리(다시 읽기 대신) | 측정상 비용이 작고(2,000권 16.7ms, 백그라운드), Repository를 거치지 않는 변경을 놓칠 수 있음 | 즐겨찾기를 수백 페이지씩 보는 패턴이나 저사양 기기 지연이 확인될 때 |
| DTO 필드 non-null | 서버가 null을 주지 않는다는 보장이 없고, null 하나에 응답 전체 파싱이 실패함 | — |
| 공용 모듈(`:core:ui`, `:core:common`) | `UiState` · `Paginator` · `LoadMoreEffect`를 쓰는 기능이 아직 하나뿐 | 다른 기능 모듈이 같은 코드를 쓰게 될 때 `:core`로 이동 |
| Robolectric + 실제 Room으로 ViewModel 테스트 | 테스트가 무겁고 느려짐. ViewModel 테스트의 목적은 상태 변화 확인이고, SQL은 sqlite3로 따로 검증함 | DAO 쿼리 자체를 자동 테스트로 지켜야 할 때 |
| 재시도 시 로딩 표시 최소 시간 | 로딩/실패 푸터 높이를 맞추는 것으로 덜컹거림을 해결 | — |
| 목록 여백에 키보드 높이 포함 | 키보드가 올라올 때 가운데 정렬 화면이 움직임 | 키보드를 연 채 목록 끝까지 보는 사용이 많을 때 목록에만 따로 적용 |
| 즐겨찾기 DB 마이그레이션(id → isbn) | 아직 배포 전이라 버전 1 스키마를 그대로 수정 | 배포 후 스키마가 바뀔 때는 버전을 올리고 마이그레이션 작성 |
| 즐겨찾기 "최근 추가순" 정렬용 `createdAt` 컬럼 | 지금 필요한 정렬이 아님 | 추가순 정렬이 필요해질 때(id에 기대지 않고 명시적인 컬럼으로) |
| 카카오 API 한 페이지 50권 요청 | 요청 수가 줄고 도달 가능한 결과가 2,500권으로 늘지만, 요구 사항이 20개 단위 | — |
| release 빌드에서 Preview 코드 제거(R8 범위 확장, Preview를 `src/debug`로 이동) | Preview 샘플 데이터가 release APK에도 남는 것을 확인했지만 보류 | 앱 크기 최적화가 필요할 때 |
