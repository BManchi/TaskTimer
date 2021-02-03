package com.bmanchi.tasktimer

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.content_main.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), AddEditFragment.OnSaveClicked, MainActivityFragment.OnTaskEdit {

    // Whether or the activity is in two pane mode
    private var mTwoPane = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        mTwoPane = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val fragment = supportFragmentManager.findFragmentById(R.id.task_details_container)
        if (fragment != null) {
            // there was an existing fragment to edit a task, make sure the panes are set correctly
            showEditPane()
        } else {
            task_details_container.visibility = if(mTwoPane) View.INVISIBLE else View.GONE
            mainFragment.view?.visibility = View.VISIBLE
        }
        /*testInsert("prueba 1", "probando agregar", 1)
        testInsert("prueba 2", "probando agregar", 2)
        testInsert("prueba 3", "probando agregar", 1)

        testUpdate()

        testUpdate2()

        testDelete()

        testDelete2()

        val appDatabase = AppDatabase.getInstance(this)
        val db = appDatabase.readableDatabase

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
        }*/
    }

    private fun showEditPane() {
        task_details_container.visibility = View.VISIBLE
        // hide the left hand pane, if in single pane vieew
        mainFragment.view?.visibility = if(mTwoPane) View.VISIBLE else View.GONE
    }
    private fun removeEditePane(fragment: Fragment? = null) {
        Log.d(TAG, "removeEditPane called")
        if (fragment != null){
            supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
        }

        // Set the visibility of the right hand pane
        task_details_container.visibility = if(mTwoPane) View.INVISIBLE else View.GONE
        // and show the left hand pane
        mainFragment.view?.visibility = View.VISIBLE

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onSaveClicked() {
        Log.d(TAG, "onSaveClicked: called")
        val fragment = supportFragmentManager.findFragmentById(R.id.task_details_container)
        removeEditePane(fragment)
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
        when (item.itemId) {
            R.id.menumain_addTask -> taskEditRequest(null)
//            R.id.menumain_settings -> true
            android.R.id.home -> {
                Log.d(TAG, "onOptionsItemSelected: home button pressed")
                val fragment = supportFragmentManager.findFragmentById(R.id.task_details_container)
                removeEditePane(fragment)
            }
//            else -> super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun taskEditRequest(task: Task?) {
        Log.d(TAG, "taskEditRequest: starts")

        // Create a new fragment to edit the task
        val newFragment = AddEditFragment.newInstance(task)
        supportFragmentManager.beginTransaction()
            .replace(R.id.task_details_container, newFragment)
            .commit()

        showEditPane()

        Log.d(TAG, "Exiting taskEditRequest")
    }

    /**
     * Called when the activity has detected the user's press of the back
     * key. The [OnBackPressedDispatcher][.getOnBackPressedDispatcher] will be given a
     * chance to handle the back button before the default behavior of
     * [android.app.Activity.onBackPressed] is invoked.
     *
     * @see .getOnBackPressedDispatcher
     */
    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.task_details_container)
        if (fragment == null || mTwoPane) {
            super.onBackPressed()
        } else {
            removeEditePane(fragment)
        }
    }

    override fun onTaskEdit(task: Task) {
        taskEditRequest(task)
    }

    //    private fun testInsert(name: String, description: String, sortOrder: Int) {
//        val values = ContentValues().apply {
//            put(TasksContract.Columns.TASK_NAME, "$name")
//            put(TasksContract.Columns.TASK_DESCRIPTION, "$description")
//            put(TasksContract.Columns.TASK_SORT_ORDER, sortOrder)
//        }
//
//        val uri = contentResolver.insert(TasksContract.CONTENT_URI, values)
//        Log.d(TAG, "New row id (in uri) is $uri")
//        Log.d(TAG, "id (in uri) is ${TasksContract.getId(uri!!)}")
//    }
//
//    private fun testDelete() {
//
//        val taskUri = TasksContract.buildUriFormId(3)
//        val rowsAffected = contentResolver.delete(taskUri, null, null)
//        Log.d(TAG, "Number of rows deleted is $rowsAffected")
//    }
//
//    private fun testDelete2() {
//
//        val selection = TasksContract.Columns.TASK_DESCRIPTION + " = ?"
//        val selectionArgs = arrayOf("For deletion")
//        val rowsAffected = contentResolver.delete(TasksContract.CONTENT_URI,
//                selection,
//                selectionArgs)
//        Log.d(TAG, "Number of rows affected is $rowsAffected")
//    }
//
//    private fun testUpdate2() {
//        val values = ContentValues().apply {
//            put(TasksContract.Columns.TASK_SORT_ORDER, 999)
//            put(TasksContract.Columns.TASK_DESCRIPTION, "For deletion")
//        }
//
//        val selection = TasksContract.Columns.TASK_SORT_ORDER + " = ?"
//        val selectionArgs = arrayOf("99")
////        val taskUri = TasksContract.buildUriFormId(3)
//        val rowsAffected = contentResolver.update(TasksContract.CONTENT_URI,
//                values,
//                selection,
//                selectionArgs)
//        Log.d(TAG, "Number of rows affected is $rowsAffected")
//    }
//
//    private fun testUpdate() {
//        val values = ContentValues().apply {
//            put(TasksContract.Columns.TASK_NAME, "Content Provider")
//            put(TasksContract.Columns.TASK_DESCRIPTION, "Record content providers videos")
//        }
//
//        val taskUri = TasksContract.buildUriFormId(3)
//        val rowsAffected = contentResolver.update(taskUri, values, null, null)
//        Log.d(TAG, "Number of rows affected is $rowsAffected")
//    }


}