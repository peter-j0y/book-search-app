package com.peter.booksearchapp.feature.book.presentation.detail

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.peter.booksearchapp.feature.book.presentation.R
import com.peter.booksearchapp.feature.book.presentation.component.MessageContent

/** 넓은 화면에서 아직 도서를 고르지 않았을 때 상세 칸에 보여주는 안내 */
@Composable
internal fun BookDetailPlaceholder(modifier: Modifier = Modifier) {
    MessageContent(message = stringResource(R.string.book_detail_placeholder), modifier = modifier)
}

@Preview(showBackground = true)
@Composable
private fun BookDetailPlaceholderPreview() {
    MaterialTheme {
        BookDetailPlaceholder()
    }
}
