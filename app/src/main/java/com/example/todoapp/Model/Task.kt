package com.example.todoapp.Model

data class Task(
    val id: Int,
    val title: String,
    val content: String,
    var status: String,
    val date: String,
    val time: String
)
