package com.bmanchi.tasktimer

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_main.*
import java.lang.AssertionError

private const val DIALOG_ID_DELETE = 1
private const val DIALOG_TASK_ID = "task_id"

private const val TAG = "MainActivityFragment"
/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MainActivityFragment : Fragment(),
CursorRecyclerViewAdapter.OnTaskClickListener,
AppDialog.DialogEvents {

    private val viewModel by lazy { ViewModelProviders.of(requireActivity()).get(TaskTimerViewModel::class.java) }
    private val mAdapter = CursorRecyclerViewAdapter(null, this)

    /**
     * Called when a fragment is first attached to its context.
     * [.onCreate] will be called after this.
     */
    override fun onAttach(context: Context) {
        Log.d(TAG, "onAttach: called")
        super.onAttach(context)

        if (context !is OnTaskEdit) {
            throw RuntimeException("$context must implement OnTaskEdit")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: called")
        super.onCreate(savedInstanceState)
        viewModel.cursor.observe(this, Observer { cursor -> mAdapter.swapCursor(cursor)?.close() })
    }

    override fun onCreateView(
             inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: called")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: called")
//        view.findViewById<Button>(R.id.button_first).setOnClickListener {
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
//        }

        task_list.layoutManager = LinearLayoutManager(context)
        task_list.adapter = mAdapter
    }

    override fun onEditClick(task: Task) {
        (activity as OnTaskEdit?)?.onTaskEdit(task)
    }

    override fun onDeleteClick(task: Task) {
        val args = Bundle().apply {
            putInt(DIALOG_ID, DIALOG_ID_DELETE)
            putString(DIALOG_MESSAGE, getString(R.string.deldiag_message, task.id, task.name))
            putInt(DIALOG_POSITIVE_RID, R.string.deldiag_positive_caption)
            putLong(DIALOG_TASK_ID, task.id)     // pass the id in the arguments, so we can retrieve it when we get called back.
        }
        val dialog = AppDialog()
        dialog.arguments = args
        dialog.show(childFragmentManager, null)
    }

    override fun onTaskLongClick(task: Task) {
        Log.d(TAG, "onTaskLongClick: called")
        viewModel.timeTask(task)
    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        Log.d(TAG, "onPositiveDialogResult: called")

        if (dialogId == DIALOG_ID_DELETE) {
            val taskId = args.getLong(DIALOG_TASK_ID)
            if (BuildConfig.DEBUG && taskId == 0L) throw AssertionError("task ID is zero")
            viewModel.deleteTask(taskId)
        }
    }

    override fun onNegativeDialogResult(dialogId: Int, args: Bundle) {
        Log.d(TAG, "onNegativeDialogResult: called")
    }

    override fun onDialogCancelled(dialogId: Int) {
        Log.d(TAG, "onDialogCancelled: called")
    }

    interface OnTaskEdit {
        fun onTaskEdit(task: Task)
    }
}