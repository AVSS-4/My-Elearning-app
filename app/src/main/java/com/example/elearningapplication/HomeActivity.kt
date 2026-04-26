package com.example.elearningapplication

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Find UI elements
        val etSearch = findViewById<TextInputEditText>(R.id.etSearch)
        val searchBarLayout = findViewById<TextInputLayout>(R.id.searchBarLayout)
        val cardComputerScience = findViewById<MaterialCardView>(R.id.cardComputerScience)
        val btnCSQuiz = findViewById<MaterialButton>(R.id.btnCSQuiz)
        val btnNavProfile = findViewById<MaterialButton>(R.id.btnNavProfile)

        // Search Logic
        etSearch.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                performSearch(etSearch.text.toString())
                true
            } else {
                false
            }
        }

        searchBarLayout.setEndIconOnClickListener {
            performSearch(etSearch.text.toString())
        }

        // Navigation
        btnNavProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        cardComputerScience.setOnClickListener { goToSetup("Computer Science") }
        btnCSQuiz.setOnClickListener { goToSetup("Computer Science") }
    }

    private fun performSearch(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isNotEmpty()) {
            goToSetup(trimmedQuery)
        } else {
            Toast.makeText(this, "Please enter a subject name", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goToSetup(subjectName: String) {
        val intent = Intent(this, QuizSetupActivity::class.java)
        intent.putExtra("SUBJECT_NAME", subjectName)
        startActivity(intent)
    }
}