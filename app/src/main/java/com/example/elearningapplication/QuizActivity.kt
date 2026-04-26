package com.example.elearningapplication

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QuizActivity : AppCompatActivity() {

    private var currentQuestionIndex = 0
    private var score = 0
    private var selectedOptionIndex: Int? = null
    private var correctAnswerText: String = ""

    private var activeQuestions: MutableList<QuestionModel> = mutableListOf()
    private var currentOptions: List<String> = listOf()
    private var countDownTimer: CountDownTimer? = null

    private lateinit var tvTimer: TextView
    private lateinit var tvQuestionTracker: TextView
    private lateinit var tvQuestionText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var loadingProgress: ProgressBar
    private lateinit var btnOptions: List<MaterialButton>

    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        tvTimer = findViewById(R.id.tvTimer)
        tvQuestionTracker = findViewById(R.id.tvQuestionTracker)
        tvQuestionText = findViewById(R.id.tvQuestionText)
        progressBar = findViewById(R.id.progressBar)
        loadingProgress = findViewById(R.id.progressBarLoading)

        val btnClose = findViewById<ImageButton>(R.id.btnClose)
        val btnNext = findViewById<MaterialButton>(R.id.btnNext)

        btnOptions = listOf(
            findViewById(R.id.btnOption0),
            findViewById(R.id.btnOption1),
            findViewById(R.id.btnOption2),
            findViewById(R.id.btnOption3)
        )

        val subjectName = intent.getStringExtra("SUBJECT_NAME") ?: "General"
        Log.d("QuizActivity", "Subject received: $subjectName")

        if (subjectName.lowercase().trim() == "kotlin") {
            fetchFirebaseQuestions()
        } else {
            fetchLiveQuestions(subjectName)
        }

        btnOptions.forEachIndexed { index, button ->
            button.setOnClickListener { selectOption(index) }
        }

        btnNext.setOnClickListener { handleNextButtonClick() }
        
        btnClose.setOnClickListener { 
            showExitConfirmation(subjectName) 
        }
    }

    private fun showExitConfirmation(subject: String) {
        AlertDialog.Builder(this)
            .setTitle("Exit Quiz?")
            .setMessage("Are you sure you want to stop? You will be returned to the assessment start page.")
            .setPositiveButton("Yes, Exit") { _, _ ->
                val intent = Intent(this, QuizSetupActivity::class.java)
                intent.putExtra("SUBJECT_NAME", subject)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun fetchFirebaseQuestions() {
        loadingProgress.visibility = View.VISIBLE
        tvQuestionText.text = "Loading questions..."

        val databaseUrl = "https://elearningapplication-e1ba188c-default-rtdb.asia-southeast1.firebasedatabase.app/"
        
        try {
            databaseReference = FirebaseDatabase.getInstance(databaseUrl).getReference("quizzes/kotlin")

            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    loadingProgress.visibility = View.GONE
                    if (snapshot.exists()) {
                        activeQuestions.clear()
                        for (data in snapshot.children) {
                            val q = data.getValue(QuestionModel::class.java)
                            q?.let { activeQuestions.add(it) }
                        }

                        if (activeQuestions.isNotEmpty()) {
                            activeQuestions.shuffle()
                            // UPDATED: Restored to 25 questions
                            activeQuestions = activeQuestions.take(25).toMutableList()

                            progressBar.max = activeQuestions.size
                            // UPDATED: Timer set to 25 minutes
                            startTimer(25 * 60 * 1000)
                            loadQuestion()
                        } else {
                            tvQuestionText.text = "Error: Found 'quizzes/kotlin' but it's empty."
                        }
                    } else {
                        tvQuestionText.text = "No questions found for this subject."
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    loadingProgress.visibility = View.GONE
                    tvQuestionText.text = "Firebase Error: ${error.message}"
                }
            })
        } catch (e: Exception) {
            loadingProgress.visibility = View.GONE
            tvQuestionText.text = "Error: ${e.message}"
        }
    }

    private fun fetchLiveQuestions(subject: String) {
        loadingProgress.visibility = View.VISIBLE
        tvQuestionText.text = "Fetching questions for $subject..."

        val categoryId = getCategoryId(subject)

        RetrofitClient.instance.getQuestions(categoryId).enqueue(object : Callback<QuizResponse> {
            override fun onResponse(call: Call<QuizResponse>, response: Response<QuizResponse>) {
                loadingProgress.visibility = View.GONE
                if (response.isSuccessful) {
                    val rawList = response.body()?.results ?: emptyList()
                    if (rawList.isNotEmpty()) {
                        activeQuestions = rawList.map { api ->
                            QuestionModel(
                                question = api.question,
                                options = (api.incorrect_answers + api.correct_answer).shuffled(),
                                answer = api.correct_answer
                            )
                        }.shuffled().toMutableList()

                        progressBar.max = activeQuestions.size
                        // UPDATED: Timer set to 25 minutes
                        startTimer(25 * 60 * 1000)
                        loadQuestion()
                    }
                }
            }
            override fun onFailure(call: Call<QuizResponse>, t: Throwable) {
                loadingProgress.visibility = View.GONE
                Toast.makeText(this@QuizActivity, "Connection Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadQuestion() {
        if (activeQuestions.isEmpty() || currentQuestionIndex >= activeQuestions.size) return

        val q = activeQuestions[currentQuestionIndex]
        tvQuestionText.text = decodeHtml(q.question)
        tvQuestionTracker.text = "Question ${currentQuestionIndex + 1} of ${activeQuestions.size}"
        progressBar.progress = currentQuestionIndex

        correctAnswerText = q.answer
        currentOptions = q.options.shuffled()

        selectedOptionIndex = null
        btnOptions.forEachIndexed { index, button ->
            if (index < currentOptions.size) {
                button.visibility = View.VISIBLE
                button.text = decodeHtml(currentOptions[index])
            } else {
                button.visibility = View.GONE
            }
            resetButtonStyle(button)
        }
    }

    private fun selectOption(selectedIndex: Int) {
        selectedOptionIndex = selectedIndex
        btnOptions.forEachIndexed { index, button ->
            if (index == selectedIndex) {
                button.setBackgroundColor(getColor(R.color.primary_fixed))
                button.setTextColor(getColor(R.color.primary))
                button.strokeWidth = 4
            } else {
                resetButtonStyle(button)
            }
        }
    }

    private fun resetButtonStyle(button: MaterialButton) {
        button.setBackgroundColor(getColor(R.color.surface_container_low))
        button.setTextColor(getColor(R.color.outline))
        button.strokeWidth = 0
    }

    private fun getCategoryId(subject: String): Int {
        val s = subject.lowercase().trim()
        return when {
            s.contains("math") || s.contains("calculus") -> 19
            s.contains("computer") || s.contains("it") || s.contains("coding") -> 18
            s.contains("science") -> 17
            s.contains("history") -> 23
            s.contains("geography") -> 22
            else -> 9
        }
    }

    private fun startTimer(timeInMillis: Long) {
        countDownTimer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                tvTimer.text = String.format("%02d:%02d", minutes, seconds)
            }
            override fun onFinish() { finishQuiz() }
        }.start()
    }

    private fun handleNextButtonClick() {
        if (activeQuestions.isEmpty()) {
            Toast.makeText(this, "Questions not loaded yet", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedOptionIndex == null) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentOptions.isNotEmpty() && currentOptions[selectedOptionIndex!!] == correctAnswerText) {
            score++
        }

        currentQuestionIndex++
        if (currentQuestionIndex < activeQuestions.size) {
            loadQuestion()
        } else {
            finishQuiz()
        }
    }

    private fun finishQuiz() {
        countDownTimer?.cancel()
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("FINAL_SCORE", score)
        intent.putExtra("TOTAL_QUESTIONS", activeQuestions.size)
        val sName = this.intent.getStringExtra("SUBJECT_NAME") ?: "General"
        intent.putExtra("SUBJECT_NAME", sName)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    private fun decodeHtml(text: String): String {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(text).toString()
        }
    }
}