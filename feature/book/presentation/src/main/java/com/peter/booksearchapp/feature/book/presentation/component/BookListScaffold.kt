package com.peter.booksearchapp.feature.book.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BookListScaffold(
    title: String,
    searchBar: @Composable () -> Unit,
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Surface {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CenterAlignedTopAppBar(
                    title = { Text(text = title, fontWeight = FontWeight.Bold) },
                )
                Column(
                    modifier = Modifier
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
            content()
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
