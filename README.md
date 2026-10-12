# book-search-app
카카오 API를 이용한 도서 검색 앱입니다.

## UI (`:feature:book:presentation`)

| 파일 | 내용 |
| --- | --- |
| `search/SearchScreen.kt` | 검색 탭. 정확도순/발간일순 정렬, 다음 페이지 로딩·재시도 |
| `favorite/FavoriteScreen.kt` | 즐겨찾기 탭. 로컬 검색, 제목 오름/내림차순 정렬, 금액 필터(바텀시트) |
| `detail/BookDetailScreen.kt` | 도서 상세. 표지, 정보, 책 소개, 즐겨찾기 토글 |
| `component/` | 두 탭이 함께 쓰는 카드, 검색창, 정렬 버튼, 리스트, 로딩·빈 화면·에러 화면 |

### 결정한 것

- **화면 Composable은 상태만 받는다.** `XxxScreen(uiState, onXxx...)`는 ViewModel을 모르고, 화면 상태는 `XxxUiState` 하나로 받는다. 그래서 모든 상태(로딩, 빈 결과, 에러, 다음 페이지 실패, 태블릿)를 Preview로 바로 확인할 수 있다. ViewModel과 연결하는 진입점은 ViewModel 작업 때 추가한다.
- **화면용 모델 `BookUiModel`을 presentation에 둔다.** API 응답 DTO의 null 처리와 값 변환(할인가 -1 → null 등)을 끝낸 값만 Composable에 넘긴다. 변환 위치(ViewModel 또는 domain)는 data 레이어와 연결할 때 정한다.
- **다음 페이지 실패는 결과를 지우지 않는다.** 지하철처럼 연결이 끊기는 환경에서 이미 본 목록을 잃지 않도록, 첫 페이지 실패(`SearchResult.Error`)와 다음 페이지 실패(`AppendState.Error`)를 나눠 리스트 맨 아래에만 재시도 버튼을 띄운다.
- **태블릿·폴더블은 폭 기준으로 대응한다.** 리스트는 `GridCells.Adaptive(340.dp)`로 폭에 따라 열 수가 늘고, 상세 화면은 폭 600dp 이상이면 표지와 정보를 좌우로 놓는다. 검색창은 넓은 화면에서 최대 840dp로 제한한다.
- **이미지 로딩은 Coil 3**을 쓴다. Compose 전용 API(`AsyncImage`)가 있고, 프로젝트가 이미 쓰는 OkHttp를 네트워크 계층으로 쓸 수 있다. Glide는 Compose 지원이 별도 실험 모듈이라 고르지 않았다.
- **아이콘은 벡터 드로어블로 넣는다.** `material-icons-extended`는 수천 개 아이콘이 들어 있는 큰 라이브러리이고 더 이상 갱신되지 않아, 필요한 7개만 `res/drawable`에 넣었다.

### 아직 하지 않은 것

- 하단 탭과 화면 이동(Navigation)은 `:app`에서 조립한다. 태블릿에서는 하단 탭 대신 NavigationRail을 쓰는 것을 고려하고 있다.
- 테마와 공통 컴포넌트는 지금 `:feature:book:presentation` 안에만 있다. 다른 기능이 같은 컴포넌트를 쓰게 되면 `:core:designsystem`으로 옮긴다.
