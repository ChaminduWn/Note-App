package com.example.todoapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import android.widget.TextView
import com.example.todoapp.Model.Task
import java.util.*

class UpdateTask : AppCompatActivity() {

    private lateinit var taskManager: TaskSharedPreferencesManager
    private lateinit var titleInput: EditText
    private lateinit var contentInput: EditText
    private lateinit var updateButton: Button
    private lateinit var dateText: TextView
    private lateinit var timeText: TextView
    private lateinit var pickDateButton: Button
    private lateinit var pickTimeButton: Button
    private lateinit var statusSwitch: SwitchCompat
    private var selectedDate: String = "2024-09-22" // Default date
    private var selectedTime: String = "12:00" // Default time
    private var taskId: Int? = null
    private var taskStatus: String = "in progress"
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_task)

        // Initialize components
        taskManager = TaskSharedPreferencesManager(this)
        titleInput = findViewById(R.id.titleInput)
        contentInput = findViewById(R.id.contentInput)
        updateButton = findViewById(R.id.updateButton)
        dateText = findViewById(R.id.dateText)
        timeText = findViewById(R.id.timeText)
        pickDateButton = findViewById(R.id.pickDateButton)
        pickTimeButton = findViewById(R.id.pickTimeButton)
        statusSwitch = findViewById(R.id.statusSwitch)
        cancelButton = findViewById(R.id.cancel)

        // Get task data passed from MainActivity
        taskId = intent.getIntExtra("taskId", -1)
        if (taskId != -1) {
            titleInput.setText(intent.getStringExtra("taskTitle"))
            contentInput.setText(intent.getStringExtra("taskContent"))
            dateText.text = intent.getStringExtra("taskDate")
            timeText.text = intent.getStringExtra("taskTime")
            selectedDate = intent.getStringExtra("taskDate") ?: selectedDate
            selectedTime = intent.getStringExtra("taskTime") ?: selectedTime
            taskStatus = intent.getStringExtra("taskStatus") ?: "in progress"
        }

        // Set switch based on current status
        statusSwitch.isChecked = taskStatus == "complete"
        statusSwitch.text = if (taskStatus == "complete") "Complete" else "In Progress"

        // Set up DatePicker and TimePicker
        pickDateButton.setOnClickListener { showDatePicker() }
        pickTimeButton.setOnClickListener { showTimePicker() }

        // Handle status switch toggle
        statusSwitch.setOnCheckedChangeListener { _, isChecked ->
            taskStatus = if (isChecked) "complete" else "in progress"
            statusSwitch.text = if (isChecked) "Complete" else "In Progress"
        }

        cancelButton.setOnClickListener {
            // Navigate back to MainActivity when cancel is clicked
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Handle updating the task
        updateButton.setOnClickListener {
            val updatedTitle = titleInput.text.toString()
            val updatedContent = contentInput.text.toString()

            if (updatedTitle.isNotEmpty() && updatedContent.isNotEmpty()) {
                val updatedTask = Task(
                    id = taskId!!,
                    title = updatedTitle,
                    content = updatedContent,
                    status = taskStatus,
                    date = selectedDate,
                    time = selectedTime
                )

                taskManager.updateTask(updatedTask)
                updateWidget() // Update widget after task update

                // Return to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Finish this activity to remove it from the back stack
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
            selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            dateText.text = selectedDate
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // Create a TimePickerDialog
        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            timeText.text = selectedTime
        }, hour, minute, true)

        timePickerDialog.show()
    }
}
