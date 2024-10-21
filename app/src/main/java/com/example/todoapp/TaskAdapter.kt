package com.example.todoapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.Model.Task

class TaskAdapter(
    private var tasks: List<Task>,
    private val onTaskClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.taskTitle)
        val content: TextView = itemView.findViewById(R.id.taskContent)
        val status: TextView = itemView.findViewById(R.id.taskStatus)
        val date: TextView = itemView.findViewById(R.id.taskDate)
        val time: TextView = itemView.findViewById(R.id.taskTime) // New TextView for time
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.title.text = task.title
        holder.content.text = task.content
        holder.status.text = "Status: ${task.status}"
        holder.date.text = "Date: ${task.date}"
        holder.time.text = "Time: ${task.time}" // Set the task time

        // Set click listener for task item
        holder.itemView.setOnClickListener {
            onTaskClick(task)
        }
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    fun updateTaskList(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}
