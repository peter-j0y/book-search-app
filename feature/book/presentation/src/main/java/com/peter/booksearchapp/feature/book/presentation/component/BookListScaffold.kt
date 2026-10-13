package com.peter.booksearchapp.feature.book.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.onConsumedWindowInsetsChanged
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 검색 · 즐겨찾기 목록 화면의 공통 틀. 위에 제목 · 검색창 · 정렬 영역을 두고, 그 아래에 [content]를 그립니다.
 *
 * 시스템 바 · 카메라 홀을 피하기 위한 여백을 여기서 한 번만 계산합니다.
 * 키보드는 넣지 않습니다. 넣으면 키보드가 올라올 때마다 안내 문구 같은 가운데 정렬 화면이 위아래로 움직입니다.
 * 탭 바 · 레일이 이미 처리한 영역은 빼고 남은 만큼만 씁니다.
 * - 검색창 영역: 좌우 여백 (가로 화면에서 카메라 홀 · 내비게이션 바가 옆에 올 때)
 * - [content]: 좌우 · 아래 여백을 contentPadding으로 넘깁니다. 목록은 이 여백을 contentPadding으로 써서
 *   시스템 바 뒤까지 그리되 마지막 항목은 가리지 않습니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BookListScaffold(
    title: String,
    searchBar: @Composable () -> Unit,
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (contentPadding: PaddingValues) -> Unit,
) {
    var consumedInsets by remember { mutableStateOf(WindowInsets(0, 0, 0, 0)) }
    val safeInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout).exclude(consumedInsets)

    Column(
        modifier = modifier
            .fillMaxSize()
            .onConsumedWindowInsetsChanged { consumedInsets = it },
    ) {
        Surface {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CenterAlignedTopAppBar(
                    title = { Text(text = title, fontWeight = FontWeight.Bold) },
                )
                Column(
                    modifier = Modifier
                        .padding(safeInsets.only(WindowInsetsSides.Horizontal).asPaddingValues())
                        .widthIn(max = MaxSearchAreaWidth)
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                ) {
                    searchBar()
                    Box(modifier = Modifier.padding(top = 8.dp)) {
                        header()
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer),
        ) {
            content(safeInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom).asPaddingValues())
            HeaderShadow()
        }
    }
}

/**
 * 헤더 아래 그림자. Surface의 elevation 그림자는 화면 전환(페이드) 중에 그려지지 않다가 끝난 뒤에 나타나고,
 * 좌우로도 번져 내비게이션 레일 옆에 선처럼 보여서 그라데이션으로 직접 그립니다.
 */
@Composable
private fun HeaderShadow() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(HeaderShadowHeight)
            .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.08f), Color.Transparent))),
    )
}

private val MaxSearchAreaWidth = 840.dp
private val HeaderShadowHeight = 6.dp
