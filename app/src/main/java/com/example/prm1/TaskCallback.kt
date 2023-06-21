package com.example.prm1

import androidx.recyclerview.widget.DiffUtil
import com.example.prm1.model.Task

class TaskCallback(val notSorted: List<Task>, val sorted: List<Task>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = notSorted.size

    override fun getNewListSize(): Int = sorted.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        notSorted[oldItemPosition] === sorted[newItemPosition]

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        notSorted[oldItemPosition] == sorted[newItemPosition]
}