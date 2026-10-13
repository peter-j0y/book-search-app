package com.peter.booksearchapp.feature.book.presentation.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.peter.booksearchapp.feature.book.presentation.R
import com.peter.booksearchapp.feature.book.presentation.model.SearchSort

/**
 * 검색창 아래 줄. 왼쪽에 현재 적용된 조건을, 오른쪽에 정렬·필터 버튼을 둔다.
 *
 * @param actions 오른쪽 버튼들. [HeaderOptionButton], [SortMenuButton]을 넣는다.
 */
@Composable
internal fun BookListHeader(
    label: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        actions()
    }
}

/** "정렬", "필터" 같은 테두리 알약 버튼. */
@Composable
internal fun HeaderOptionButton(
    text: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(text)
    }
}

/**
 * 누르면 정렬 옵션 메뉴가 열리는 "정렬" 버튼.
 *
 * 메뉴가 열려 있는지는 화면 밖에서 알 필요가 없는 UI 내부 상태라 여기서 들고 있는다.
 */
@Composable
internal fun <T> SortMenuButton(
    options: List<T>,
    selected: T,
    optionLabel: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        HeaderOptionButton(
            text = stringResource(R.string.book_sort),
            iconRes = R.drawable.ic_sort,
            onClick = { expanded = true },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = optionLabel(option),
                            fontWeight = if (option == selected) FontWeight.Bold else null,
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BookListHeaderPreview() {
    MaterialTheme {
        BookListHeader(
            label = stringResource(SearchSort.ACCURACY.labelRes),
            modifier = Modifier.padding(16.dp),
        ) {
            HeaderOptionButton(
                text = stringResource(R.string.book_filter),
                iconRes = R.drawable.ic_filter,
                onClick = {},
            )
            SortMenuButton(
                options = SearchSort.entries,
                selected = SearchSort.ACCURACY,
                optionLabel = { stringResource(it.labelRes) },
                onSelect = {},
            )
        }
    }
}
