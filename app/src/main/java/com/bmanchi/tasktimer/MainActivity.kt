package com.bmanchi.tasktimer

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        testInsert("prueba 1", "probando agregar", 1)
        testInsert("prueba 2", "probando agregar", 2)
        testInsert("prueba 3", "probando agregar", 1)

        testUpdate()

//        val appDatabase = AppDatabase.getInstance(this)
//        val db = appDatabase.readableDatabase

        val projection = arrayOf(TasksContract.Columns.TASK_NAME, TasksContract.Columns.TASK_SORT_ORDER)
        val sortColumn = TasksContract.Columns.TASK_SORT_ORDER

        // Replace db with contentResolver
        val cursor = contentResolver.query(TasksContract.CONTENT_URI, null, null, null, sortColumn)
        Log.d(TAG, "*****************")
        cursor?.use {
            while (it.moveToNext()) {
                // Cycle through all records
                with (cursor) {
                    val id = this.getLong(0)
                    val name = getString(1)
                    val description = getString(2)
                    val sortOrder = getString( 3)
                    val result ="ID: $id, Name: $name, description: $description, sort order: $sortOrder"
                    Log.d(TAG, "onCreate: reading data $result")
                }
            }
        }
        Log.d(TAG, "*****************")

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun testInsert(name: String, description: String, sortOrder: Int) {
        val values = ContentValues().apply {
            put(TasksContract.Columns.TASK_NAME, "$name")
            put(TasksContract.Columns.TASK_DESCRIPTION, "$description")
            put(TasksContract.Columns.TASK_SORT_ORDER, sortOrder)
        }

        val uri = contentResolver.insert(TasksContract.CONTENT_URI, values)
        Log.d(TAG, "New row id (in uri) is $uri")
        Log.d(TAG, "id (in uri) is ${TasksContract.getId(uri!!)}")
    }

    private fun testUpdate() {
        val values = ContentValues().apply {
            put(TasksContract.Columns.TASK_NAME, "Content Provider")
            put(TasksContract.Columns.TASK_DESCRIPTION, "Record content providers videos")
        }

        val taskUri = TasksContract.buildUriFormId(3)
        val rowsAffected = contentResolver.update(taskUri, values, null, null)
        Log.d(TAG, "Number of rows affected is $rowsAffected")
    }
}