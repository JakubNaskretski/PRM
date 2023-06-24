package com.example.prm1.adapters

data class TaskDbObj (
    val id: Long? = 0,
    val name: String? = "",
    val subTasks: List<String> = emptyList(),
    val resName: String? = ""
)
