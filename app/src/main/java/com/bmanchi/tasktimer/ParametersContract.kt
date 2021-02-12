package com.bmanchi.tasktimer

import android.content.ContentUris
import android.net.Uri
import android.provider.BaseColumns

/**
 * Created by timbuchalka for the Android Oreo using Kotlin course
 * from www.learnprogramming.academy
 */

object ParametersContract {
    const val ID_SHORT_TIMING = 1L

    internal const val TABLE_NAME = "Parameters"

    /**
     * The URI to access the Parameters table.
     */
    val CONTENT_URI: Uri = Uri.withAppendedPath(CONTENT_AUTHORITY_URI, TABLE_NAME)

    const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"
    const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"

    // Parameters fields
    object Columns {
        const val ID = BaseColumns._ID
        const val VALUE = "Value"
    }

    fun getId(uri: Uri): Long {
        return ContentUris.parseId(uri)
    }

    fun buildUriFromId(id: Long): Uri {
        return ContentUris.withAppendedId(CONTENT_URI, id)
    }
}
