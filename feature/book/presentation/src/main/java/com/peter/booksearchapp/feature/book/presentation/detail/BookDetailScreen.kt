package com.peter.booksearchapp.feature.book.presentation.detail

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.peter.booksearchapp.feature.book.presentation.model.UiState
import com.peter.booksearchapp.feature.book.presentation.R
import com.peter.booksearchapp.feature.book.presentation.component.BookThumbnail
import com.peter.booksearchapp.feature.book.presentation.component.FavoriteButton
import com.peter.booksearchapp.feature.book.presentation.component.LoadingContent
import com.peter.booksearchapp.feature.book.presentation.component.MessageContent
import com.peter.booksearchapp.feature.book.presentation.component.formatPrice
import com.peter.booksearchapp.feature.book.presentation.component.labeledText
import com.peter.booksearchapp.feature.book.presentation.component.orUnknown
import com.peter.booksearchapp.feature.book.presentation.model.BookUiModel
import com.peter.booksearchapp.feature.book.presentation.preview.PreviewBooks

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    uiState: UiState<BookUiModel>,
    onBackClick: () -> Unit,
    onFavoriteClick: (BookUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    Icon(
                        modifier = Modifier.clickable { onBackClick() },
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.book_detail_back),
                    )
                },
                actions = {
                    if (uiState is UiState.Success) {
                        FavoriteButton(
                            isFavorite = uiState.data.isFavorite,
                            onClick = { onFavoriteClick(uiState.data) },
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            UiState.Idle, UiState.Loading -> LoadingContent(Modifier.padding(innerPadding))
            UiState.Error -> MessageContent(
                message = stringResource(R.string.book_detail_error),
                modifier = Modifier.padding(innerPadding),
            )
            is UiState.Success -> BookDetailContent(
                book = uiState.data,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun BookDetailContent(
    book: BookUiModel,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter,
    ) {
        if (maxWidth < ExpandedLayoutMinWidth) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                BookDetailTitle(book.title)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    BookThumbnail(
                        thumbnailUrl = book.thumbnailUrl,
                        contentDescription = book.title,
                        modifier = Modifier
                            .weight(0.42f)
                            .aspectRatio(CoverAspectRatio),
                    )
                    BookDetailInfo(book = book, modifier = Modifier.weight(0.58f))
                }
                BookDetailContents(book.contents)
            }
        } else {
            Row(
                modifier = Modifier
                    .widthIn(max = MaxExpandedContentWidth)
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                BookThumbnail(
                    thumbnailUrl = book.thumbnailUrl,
                    contentDescription = book.title,
                    modifier = Modifier
                        .width(ExpandedCoverWidth)
                        .aspectRatio(CoverAspectRatio),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    BookDetailTitle(book.title)
                    BookDetailInfo(book = book)
                    BookDetailContents(book.contents)
                }
            }
        }
    }
}

@Composable
private fun BookDetailTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun BookDetailInfo(book: BookUiModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BookDetailInfoRow(R.string.book_label_author, orUnknown(book.authors.joinToString()))
        BookDetailInfoRow(R.string.book_label_publisher, orUnknown(book.publisher))
        BookDetailInfoRow(R.string.book_label_published_date, orUnknown(book.publishedDate))
        BookDetailInfoRow(R.string.book_label_isbn, orUnknown(book.isbn))
        BookDetailInfoRow(R.string.book_label_price, formatPrice(book.price))
        if (book.salePrice != null) {
            BookDetailInfoRow(R.string.book_label_sale_price, formatPrice(book.salePrice))
        }
    }
}

@Composable
private fun BookDetailInfoRow(@StringRes labelRes: Int, value: String) {
    Text(
        text = labeledText(stringResource(labelRes), value),
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
private fun BookDetailContents(contents: String) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.book_detail_contents),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = contents.ifBlank { stringResource(R.string.book_detail_contents_empty) },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(16.dp),
                )
                .padding(16.dp),
        )
    }
}

private val ExpandedLayoutMinWidth = 600.dp
private val MaxExpandedContentWidth = 1040.dp
private val ExpandedCoverWidth = 280.dp

/** 카카오 API 썸네일 비율(120x174)에 맞춘 표지 가로:세로 비율. */
private const val CoverAspectRatio = 120f / 174f

@Composable
private fun BookDetailScreenPreview(uiState: UiState<BookUiModel>) {
    MaterialTheme {
        BookDetailScreen(uiState = uiState, onBackClick = {}, onFavoriteClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun BookDetailScreenSuccessPreview() {
    BookDetailScreenPreview(UiState.Success(PreviewBooks.book))
}

@Preview(showBackground = true)
@Composable
private fun BookDetailScreenNoInfoPreview() {
    BookDetailScreenPreview(UiState.Success(PreviewBooks.noInfoBook))
}

@Preview(showBackground = true)
@Composable
private fun BookDetailScreenLoadingPreview() {
    BookDetailScreenPreview(UiState.Loading)
}

@Preview(showBackground = true)
@Composable
private fun BookDetailScreenErrorPreview() {
    BookDetailScreenPreview(UiState.Error)
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
private fun BookDetailScreenTabletPreview() {
    BookDetailScreenPreview(UiState.Success(PreviewBooks.longTitleBook))
}
