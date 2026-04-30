package com.example.elearningapplication

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView


class QuizHistoryActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_quiz_history)
        val tvHistory = findViewById<TextView>(R.id.historyText)

        val sharedPref = getSharedPreferences("quiz_history", MODE_PRIVATE)
        val history = sharedPref.getString("history", "No history yet")

        tvHistory.text = history

        val btnClear = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnClear)

        btnClear.setOnClickListener {
            val sharedPref = getSharedPreferences("quiz_history", MODE_PRIVATE)
            sharedPref.edit().clear().apply()

            tvHistory.text = "No history yet"
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}