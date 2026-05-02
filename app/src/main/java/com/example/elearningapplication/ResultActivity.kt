package com.example.elearningapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // ✅ FIXED KEYS (VERY IMPORTANT)
        val score = intent.getIntExtra("score", 0)
        val totalQuestions = intent.getIntExtra("total", 10)
        val subject = intent.getStringExtra("SUBJECT_NAME") ?: "General"

        // ✅ SAVE HISTORY (LOCAL)
        val sharedPref = getSharedPreferences("quiz_history", MODE_PRIVATE)
        val editor = sharedPref.edit()

        val oldData = sharedPref.getString("history", "") ?: ""
        val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        val newEntry = "📘 $subject: $score/$totalQuestions\n🕒 $date\n\n"

        editor.putString("history", oldData + newEntry)
        editor.apply()

        // ✅ CALCULATIONS
        val incorrect = totalQuestions - score
        val percentage = if (totalQuestions > 0)
            ((score.toFloat() / totalQuestions.toFloat()) * 100).toInt()
        else 0

        // ✅ UI ELEMENTS
        val circularProgress = findViewById<CircularProgressIndicator>(R.id.circularProgress)
        val tvPercentage = findViewById<TextView>(R.id.tvPercentage)
        val tvCorrect = findViewById<TextView>(R.id.tvCorrect)
        val tvIncorrect = findViewById<TextView>(R.id.tvIncorrect)
        val tvFeedbackHeadline = findViewById<TextView>(R.id.tvFeedbackHeadline)

        val btnRetake = findViewById<MaterialButton>(R.id.btnRetake)
        val btnBackHome = findViewById<MaterialButton>(R.id.btnBackHome)

        // ✅ UPDATE UI
        circularProgress.setProgressCompat(percentage, true)
        tvPercentage.text = "$percentage%"
        tvCorrect.text = score.toString()
        tvIncorrect.text = incorrect.toString()

        tvFeedbackHeadline.text = when {
            percentage <= 25 -> "Take a break, grab a chai, and try again. ☕"
            percentage <= 50 -> "Keep it up! You're getting the hang of it. ✨"
            percentage <= 85 -> "Nicely done! You've got a solid grip on this. 🔥"
            else -> "Absolute Legend! You've totally nailed this quiz. 👑"
        }

        // ✅ SAVE TO FIRESTORE
        saveResultToFirestore(score, totalQuestions, subject)

        // ✅ BUTTONS
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
            val percentage = if (total > 0)
                (score.toFloat() / total.toFloat()) * 100
            else 0f

            val resultData = hashMapOf(
                "studentName" to (user.displayName ?: "Anonymous"),
                "studentEmail" to user.email,
                "score" to score,
                "totalQuestions" to total,
                "percentage" to percentage,
                "subject" to subject,
                "timestamp" to com.google.firebase.Timestamp(Date()),
                "feedbackGiven" to getFeedbackString(percentage.toInt())
            )

            db.collection("quiz_results")
                .add(resultData)
                .addOnSuccessListener {
                    Log.d("FIRESTORE", "Result saved successfully")
                }
                .addOnFailureListener {
                    Log.e("FIRESTORE", "Error saving result", it)
                }
        }
    }

    private fun getFeedbackString(pct: Int): String {
        return when {
            pct <= 25 -> "Needs improvement - Suggested a break."
            pct <= 50 -> "Showing progress."
            pct <= 85 -> "Strong performance."
            else -> "Mastery demonstrated."
        }
    }
}