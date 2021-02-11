package com.bmanchi.tasktimer

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bmanchi.tasktimer.debug.TestData
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_main.*

private const val TAG = "MainActivity"
private const val DIALOG_ID_CANCEL_EDIT = 1


class MainActivity : AppCompatActivity(), AddEditFragment.OnSaveClicked,
    MainActivityFragment.OnTaskEdit,
    AppDialog.DialogEvents {

    // Whether or the activity is in two pane mode
    private var mTwoPane = false

    // module scope because we need to dismiss it in onStop (E.g. when orientation changes) to avoid memory leaks.
    private var aboutDialog: AlertDialog? = null

    private val viewModel by lazy { ViewModelProviders.of(this).get(TaskTimerViewModel::class.java)}

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: starts")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        mTwoPane = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        Log.d(TAG, "onCreate: twoPane is $mTwoPane")

        val fragment = findFragmentById(R.id.task_details_container)
        if (fragment != null) {
            // there was an existing fragment to edit a task, make sure the panes are set correctly
            showEditPane()
        } else {
            task_details_container.visibility = if (mTwoPane) View.INVISIBLE else View.GONE
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

        viewModel.timing.observe(this, Observer<String> { timing ->
            current_task.text = if (timing != null) {
                getString(R.string.timing_message, timing)
            } else {
                getString(R.string.no_task_message)
            }
        })

        Log.d(TAG, "onCreate: finished")
    }

    private fun showEditPane() {
        task_details_container.visibility = View.VISIBLE
        // hide the left hand pane, if in single pane vieew
        mainFragment.view?.visibility = if (mTwoPane) View.VISIBLE else View.GONE
    }

    private fun removeEditePane(fragment: Fragment? = null) {
        Log.d(TAG, "removeEditPane called")
        if (fragment != null) {
//            supportFragmentManager.beginTransaction()
//                .remove(fragment)
//                .commit()
            removeFragment(fragment)
        }

        // Set the visibility of the right hand pane
        task_details_container.visibility = if (mTwoPane) View.INVISIBLE else View.GONE
        // and show the left hand pane
        mainFragment.view?.visibility = View.VISIBLE

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onSaveClicked() {
        Log.d(TAG, "onSaveClicked: called")
//        val fragment = supportFragmentManager.findFragmentById(R.id.task_details_container)
        removeEditePane(findFragmentById(R.id.task_details_container))  // replaced fragment with ExtensionFunction
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        if (BuildConfig.DEBUG) {
            val generate = menu.findItem(R.id.menumain_generate)
            generate.isVisible = true
        }


        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.menumain_addTask -> taskEditRequest(null)
            R.id.menumain_showDurations -> startActivity(Intent(this, DurationsReport::class.java))
            R.id.menumain_settings -> {
                val dialog = SettingsDialog()
                dialog.show(supportFragmentManager, null)
            }
            R.id.menumain_showAbaut -> showAboutDialog()
            R.id.menumain_generate -> TestData.generateTestData(contentResolver)
            android.R.id.home -> {
                Log.d(TAG, "onOptionsItemSelected: home button pressed")
                val fragment = findFragmentById(R.id.task_details_container)
//                removeEditePane(fragment)
                if ((fragment is AddEditFragment) && fragment.isDirty()) {
                    showConfirmationDialog(
                        DIALOG_ID_CANCEL_EDIT,
                        getString(R.string.cancelEditDiago_message),
                        R.string.cancelEditDiag_positive_caption,
                        R.string.cancelEditDiag_negative_caption
                    )
                } else {
                    removeEditePane(fragment)
                }
            }
//            else -> super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAboutDialog() {
        val messageView = layoutInflater.inflate(R.layout.about, null, false)
        val builder = AlertDialog.Builder(this)

        builder.setTitle(R.string.app_name)
        builder.setIcon(R.mipmap.ic_launcher)

        builder.setPositiveButton(R.string.ok) { _, _ ->
            Log.d(TAG, "onClick: Entering messageView.onClick")
            if (aboutDialog != null && aboutDialog?.isShowing == true) {
                aboutDialog?.dismiss()
            }
        }

        aboutDialog = builder.setView(messageView).create()
        aboutDialog?.setCanceledOnTouchOutside(true)

        messageView.setOnClickListener {
            Log.d(TAG, "Entering messageView.onClick")
            if (aboutDialog != null && aboutDialog?.isShowing == true) {
                aboutDialog?.dismiss()
            }
        }

        val aboutVersion = messageView.findViewById(R.id.about_version) as TextView
        aboutVersion.text = BuildConfig.VERSION_NAME

        aboutDialog?.show()
    }


    private fun taskEditRequest(task: Task?) {
        Log.d(TAG, "taskEditRequest: starts")

        // Create a new fragment to edit the task
//        val newFragment = AddEditFragment.newInstance(task)
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.task_details_container, newFragment)
//            .commit()

        showEditPane()
        replaceFragment(AddEditFragment.newInstance(task), R.id.task_details_container)
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
        val fragment = findFragmentById(R.id.task_details_container)
        if (fragment == null || mTwoPane) {
            super.onBackPressed()
        } else {
//            removeEditePane(fragment)
            if ((fragment is AddEditFragment) && fragment.isDirty()) {
                showConfirmationDialog(
                    DIALOG_ID_CANCEL_EDIT,
                    getString(R.string.cancelEditDiago_message),
                    R.string.cancelEditDiag_positive_caption,
                    R.string.cancelEditDiag_negative_caption
                )
            } else {
                removeEditePane(fragment)
            }
        }
    }

    override fun onStop() {
        Log.d(TAG, "onStop: called")
        super.onStop()
        if (aboutDialog?.isShowing == true) {
            aboutDialog?.dismiss()
        }
    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        Log.d(TAG, "onPositiveDialogResult called with dialogId $dialogId")
        if (dialogId == DIALOG_ID_CANCEL_EDIT) {
            val fragment = findFragmentById(R.id.task_details_container)
            removeEditePane(fragment)
        }
    }

    override fun onNegativeDialogResult(dialogId: Int, args: Bundle) {
        TODO("Not yet implemented")
    }

    override fun onDialogCancelled(dialogId: Int) {
        TODO("Not yet implemented")
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