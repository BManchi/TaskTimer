package com.bmanchi.tasktimer

import android.database.Cursor
import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.tasks_durations.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val TAG = "DurationsReport"

enum class SortColumns {
    NAME,
    DESCRIPTION,
    START_DATE,
    DURATION
}
class DurationsReport : AppCompatActivity() {

    private val reportAdapter by lazy { DurationsRVAdapter(this, null) }

    var databaseCursor: Cursor? = null

    var sortOrder = SortColumns.NAME

    private val selection = "${DurationsContract.Columns.START_TIME} Between ? AND ?"
    private var selectionArgs = arrayOf("1556668800", "1559347199")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_durations_report)
        setSupportActionBar(findViewById(R.id.toolbar))

        td_list.layoutManager = LinearLayoutManager(this)
        td_list.adapter = reportAdapter

        loadData()  // Do not do on production. The data is queried every time the device is rotated
    }

    private fun loadData() {
        val order = when (sortOrder) {
            SortColumns.NAME -> DurationsContract.Columns.NAME
            SortColumns.DESCRIPTION -> DurationsContract.Columns.DESCRIPTION
            SortColumns.START_DATE -> DurationsContract.Columns.START_DATE
            SortColumns.DURATION -> DurationsContract.Columns.DURATION
        }
        Log.d(TAG, "order is $order")

        GlobalScope.launch {
            val cursor = application.contentResolver.query(
                DurationsContract.CONTENT_URI,
                null,
                selection,
                selectionArgs,
                order
            )
        }
    }
}