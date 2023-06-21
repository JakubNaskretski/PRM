package com.example.prm1.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.prm1.R
import com.example.prm1.databinding.ListItemBinding
import com.example.prm1.databinding.TaskImageBinding

class TaskImageViewHolder(val binding : TaskImageBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(resId: Int, isSelected: Boolean) {
        binding.image.setImageResource(resId)
        binding.selectedFrame.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
    }
}

class TaskImagesAdapter : RecyclerView.Adapter<TaskImageViewHolder>() {
    private val images = listOf(R.drawable.bell, R.drawable.phone, R.drawable.message)
    private var selectedPosition: Int = 0
    val selectedIdRes: Int
        get() = images[selectedPosition]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskImageViewHolder {
        val binding = TaskImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskImageViewHolder(binding).also {vh ->
            binding.root.setOnClickListener() {
                setSelected(vh.layoutPosition)
            }
        }
    }

    private fun setSelected(layoutPosition: Int) {
        notifyItemChanged(selectedPosition)
        selectedPosition = layoutPosition
        notifyItemChanged(selectedPosition)
    }

    override fun onBindViewHolder(holder: TaskImageViewHolder, position: Int) {
        holder.bind(images[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = images.size

    fun setSelection(icon: Int?) {
        val index = images.indexOfFirst { it == icon }
        if (index == -1) return
        setSelected(index)
    }

}