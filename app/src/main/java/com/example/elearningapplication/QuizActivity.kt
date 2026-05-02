package com.example.elearningapplication

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class QuizActivity : AppCompatActivity() {

    private lateinit var questions: ArrayList<Question>
    private var currentIndex = 0
    private var score = 0

    private lateinit var tvQuestion: TextView
    private lateinit var radioGroup: RadioGroup
    private lateinit var optionA: RadioButton
    private lateinit var optionB: RadioButton
    private lateinit var optionC: RadioButton
    private lateinit var optionD: RadioButton
    private lateinit var btnNext: Button
    private lateinit var tvTimer: TextView

    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 60000 // 60 sec

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        // ✅ SAFE DATA FETCH (VERY IMPORTANT FIX)
        val receivedQuestions = intent.getSerializableExtra("questions")

        if (receivedQuestions != null && receivedQuestions is ArrayList<*>) {
            questions = receivedQuestions as ArrayList<Question>
        } else {
            questions = ArrayList()
            Toast.makeText(this, "Error: No questions received!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvQuestion = findViewById(R.id.tvQuestion)
        radioGroup = findViewById(R.id.radioGroup)
        optionA = findViewById(R.id.optionA)
        optionB = findViewById(R.id.optionB)
        optionC = findViewById(R.id.optionC)
        optionD = findViewById(R.id.optionD)
        btnNext = findViewById(R.id.btnNext)
        tvTimer = findViewById(R.id.tvTimer)

        loadQuestion()

        btnNext.setOnClickListener {
            checkAnswer()
        }
    }

    private fun loadQuestion() {
        if (currentIndex < questions.size) {

            val q = questions[currentIndex]

            tvQuestion.text = q.question
            optionA.text = "A. ${q.optionA}"
            optionB.text = "B. ${q.optionB}"
            optionC.text = "C. ${q.optionC}"
            optionD.text = "D. ${q.optionD}"

            radioGroup.clearCheck()

            // 🔥 RESET TIMER
            timeLeftInMillis = 60000
            startTimer()

        } else {
            showResult()
        }
    }

    private fun startTimer() {
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvTimer.text = "Time: ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                tvTimer.text = "Time: 0"
                currentIndex++
                loadQuestion()
            }
        }.start()
    }

    private fun checkAnswer() {
        val selectedId = radioGroup.checkedRadioButtonId

        if (selectedId == -1) {
            Toast.makeText(this, "Select an answer", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedAnswer = when (selectedId) {
            R.id.optionA -> "A"
            R.id.optionB -> "B"
            R.id.optionC -> "C"
            R.id.optionD -> "D"
            else -> ""
        }

        if (selectedAnswer == questions[currentIndex].correctAnswer) {
            score++
        }

        countDownTimer?.cancel()

        currentIndex++
        loadQuestion()
    }

    private fun showResult() {

        // 💾 SAVE SCORE
        val sharedPref = getSharedPreferences("QuizApp", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt("last_score", score)
        editor.apply()

        // 🚀 OPEN RESULT SCREEN
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("score", score)
        intent.putExtra("total", questions.size)
        startActivity(intent)

        finish()
    }
}