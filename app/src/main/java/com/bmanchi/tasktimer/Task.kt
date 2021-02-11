package com.bmanchi.tasktimer

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Task(val name: String, val description: String, val sortOrder: Int, var id: Long = 0) : Parcelable {
}