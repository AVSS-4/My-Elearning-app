package com.example.elearningapplication

data class QuestionModel(
    val question: String = "",
    val options: List<String> = listOf(),
    val answer: String = ""
)