package com.example.elearningapplication

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class QuizSetupActivity : AppCompatActivity() {

    // Variable to track what the user selected
    private var selectedDifficulty = "Medium"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_setup)

        val tvSubjectName = findViewById<TextView>(R.id.tvSubjectName)
        val btnEasy = findViewById<MaterialButton>(R.id.btnEasy)
        val btnMedium = findViewById<MaterialButton>(R.id.btnMedium)
        val btnHard = findViewById<MaterialButton>(R.id.btnHard)
        val btnBeginAssessment = findViewById<MaterialButton>(R.id.btnBeginAssessment)

        val btnNavHome = findViewById<MaterialButton>(R.id.btnNavHome)

        // 1. Get the subject name from the intent and display it
        val subjectName = intent.getStringExtra("SUBJECT_NAME") ?: "General"
        tvSubjectName.text = subjectName

        // --- Difficulty Selection Logic ---

        // Helper function to reset all buttons to the "unselected" grey state
        fun resetButtons() {
            val unselectedColor = getColor(R.color.surface_container_low)
            val unselectedTextColor = getColor(R.color.outline)

            btnEasy.setBackgroundColor(unselectedColor)
            btnEasy.setTextColor(unselectedTextColor)
            btnEasy.setIconResource(android.R.drawable.radiobutton_off_background)
            btnEasy.iconTint = getColorStateList(R.color.outline)
            btnEasy.strokeWidth = 0

            btnMedium.setBackgroundColor(unselectedColor)
            btnMedium.setTextColor(unselectedTextColor)
            btnMedium.setIconResource(android.R.drawable.radiobutton_off_background)
            btnMedium.iconTint = getColorStateList(R.color.outline)
            btnMedium.strokeWidth = 0

            btnHard.setBackgroundColor(unselectedColor)
            btnHard.setTextColor(unselectedTextColor)
            btnHard.setIconResource(android.R.drawable.radiobutton_off_background)
            btnHard.iconTint = getColorStateList(R.color.outline)
            btnHard.strokeWidth = 0
        }

        // Helper function to set a button to the "selected" blue state
        fun selectButton(button: MaterialButton, difficulty: String) {
            resetButtons()
            selectedDifficulty = difficulty

            button.setBackgroundColor(getColor(R.color.primary_fixed))
            button.setTextColor(getColor(R.color.primary))
            button.setIconResource(android.R.drawable.radiobutton_on_background)
            button.iconTint = getColorStateList(R.color.primary)
            button.strokeColor = getColorStateList(R.color.primary)
            button.strokeWidth = 4 // 2dp equivalent
        }

        // Click listeners for the three difficulties
        btnEasy.setOnClickListener { selectButton(btnEasy, "Easy") }
        btnMedium.setOnClickListener { selectButton(btnMedium, "Medium") }
        btnHard.setOnClickListener { selectButton(btnHard, "Hard") }

        // --- Start Quiz Logic ---
        btnBeginAssessment.setOnClickListener {
            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra("SUBJECT_NAME", subjectName)
            intent.putExtra("DIFFICULTY_LEVEL", selectedDifficulty)
            startActivity(intent)
        }

        // Bottom Navigation Logic
        btnNavHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish() // Close this setup screen
        }
    }
}