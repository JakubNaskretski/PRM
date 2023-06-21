package com.example.prm1.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.prm1.TaskCallback
import com.example.prm1.databinding.ListItemBinding
import com.example.prm1.model.Task
import kotlin.concurrent.thread
import kotlin.math.log

class TaskViewHolder(val binding : ListItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(task: Task) {
        binding.taskName.text = task.name
        binding.subTasks.text = task.subTasks.joinToString(",\n")
        binding.image.setImageResource(task.resId)
    }
}

class TasksAdapter : RecyclerView.Adapter<TaskViewHolder>() {
    private val data = mutableListOf<Task>()
    var onItemClick: (Long) -> Unit = {}
    var onLongClick: (Int) -> Unit = {}
    var onShareClick: (Int) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding).also { vh ->
            Log.d("Test", vh.toString());
            binding.root.setOnClickListener {
                onItemClick(data[vh.layoutPosition].id)
            }
            binding.root.setOnLongClickListener {
                onLongClick(vh.layoutPosition)
                true
            }
        }
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    fun replace(newData: List<Task>) {
        val callback = TaskCallback(data.toList(), newData)
        data.clear()
        data.addAll(newData)
        val result = DiffUtil.calculateDiff(callback)
        result.dispatchUpdatesTo(this)

    }

    fun sort() {
        val notSorted = data.toList()
        data.sortBy { it.name }
        val callback = TaskCallback(notSorted, data)
        val result = DiffUtil.calculateDiff(callback)
        result.dispatchUpdatesTo(this)
    }

    fun removeItem(layoutPosition: Int): Task {
        val task = data.removeAt(layoutPosition)
        notifyItemRemoved(layoutPosition)
        return task
    }

}