package com.example.prm1.model

import androidx.annotation.DrawableRes

data class Task(
    val id: Long,
    val name: String,
    val subTasks: List<String>,
    @DrawableRes
    val resId: Int
)
