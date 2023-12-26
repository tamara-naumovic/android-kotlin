package com.example.myapplication.utils.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.EachTodoItemBinding
import com.example.myapplication.utils.model.ToDoData

class TaskAdapter(private val list: MutableList<ToDoData>) : RecyclerView.Adapter<TaskAdapter.ToDoViewHolder>(){
    private val TAG = "TaskAdapter"
    private var listener:TaskAdapterClickInterface? = null
    fun setListener(listener:TaskAdapterClickInterface){
        this.listener = listener
    }
    inner class ToDoViewHolder(val binding:EachTodoItemBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val binding = EachTodoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ToDoViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        with(holder) {
            with(list[position]) {
                binding.todoTask.text = this.task

                Log.d(TAG, "onBindViewHolder: "+this)
                binding.editTask.setOnClickListener {
                    listener?.onEditItemClicked(this , position)
                }

                binding.deleteTask.setOnClickListener {
                    listener?.onDeleteItemClicked(this , position)
                }
            }
        }
    }
    interface TaskAdapterClickInterface{
        fun onDeleteItemClicked(toDoData: ToDoData , position : Int)
        fun onEditItemClicked(toDoData: ToDoData , position: Int)
    }

}