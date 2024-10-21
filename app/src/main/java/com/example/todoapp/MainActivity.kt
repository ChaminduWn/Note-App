package com.example.todoapp

import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.Model.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*



class MainActivity : AppCompatActivity() {

    private lateinit var taskManager: TaskSharedPreferencesManager
    private lateinit var adapter: TaskAdapter
    private lateinit var calendarDialog: Dialog
    private lateinit var focusDialog: Dialog
    private lateinit var upcoming_task: Dialog
    private lateinit var focusTimer: CountDownTimer
    private lateinit var circularProgressBar: ProgressBar
    private var isTimerRunning = false
    private val focusDurationInMillis = 25 * 60 * 1000L // 25 minutes
    private var timeRemaining = focusDurationInMillis

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize TaskSharedPreferencesManager
        taskManager = TaskSharedPreferencesManager(this)

        // Check for the next upcoming task
        scheduleNextReminder()

        // Initialize RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recycleview)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Retrieve the list of tasks and set the adapter
        val taskList = taskManager.getTaskList()
        adapter = TaskAdapter(taskList) { task ->
            // Handle task item click if needed
        }
        recyclerView.adapter = adapter

        // Find the FloatingActionButton and set an OnClickListener
        val fab: FloatingActionButton = findViewById(R.id.flotbuttton)
        fab.setOnClickListener {
            // Navigate to the ManageTask activity
            val intent = Intent(this, ManageTask::class.java)
            startActivity(intent)
        }

        // Initialize the calendar dialog
        calendarDialog = Dialog(this)
        calendarDialog.setContentView(R.layout.dialog_calendar) // Create a custom layout for the dialog
        calendarDialog.setCancelable(true) // Allow dismissing the dialog

        // Handle calendar icon click to toggle the dialog
        val calendarButton: ImageView = findViewById(R.id.calenderButton)
        calendarButton.setOnClickListener {
            if (calendarDialog.isShowing) {
                calendarDialog.dismiss()
            } else {
                calendarDialog.show()
            }
        }
              //for countdown
        upcoming_task = Dialog(this)
        upcoming_task.setContentView(R.layout.dialog_upcoming_tasks) // Create a custom layout for the dialog
        upcoming_task.setCancelable(true) // Allow dismissing the dialog


        val task24Button: ImageView = findViewById(R.id.uptaskButton)

        task24Button.setOnClickListener {
            val upcomingTasks = taskManager.getTasksWithinNext24Hours()

            if (upcomingTasks.isNotEmpty()) {
                val dialog = Dialog(this)
                dialog.setContentView(R.layout.dialog_upcoming_tasks) // Ensure this layout can handle a list of tasks
                dialog.setCancelable(true)

                val taskContainer: LinearLayout = dialog.findViewById(R.id.taskContainer) // This should be a LinearLayout in your dialog layout
                val closeButton: Button = dialog.findViewById(R.id.closePopupButton)

                // Loop through the upcoming tasks and add views dynamically
                for (task in upcomingTasks) {
                    val taskView = layoutInflater.inflate(R.layout.task_item_view, taskContainer, false) // task_item_view should be a layout representing each task

                    val taskTitleText: TextView = taskView.findViewById(R.id.upcomingTaskTitle)
                    val countdownText: TextView = taskView.findViewById(R.id.taskCountdown)

                    taskTitleText.text = task.title

                    // Calculate and display the countdown in hours for each task
                    val hoursRemaining = getHoursUntilTask(task)
                    countdownText.text = "Countdown: $hoursRemaining hours"

                    taskContainer.addView(taskView) // Add the task view to the container
                }

                closeButton.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
            }
        }

        // Initialize the focus dialog for focus timer
        focusDialog = Dialog(this)
        focusDialog.setContentView(R.layout.dialog_focus_time) // Custom layout for focus widget
        focusDialog.setCancelable(true) // Allow dismissing the dialog

        val focusButton: ImageView = findViewById(R.id.focusButton)
        focusButton.setOnClickListener {
            if (!focusDialog.isShowing) {
                focusDialog.show()

                // Initialize views inside focus dialog
                val focusTimerText: TextView = focusDialog.findViewById(R.id.focusTimerText)
                val startButton: Button = focusDialog.findViewById(R.id.startFocusButton)
                val stopButton: Button = focusDialog.findViewById(R.id.stopFocusButton)
                circularProgressBar = focusDialog.findViewById(R.id.circularProgressBar)

                // Start the timer
                startButton.setOnClickListener {
                    if (!isTimerRunning) {
                        startFocusTimer(focusTimerText)
                    }
                }

                // Stop the timer
                stopButton.setOnClickListener {
                    if (isTimerRunning) {
                        stopFocusTimer()
                    }
                }
            }
        }

        // Attach swipe handler to RecyclerView
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false // We don't want drag-and-drop, only swipe
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val task = taskList[position]

                if (direction == ItemTouchHelper.LEFT) {
                    // Swipe left: Edit the task
                    val intent = Intent(this@MainActivity, UpdateTask::class.java)
                    intent.putExtra("taskId", task.id)
                    intent.putExtra("taskTitle", task.title)
                    intent.putExtra("taskContent", task.content)
                    intent.putExtra("taskDate", task.date)
                    intent.putExtra("taskTime", task.time)
                    intent.putExtra("taskStatus", task.status)
                    startActivity(intent)
                } else if (direction == ItemTouchHelper.RIGHT) {
                    // Swipe right: Confirm before deleting the task
                    showDeleteConfirmationDialog(task, position)
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val background = ColorDrawable()

                if (dX > 0) {
                    // Right swipe (delete)
                    background.color = Color.LTGRAY
                    background.setBounds(
                        itemView.left, itemView.top,
                        itemView.left + dX.toInt(), itemView.bottom
                    )
                } else {
                    // Left swipe (edit)
                    background.color = Color.LTGRAY
                    background.setBounds(
                        itemView.right + dX.toInt(), itemView.top,
                        itemView.right, itemView.bottom
                    )
                }

                background.draw(c)

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        // Attach the ItemTouchHelper to the RecyclerView
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun startFocusTimer(focusTimerText: TextView) {
        focusTimer = object : CountDownTimer(timeRemaining, 1000) { // Remaining time or 25 minutes
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = millisUntilFinished / 1000 % 60
                focusTimerText.text = String.format("%02d:%02d", minutes, seconds)

                // Update the ProgressBar
                val progress = ((focusDurationInMillis - millisUntilFinished).toFloat() / focusDurationInMillis * 100).toInt()
                circularProgressBar.progress = progress
            }

            override fun onFinish() {
                focusTimerText.text = "00:00"
                isTimerRunning = false
                circularProgressBar.progress = 100 // Full completion
            }
        }.start()
        isTimerRunning = true
    }

    private fun stopFocusTimer() {
        focusTimer.cancel()
        isTimerRunning = false
        timeRemaining = focusDurationInMillis // Reset time
        circularProgressBar.progress = 0 // Reset progress
    }

    private fun scheduleNextReminder() {
        val nextTask = taskManager.getNextUpcomingTask()
        nextTask?.let { task ->
            scheduleReminder(task)
        }
    }


    private fun scheduleReminder(task: Task) {
        // Parse the task's date and time
        val calendar = Calendar.getInstance().apply {
            val taskDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse("${task.date} ${task.time}")
            taskDateTime?.let {
                timeInMillis = it.time
            }
        }

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderBroadcastReceiver::class.java).apply {
            putExtra("taskId", task.id)
            putExtra("taskTitle", task.title)
            putExtra("taskContent", task.content)
        }

        val pendingIntent = PendingIntent.getBroadcast(this, task.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Schedule the reminder at the task's time
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    // Function to show delete confirmation dialog
    private fun showDeleteConfirmationDialog(task: Task, position: Int) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Are you sure you want to delete the task '${task.title}'?")
            .setCancelable(false)
            .setPositiveButton("Delete") { _, _ ->
                // Delete the task
                taskManager.deleteTask(task.id)
                adapter.updateTaskList(taskManager.getTaskList()) // Refresh the task list after deletion
                scheduleNextReminder() // Reschedule the next reminder
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // If user cancels, reset the item position
                adapter.notifyItemChanged(position)
                dialog.dismiss()
            }

        val alert = dialogBuilder.create()
        alert.setTitle("Delete Task")
        alert.show()
    }
}
private fun getHoursUntilTask(task: Task): Long {
    val taskDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse("${task.date} ${task.time}")
    val currentTime = Calendar.getInstance().time

    return if (taskDateTime != null) {
        val diffInMillis = taskDateTime.time - currentTime.time
        diffInMillis / (1000 * 60 * 60) // Convert milliseconds to hours
    } else {
        0
    }
}
