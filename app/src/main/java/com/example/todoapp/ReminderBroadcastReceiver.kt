package com.example.todoapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.todoapp.Model.Task

class ReminderBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Get task details from the intent
        val taskId = intent.getIntExtra("taskId", -1)
        val taskTitle = intent.getStringExtra("taskTitle") ?: "Task"
        val taskContent = intent.getStringExtra("taskContent") ?: "You have a task!"

        // Create the notification
        createNotification(context, taskTitle, taskContent, taskId)

        // Launch PopupActivity to show task details
        launchPopupActivity(context, taskId, taskTitle, taskContent)
    }

    private fun createNotification(context: Context, title: String, content: String, taskId: Int) {
        val notificationId = taskId

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "task_reminder_channel"
            val channelName = "Task Reminders"
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        // Create an Intent for the notification click
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("taskId", taskId) // Pass the task ID if you want to open a specific task
        }
        val pendingIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Create the notification
        val notificationBuilder = NotificationCompat.Builder(context, "task_reminder_channel")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Set the pending intent

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun launchPopupActivity(context: Context, taskId: Int, taskTitle: String, taskContent: String) {
        val popupIntent = Intent(context, PopupActivity::class.java).apply {
            putExtra("taskId", taskId)
            putExtra("taskTitle", taskTitle)
            putExtra("taskContent", taskContent)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK // Launch activity in a new task
        }
        context.startActivity(popupIntent) // Start the PopupActivity
    }
}
