package com.example.prm1.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.prm1.data.model.TaskEntity

@Database(
    entities = [TaskEntity::class],
    version = 1
)
abstract class TaskDatabase : RoomDatabase() {
    abstract  val tasks: TaskDao

    companion object {
        fun open(context: Context): TaskDatabase = Room.databaseBuilder(
            context, TaskDatabase::class.java, "tasks.db"
        ).build()
    }
}