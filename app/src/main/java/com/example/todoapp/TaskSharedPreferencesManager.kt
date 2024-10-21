package com.example.todoapp

import android.content.Context
import android.content.SharedPreferences
import com.example.todoapp.Model.Task
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class TaskSharedPreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("task_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        const val TASK_LIST_KEY = "task_list"
    }

    // Save a list of tasks to SharedPreferences
    fun saveTaskList(taskList: List<Task>) {
        val jsonString = gson.toJson(taskList)
        with(sharedPreferences.edit()) {
            putString(TASK_LIST_KEY, jsonString)
            apply()
        }
    }

    // Retrieve the task list from SharedPreferences
    fun getTaskList(): List<Task> {
        val jsonString = sharedPreferences.getString(TASK_LIST_KEY, null)
        return if (jsonString != null) {
            val type = object : TypeToken<List<Task>>() {}.type
            gson.fromJson(jsonString, type) ?: emptyList() // Safeguard against null
        } else {
            emptyList()
        }
    }

    // Get tasks scheduled within the next 24 hours
    fun getTasksWithinNext24Hours(): List<Task> {
        val taskList = getTaskList()
        val currentTime = Calendar.getInstance().time
        val next24Hours = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 24) }.time

        return taskList.filter { task ->
            val taskDateTime = parseTaskDateTime(task)
            taskDateTime != null && taskDateTime.after(currentTime) && taskDateTime.before(next24Hours)
        }
    }

    // Add a new task
    fun addTask(newTask: Task) {
        val taskList = getTaskList().toMutableList()
        taskList.add(newTask)
        saveTaskList(taskList)
    }

    // Update an existing task
    fun updateTask(updatedTask: Task) {
        val taskList = getTaskList().toMutableList()
        val index = taskList.indexOfFirst { it.id == updatedTask.id }
        if (index != -1) {
            taskList[index] = updatedTask
            saveTaskList(taskList)
        }
    }

    // Delete a task by ID
    fun deleteTask(taskId: Int) {
        val taskList = getTaskList().toMutableList()
        taskList.removeAll { it.id == taskId }
        saveTaskList(taskList)
    }

    // Get the next upcoming task
    fun getNextUpcomingTask(): Task? {
        val taskList = getTaskList()
        val currentDateTime = Calendar.getInstance().time

        return taskList.asSequence()
            .mapNotNull { task -> parseTaskDateTime(task)?.let { task to it } }
            .filter { (_, taskDateTime) -> taskDateTime.after(currentDateTime) }
            .minByOrNull { (_, taskDateTime) -> taskDateTime.time }
            ?.first // Return only the task
    }

    // Helper function to parse task date and time
    private fun parseTaskDateTime(task: Task): Date? {
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse("${task.date} ${task.time}")
    }
}
