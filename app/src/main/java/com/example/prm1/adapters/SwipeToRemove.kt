package com.example.prm1.adapters

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class SwipeToRemove(
    private val onSwipe: (id: Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.END or ItemTouchHelper.START) {
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        onSwipe(viewHolder.layoutPosition)
    }

}