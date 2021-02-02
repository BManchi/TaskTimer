package com.bmanchi.tasktimer

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
@SuppressLint("ParcelCreator")
data class Task (val name: String, val description: String, val sortOrder: Int): Parcelable {
    var id: Long = 0
}