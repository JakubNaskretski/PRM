package com.example.prm1.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.prm1.data.model.TaskEntity

@Dao
interface TaskDao {

    @Query("SELECT * FROM task;")
    fun getAll(): List<TaskEntity>

    @Query("SELECT * FROM task WHERE id = :id;")
    fun getTask(id: Long): TaskEntity

    @Query("SELECT * FROM task ORDER BY name ASC;")
    fun getAllSortedByName(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addTask(newTask : TaskEntity)

    @Update
    fun updateTask(newTask: TaskEntity)

    @Query("DELETE FROM task WHERE id = :id;")
     fun remove(id: Long)
}