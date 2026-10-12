package com.peter.booksearchapp.core.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

internal class StringListConverter {
    @TypeConverter
    fun fromList(value: List<String>?): String? = value?.let { Json.encodeToString(it) }

    @TypeConverter
    fun toList(value: String?): List<String>? = value?.let { Json.decodeFromString(it) }
}
