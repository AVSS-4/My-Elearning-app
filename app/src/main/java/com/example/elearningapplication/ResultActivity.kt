package com.example.elearningapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import java.text.SimpleDateFormat
import java.util.Date

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // 1. Get the data passed from QuizActivity
        val score = intent.getIntExtra("FINAL_SCORE", 0)
        val totalQuestions = intent.getIntExtra("TOTAL_QUESTIONS", 10)
        val subject = intent.getStringExtra("SUBJECT_NAME") ?: "General"

        val sharedPref = getSharedPreferences("quiz_history", MODE_PRIVATE)
        val editor = sharedPref.edit()

        val oldData = sharedPref.getString("history", "") ?: ""

        val date = SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date())

        val newEntry = "📘 $subject: $score/$totalQuestions\n🕒 $date\n\n"

        editor.putString("history", oldData + newEntry)
        editor.apply()

        // 2. Calculations
        val incorrect = totalQuestions - score
        val percentage = ((score.toFloat() / totalQuestions.toFloat()) * 100).toInt()

        // 3. UI Elements
        val circularProgress = findViewById<CircularProgressIndicator>(R.id.circularProgress)
        val tvPercentage = findViewById<TextView>(R.id.tvPercentage)
        val tvCorrect = findViewById<TextView>(R.id.tvCorrect)
        val tvIncorrect = findViewById<TextView>(R.id.tvIncorrect)
        val tvFeedbackHeadline = findViewById<TextView>(R.id.tvFeedbackHeadline)

        val btnRetake = findViewById<MaterialButton>(R.id.btnRetake)
        val btnBackHome = findViewById<MaterialButton>(R.id.btnBackHome)

        // 4. Update Main Stats
        circularProgress.setProgressCompat(percentage, true)
        tvPercentage.text = "$percentage%"
        tvCorrect.text = score.toString()
        tvIncorrect.text = incorrect.toString()

        // --- THE HUMAN & CATCHY FEEDBACK LOGIC ---
        tvFeedbackHeadline.text = when {
            percentage <= 25 -> "Take a break, grab a chai, and try again. ☕"
            percentage <= 50 -> "Keep it up! You're getting the hang of it. ✨"
            percentage <= 85 -> "Nicely done! You've got a solid grip on this. 🔥"
            else -> "Absolute Legend! You've totally nailed this quiz. 👑"
        }

        // 5. Save to Firestore
        saveResultToFirestore(score, totalQuestions, subject)

        // 6. Navigation
        btnRetake.setOnClickListener {
            val intent = Intent(this, QuizSetupActivity::class.java)
            intent.putExtra("SUBJECT_NAME", subject)
            startActivity(intent)
            finish()
        }

        btnBackHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun saveResultToFirestore(score: Int, total: Int, subject: String) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val user = auth.currentUser

        if (user != null) {
            val resultData = hashMapOf(
                "studentName" to (user.displayName ?: "Anonymous"),
                "studentEmail" to user.email,
                "score" to score,
                "totalQuestions" to total,
                "percentage" to ((score.toFloat() / total.toFloat()) * 100),
                "subject" to subject,
                "timestamp" to com.google.firebase.Timestamp(Date()),
                "feedbackGiven" to getFeedbackString(((score.toFloat() / total.toFloat()) * 100).toInt())
            )

            db.collection("quiz_results").add(resultData)
        }
    }

    // Helper function so the same feedback text is saved to Firestore for the teacher to see
    private fun getFeedbackString(pct: Int): String {
        return when {
            pct <= 25 -> "Needs improvement - Suggested a break."
            pct <= 50 -> "Showing progress."
            pct <= 85 -> "Strong performance."
            else -> "Mastery demonstrated."
        }
    }
}