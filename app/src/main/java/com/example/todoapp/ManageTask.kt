package com.example.todoapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.todoapp.Model.Task
import java.util.Calendar

class ManageTask : AppCompatActivity() {

    private lateinit var taskManager: TaskSharedPreferencesManager
    private lateinit var titleInput: EditText
    private lateinit var contentInput: EditText
    private lateinit var addButton: Button
    private lateinit var dateText: TextView
    private lateinit var pickDateButton: Button
    private lateinit var timeText: TextView // TextView for displaying selected time
    private lateinit var pickTimeButton: Button // Button for picking time
    private var selectedDate: String = "2024-09-22" // Default date
    private var selectedTime: String = "12:00" // Default time
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_task)

        // Initialize components
        taskManager = TaskSharedPreferencesManager(this)
        titleInput = findViewById(R.id.titleInput)
        contentInput = findViewById(R.id.contentInput)
        addButton = findViewById(R.id.addButton)
        dateText = findViewById(R.id.dateText)
        pickDateButton = findViewById(R.id.pickDateButton)
        timeText = findViewById(R.id.timeText) // Initialize timeText
        pickTimeButton = findViewById(R.id.pickTimeButton) // Initialize pickTimeButton
        cancelButton = findViewById(R.id.cancel)

        // Set up button click listener for picking a date
        pickDateButton.setOnClickListener {
            showDatePicker()
        }

        // Set up button click listener for picking a time
        pickTimeButton.setOnClickListener {
            showTimePicker()
        }

        cancelButton.setOnClickListener {
            // Navigate back to MainActivity when cancel is clicked
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Set up button click listener for adding a task
        addButton.setOnClickListener {
            val taskTitle = titleInput.text.toString()
            val taskContent = contentInput.text.toString()

            if (taskTitle.isNotEmpty() && taskContent.isNotEmpty()) {
                // Create a new task with the selected date and time
                val newTask = Task(
                    id = taskManager.getTaskList().size + 1,
                    title = taskTitle,
                    content = taskContent,
                    status = "in progress", // Set default status
                    date = selectedDate, // Use the selected date
                    time = selectedTime // Use the selected time
                )

                // Add the task
                taskManager.addTask(newTask)

                // Update the widget to reflect the new task
                updateWidget()

                // Clear the input fields
                titleInput.text.clear()
                contentInput.text.clear()
                dateText.text = "Select Date" // Reset date text
                timeText.text = "Select Time" // Reset time text

                // Navigate back to MainActivity after adding the task
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

                // Finish the current activity to prevent going back
                finish()
            }
        }
    }

    private fun updateWidget() {
        val intent = Intent(this, ReminderWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(this).getAppWidgetIds(ComponentName(this, ReminderWidgetProvider::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Create a DatePickerDialog
        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            // Format the date as "YYYY-MM-DD"
            selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            dateText.text = selectedDate // Update the TextView to show selected date
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // Create a TimePickerDialog
        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            // Format the time as "HH:MM"
            selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            timeText.text = selectedTime // Update the TextView to show selected time
        }, hour, minute, true)

        timePickerDialog.show()
    }
}
