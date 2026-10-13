package com.peter.booksearchapp.feature.book.presentation.favorite

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.peter.booksearchapp.feature.book.presentation.R
import com.peter.booksearchapp.feature.book.presentation.model.PriceFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PriceFilterSheet(
    selected: PriceFilter,
    onSelect: (PriceFilter) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        PriceFilterOptions(
            selected = selected,
            onSelect = {
                onSelect(it)
                onDismiss()
            },
        )
    }
}

@Composable
private fun PriceFilterOptions(
    selected: PriceFilter,
    onSelect: (PriceFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(bottom = 24.dp)) {
        Text(
            text = stringResource(R.string.book_price_filter_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        Column(modifier = Modifier.selectableGroup()) {
            PriceFilter.entries.forEach { filter ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = filter == selected,
                            onClick = { onSelect(filter) },
                            role = Role.RadioButton,
                        )
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = filter == selected, onClick = null)
                    Text(
                        text = stringResource(filter.labelRes),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PriceFilterOptionsPreview() {
    MaterialTheme {
        PriceFilterOptions(selected = PriceFilter.FROM_10000_TO_20000, onSelect = {})
    }
}
