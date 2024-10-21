
package com.example.todoapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PopupActivity : AppCompatActivity() {

    private lateinit var taskTitleText: TextView
    private lateinit var taskContentText: TextView
    private lateinit var dismissButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_popup)

        taskTitleText = findViewById(R.id.taskTitleText)
        taskContentText = findViewById(R.id.taskContentText)
        dismissButton = findViewById(R.id.dismissButton)

        // Get task details from the intent
        val taskTitle = intent.getStringExtra("taskTitle") ?: "Task"
        val taskContent = intent.getStringExtra("taskContent") ?: "You have a task!"

        taskTitleText.text = taskTitle
        taskContentText.text = taskContent

        dismissButton.setOnClickListener {
            finish() // Close the popup
        }
    }
}
