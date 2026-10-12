package com.peter.booksearchapp.feature.book.presentation.component

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.peter.booksearchapp.feature.book.presentation.R

/**
 * 즐겨찾기 하트 버튼.
 *
 * @param filled true면 원형 배경 위에 하트를 그린다(리스트 카드용). false면 아이콘만 그린다(상단 바용).
 */
@Composable
internal fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        colors = if (filled) {
            IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            )
        } else {
            IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent)
        },
    ) {
        Icon(
            painter = painterResource(
                if (isFavorite || filled) R.drawable.ic_favorite else R.drawable.ic_favorite_border,
            ),
            contentDescription = stringResource(
                if (isFavorite) R.string.book_favorite_remove else R.string.book_favorite_add,
            ),
            tint = when {
                isFavorite -> MaterialTheme.colorScheme.error
                filled -> MaterialTheme.colorScheme.outline
                else -> MaterialTheme.colorScheme.onSurface
            },
        )
    }
}
