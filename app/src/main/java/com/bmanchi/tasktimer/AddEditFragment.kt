package com.bmanchi.tasktimer

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_add_edit.*

private const val TAG ="AddEditFragment"
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_TASK = "task"

/**
 * A simple [Fragment] subclass.
 * Use the [AddEditFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddEditFragment : Fragment() {

    private var task: Task? = null
    private var listener: OnSaveClicked? = null
//    private val viewModel by lazy { ViewModelProviders.of(requireActivity()).get(TaskTimerViewModel::class.java) }
    private val viewModel: TaskTimerViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: starts")
        super.onCreate(savedInstanceState)
        task = arguments?.getParcelable(ARG_TASK)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: starts")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_edit, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: called")
        if (savedInstanceState == null) {
            val task = task
            if (task != null) {
                Log.d(TAG, "onViewCreated: task details found, editing task ${task.id}")
                addedit_name.setText(task.name)
                addedit_description.setText(task.description)
                addedit_sortorder.setText(task.sortOrder.toString())
            } else {
                // No task, so we must be adding a new task
                Log.d(TAG, "onViewCreated: no arguments, adding new record")
            }
        }
    }
    
    private fun taskFromUi(): Task {
        val sortOrder = if (addedit_sortorder.text.isNotEmpty()) {
            Integer.parseInt(addedit_sortorder.text.toString())
        } else {
            0
        }
        val newTask = Task(addedit_name.text.toString(), addedit_description.text.toString(), sortOrder)
        newTask.id = task?.id ?:0

        return newTask
    }


    /*private fun saveTask() {
        // Update the database is at least one field has changed.
        // - There's no need to hit the database unless this has happened.
        val sortOrder = if (addedit_sortorder.text.isNotEmpty()){
            Integer.parseInt(addedit_sortorder.text.toString())
        } else {
            0
        }

        val values = ContentValues()
        val task = task

        if (task != null) {
            Log.d(TAG, "saveTask: updating existing task")
            if (addedit_name.text.toString() != task.name) {
                values.put(TasksContract.Columns.TASK_NAME, addedit_name.text.toString())
            }
            if (addedit_description.text.toString() != task.description) {
                values.put(TasksContract.Columns.TASK_DESCRIPTION, addedit_description.text.toString())
            }
            if (sortOrder != task.sortOrder) {
                values.put(TasksContract.Columns.TASK_SORT_ORDER, sortOrder)
            }
            if (values.size() != 0) {
                Log.d(TAG, "saveTask: Updating task")
                activity?.contentResolver?.update(TasksContract.buildUriFromId(task.id),
                values, null, null)
            }
        } else {
          Log.d(TAG, "saveTask: adding new task")
            if (addedit_name.text.isNotEmpty()) {
                values.put(TasksContract.Columns.TASK_NAME, addedit_name.text.toString())
                if (addedit_description.text.isNotEmpty()) {
                    values.put(TasksContract.Columns.TASK_DESCRIPTION, addedit_description.text.toString())
                }
                values.put(TasksContract.Columns.TASK_SORT_ORDER, sortOrder) // defaults to zero
                activity?.contentResolver?.insert(TasksContract.CONTENT_URI, values)
            }
        }
    }*/

    fun isDirty(): Boolean {
        val newTask = taskFromUi()
        return ((newTask != task) &&
                (newTask.name.isNotBlank()
                        || newTask.description.isNotBlank()
                        || newTask.sortOrder != 0)
                )
    }

    private fun saveTask() { //new save task with MVVM
        val newTask = taskFromUi()
        if (newTask != task) {
            Log.d(TAG, "saveTask: saving task, id is ${newTask.id}")
            task = viewModel.saveTask(newTask)
            Log.d(TAG, "saveTask: id is ${task?.id}")
        }

    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG, "onActivityCreated: starts")
        super.onActivityCreated(savedInstanceState)
        if (activity is AppCompatActivity) {
            val actionBar = (activity as AppCompatActivity?)?.supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(true)
        }

        addedit_save.setOnClickListener {
            saveTask()
            listener?.onSaveClicked()
        }
    }

    override fun onAttach(context: Context) {
        Log.d(TAG, "onAttach: starts")
        super.onAttach(context)
        if (context is OnSaveClicked) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnSaveClicked")
        }
    }

    override fun onDetach() {
        Log.d(TAG, "onDetach: starts")
        super.onDetach()
        listener = null
    }

    interface OnSaveClicked {
        fun onSaveClicked()
    }

    companion object {
        @JvmStatic
        fun newInstance(task: Task?) =
            AddEditFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TASK, task)
                }
            }
    }
}