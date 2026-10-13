package com.peter.booksearchapp.feature.book.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.peter.booksearchapp.feature.book.presentation.R
import com.peter.booksearchapp.feature.book.presentation.model.BookUiModel
import com.peter.booksearchapp.feature.book.presentation.preview.PreviewBooks

@Composable
internal fun BookCard(
    book: BookUiModel,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(BookCardHeight),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BookThumbnail(
                thumbnailUrl = book.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .width(96.dp)
                    .fillMaxHeight(),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = orUnknown(book.publishedDate),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                BookCardInfo(
                    label = stringResource(R.string.book_label_publisher),
                    value = orUnknown(book.publisher),
                )
                BookCardInfo(
                    label = stringResource(R.string.book_label_author),
                    value = orUnknown(book.authors.joinToString()),
                )
                Spacer(Modifier.weight(1f))
                BookCardPrice(
                    price = book.price,
                    salePrice = book.salePrice,
                    modifier = Modifier.align(Alignment.End),
                )
            }
            FavoriteButton(
                isFavorite = book.isFavorite,
                onClick = onFavoriteClick,
            )
        }
    }
}

@Composable
private fun BookCardInfo(label: String, value: String) {
    Text(
        text = labeledText(label, value),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun BookCardPrice(price: Int?, salePrice: Int?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (salePrice != null && price != null && salePrice < price) {
            Text(
                text = formatPrice(price),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                textDecoration = TextDecoration.LineThrough,
            )
        }
        Text(
            text = formatPrice(salePrice ?: price),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

private val BookCardHeight = 168.dp

@Preview(showBackground = true, backgroundColor = 0xFFE6E6E6)
@Composable
private fun BookCardPreview() {
    MaterialTheme {
        BookCard(
            book = PreviewBooks.book,
            onClick = {},
            onFavoriteClick = {},
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE6E6E6)
@Composable
private fun BookCardLongTitlePreview() {
    MaterialTheme {
        BookCard(
            book = PreviewBooks.longTitleBook,
            onClick = {},
            onFavoriteClick = {},
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE6E6E6)
@Composable
private fun BookCardNoInfoPreview() {
    MaterialTheme {
        BookCard(
            book = PreviewBooks.noInfoBook,
            onClick = {},
            onFavoriteClick = {},
            modifier = Modifier.padding(12.dp),
        )
    }
}
