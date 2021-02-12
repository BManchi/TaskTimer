package com.bmanchi.tasktimer

import android.app.Application
import android.content.ContentValues
import android.content.SharedPreferences
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "TaskTimerViewModel"

class TaskTimerViewModel(application: Application): AndroidViewModel(application) {

    private val contentObserver = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            Log.d(TAG, "contentObserver.onChange: called. uri is $uri")
            loadTasks()
        }
    }

    private val settings = PreferenceManager.getDefaultSharedPreferences(application)
    private var ignoreLessThan = settings.getInt(SETTINGS_IGNORE_LESS_THAN, SETTINGS_DEFAULT_IGNORE_LESS_THAN)

    private val settingsListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        when (key) {
            SETTINGS_IGNORE_LESS_THAN -> {
                ignoreLessThan = sharedPreferences.getInt(key, SETTINGS_DEFAULT_IGNORE_LESS_THAN)
                Log.d(TAG, "settingsListener: now ignoring timings less than $ignoreLessThan seconds")

                // Update the database parameters table
                val values = ContentValues()
                values.put(ParametersContract.Columns.VALUE, ignoreLessThan)
                viewModelScope.launch {
                    Dispatchers.IO
                    getApplication<Application>().contentResolver
                            .update(ParametersContract.buildUriFromId(ParametersContract.ID_SHORT_TIMING),
                                    values,
                                    null,
                                    null)
                }
            }
        }
    }

    private var currentTiming: Timing? = null

    private val databaseCursor = MutableLiveData<Cursor>()
    val cursor: LiveData<Cursor>
    get() = databaseCursor

    private val taskTiming = MutableLiveData<String>()
    val timing: LiveData<String>
        get() = taskTiming

    var editedTaskId = 0L
    private set

    init {
        Log.d(TAG, "TaskTimerViewModel: created")
        getApplication<Application>().contentResolver.registerContentObserver(TasksContract.CONTENT_URI,
                true, contentObserver)

        Log.d(TAG, "Ignoring timings less than $ignoreLessThan seconds")
        settings.registerOnSharedPreferenceChangeListener(settingsListener)

        currentTiming = retrieveTiming()
        loadTasks()
    }

    private fun loadTasks() {
        val projection = arrayOf(TasksContract.Columns.ID,
                TasksContract.Columns.TASK_NAME,
                TasksContract.Columns.TASK_DESCRIPTION,
                TasksContract.Columns.TASK_SORT_ORDER)
        // <order by> Tasks.SortOrder, Tasks.Name COLLATE NOCASE
        val sortOrder = "${TasksContract.Columns.TASK_SORT_ORDER}, ${TasksContract.Columns.TASK_NAME} COLLATE NOCASE"

        viewModelScope.launch(Dispatchers.IO) {
            val cursor = getApplication<Application>().contentResolver.query(
                    TasksContract.CONTENT_URI,
                    projection, null, null,
                    sortOrder)
            databaseCursor.postValue(cursor!!)
        }
    }

    fun saveTask (task: Task): Task {
        val values = ContentValues()

        if (task.name.isNotEmpty()) {
            // Don't save a task with no name
            values.put(TasksContract.Columns.TASK_NAME, task.name)
            values.put(TasksContract.Columns.TASK_DESCRIPTION, task.description)
            values.put(TasksContract.Columns.TASK_SORT_ORDER, task.sortOrder)

            if (task.id == 0L) {
                viewModelScope.launch(Dispatchers.IO) {
                    Log.d(TAG, "saveTask: adding new task")
                    val uri = getApplication<Application>().contentResolver?.insert(TasksContract.CONTENT_URI, values)
                    if (uri != null) {
                        task.id = TasksContract.getId(uri)
                        Log.d(TAG, "saveTask: new id is ${task.id}")
                    }
                }
            } else {
                // task has an id, so we're updating
                viewModelScope.launch(Dispatchers.IO) {
                    Log.d(TAG, "saveTask: updating task")
                    getApplication<Application>().contentResolver?.update(TasksContract.buildUriFromId(task.id), values, null, null)
                }
            }
        }
        return task
    }

    fun startEditing(taskId: Long) {
        if (BuildConfig.DEBUG && editedTaskId != 0L) throw IllegalStateException(
            "startEditing called without stopping previous edit. editedTaskId: $editedTaskId, taskId: $taskId"
        )
        editedTaskId = taskId
    }

    fun stopEditing() {
        editedTaskId = 0L
    }

    fun deleteTask(taskId: Long) {
        Log.d(TAG, "Deleting task")
        viewModelScope.launch(Dispatchers.IO) {
            getApplication<Application>().contentResolver?.delete(TasksContract.buildUriFromId(taskId), null, null)
        }
    }

    fun timeTask(task: Task) {
        Log.d(TAG, "timeTask: Called")
        // use local variable to allow smart casts
        val timingRecord = currentTiming

        if (timingRecord == null) {
            // no task being timed, start timing the new task
            currentTiming = Timing(task.id)
             saveTiming(currentTiming!!)
        } else {
            // we have a task being timed, so save it
            timingRecord.setDuration()
              saveTiming(timingRecord)

            currentTiming = if (task.id == timingRecord.taskId) {
                // the current task was tapped a second time, stop timing
                null
            } else {
                // a new task is being timed
                val newTiming = Timing(task.id)
                saveTiming(newTiming)
                newTiming
            }
        }

        // Update the LiveData
        taskTiming.value = if (currentTiming != null) task.name else null
    }

    private fun saveTiming(currentTiming: Timing) {
        Log.d(TAG, "saveTiming: called")

        // we are updating, or inserting a new row?
        val inserting= (currentTiming.duration == 0L)

        val values = ContentValues().apply {
            if (inserting){
                put(TimingsContract.Columns.TIMING_TASK_ID, currentTiming.taskId)
                put(TimingsContract.Columns.TIMING_START_TIME, currentTiming.startTime)
            }
            put(TimingsContract.Columns.TIMINGS_DURATION, currentTiming.duration)
        }

        viewModelScope.launch(Dispatchers.IO) {
            if (inserting) {
                val uri = getApplication<Application>().contentResolver.insert(TimingsContract.CONTENT_URI, values)
                if (uri != null) {
                    currentTiming.id = TimingsContract.getId(uri)
                }
            } else {

                Log.d(TAG, "saveTiming: saving timing with duration ${currentTiming.duration}")
                getApplication<Application>().contentResolver.update(TimingsContract.buildUriFromId(currentTiming.id), values, null, null)
            }
        }

    }

    private fun retrieveTiming(): Timing? {
        Log.d(TAG, "retrieveTiming: starts")

        val timing: Timing?

        val timingCursor: Cursor? = getApplication<Application>().contentResolver.query(
            CurrentTimingContract.CONTENT_URI,
            null,
            null,
            null,
            null)

        if (timingCursor != null && timingCursor.moveToFirst()) {
            // We have an un-timed record
            val id = timingCursor.getLong(timingCursor.getColumnIndex(CurrentTimingContract.Columns.TIMING_ID))
            val taskId = timingCursor.getLong(timingCursor.getColumnIndex(CurrentTimingContract.Columns.TASK_ID))
            val startTime = timingCursor.getLong(timingCursor.getColumnIndex(CurrentTimingContract.Columns.START_TIME))
            val name = timingCursor.getString(timingCursor.getColumnIndex(CurrentTimingContract.Columns.TASK_NAME))
            timing = Timing(taskId, startTime, id)

            // Update the LiveData
            taskTiming.value = name
        } else {
            timing = null
        }

        timingCursor?.close()

        Log.d(TAG, "retrieveTiming returning")
        return timing
    }
    override fun onCleared() {
        Log.d(TAG, "onCleared: called")
        getApplication<Application>().contentResolver.unregisterContentObserver(contentObserver)

        settings.unregisterOnSharedPreferenceChangeListener(settingsListener)
    }
}