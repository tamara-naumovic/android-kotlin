package com.example.myapplication.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentTodoDialogBinding
import com.example.myapplication.utils.model.ToDoData
import com.google.android.material.textfield.TextInputEditText


/**
 * A simple [Fragment] subclass.
 * Use the [TodoDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TodoDialogFragment : DialogFragment() {
    private lateinit var binding: FragmentTodoDialogBinding
    private var listener: OnDialogNextBtnClickListener?=null
    private var toDoData: ToDoData? = null

    fun setListener(listener: OnDialogNextBtnClickListener){
        this.listener = listener
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentTodoDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments != null){
            val taskId = arguments?.getString("taskId").toString()
            val taskContent = arguments?.getString("task").toString()
            toDoData = ToDoData( taskId, taskContent)
            binding.todoEt.setText(toDoData?.task)
        }
        createTaskOptions()
    }

    private fun createTaskOptions(){
        //dismiss
        binding.todoClose.setOnClickListener {
            dismiss()
        }

        //create+update
        binding.todoNextBtn.setOnClickListener {
            val todoTask = binding.todoEt.text.toString() //sa textInput polja vrednost
            if (todoTask.isNotEmpty()) { //ako nije prazan string
                if (toDoData == null) { //ako u tododata nema nista (u tom slucaju je kreiranje)
                    listener?.onSaveTask(
                        todoTask,
                        binding.todoEt
                    ) //safe call na metodu koja prima non-nullable
                    //a mi smo listener potencijalno definisali kao null na samom početku
                } else { //ako u tododata ima nesto to znaci da je u pitanju azuriranje
                    toDoData!!.task = todoTask
                    listener?.onUpdateTask(toDoData!!, binding.todoEt)
                }
            } else
                Toast.makeText(context, "Unesite task", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {

        const val TAG = "ToDoDialogFragment"
        @JvmStatic
        fun newInstance(taskId: String, task: String) =
            TodoDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("taskId", taskId)
                    putString("task", task)
                }
            }
    }
    interface OnDialogNextBtnClickListener{
        fun onSaveTask(todoTask:String , todoEt: TextInputEditText)
        fun onUpdateTask(toDoData: ToDoData, todoEdit: TextInputEditText)
    }
}