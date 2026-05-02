package com.example.elearningapplication

import java.io.Serializable

data class Question(
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String
) : Serializable