package com.example.prm1.model

import androidx.annotation.DrawableRes

data class Task(
    val dbHash: String? = "",
    val id: Long? = 0,
    val name: String? = "",
    val subTasks: List<String> = emptyList(),
    @DrawableRes
    val resId: Int? = 0
)
