package com.peter.booksearchapp.feature.book.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.peter.booksearchapp.feature.book.presentation.R
import java.text.NumberFormat
import java.util.Locale

/** 12000 → "12,000원". 가격 정보가 없으면 "정보 없음". */
@Composable
internal fun formatPrice(price: Int?): String =
    if (price == null) {
        stringResource(R.string.book_unknown)
    } else {
        stringResource(R.string.book_price, NumberFormat.getNumberInstance(Locale.KOREA).format(price))
    }

/** 비어 있는 값은 "정보 없음"으로 바꾼다. */
@Composable
internal fun orUnknown(value: String): String =
    value.ifBlank { stringResource(R.string.book_unknown) }

/** "라벨 : 값" 형태에서 라벨만 굵게 그린다. 디자인의 "저자 : 저자" 행에 쓴다. */
internal fun labeledText(label: String, value: String): AnnotatedString =
    buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(label) }
        append(" : ")
        append(value)
    }
