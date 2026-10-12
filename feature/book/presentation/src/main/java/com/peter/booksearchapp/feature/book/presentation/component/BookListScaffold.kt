package com.peter.booksearchapp.feature.book.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 검색 탭과 즐겨찾기 탭의 공통 뼈대. 제목 → 검색창 → 조건 줄 → 회색 배경의 리스트 영역 순서로 놓는다.
 *
 * 검색창과 조건 줄은 넓은 화면에서 너무 길게 늘어나지 않도록 [MaxSearchAreaWidth]로 폭을 제한한다.
 */
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
        Surface(shadowElevation = 4.dp) {
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
        }
    }
}

private val MaxSearchAreaWidth = 840.dp
