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

private const val TAG = "MainActivityFragment"
/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MainActivityFragment : Fragment(),
CursorRecyclerViewAdapter.OnTaskClickListener{

    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(TaskTimerViewModel::class.java) }
    private val mAdapter = CursorRecyclerViewAdapter(null, this)

    /**
     * Called when a fragment is first attached to its context.
     * [.onCreate] will be called after this.
     */
    override fun onAttach(context: Context) {
        Log.d(TAG, "onAttach: called")
        super.onAttach(context)

        if (context !is OnTaskEdit) {
            throw RuntimeException("${context.toString()} must implement OnTaskEdit")
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
        viewModel.deleteTask(task.id)
    }

    override fun onTaskLongClick(task: Task) {
        TODO("Not yet implemented")
    }

    interface OnTaskEdit {
        fun onTaskEdit(task: Task)
    }
}