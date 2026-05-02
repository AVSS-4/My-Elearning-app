package com.example.elearningapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.json.JSONObject

class HomeActivity : AppCompatActivity() {

    private val FILE_REQUEST_CODE = 100

    private var uploadedQuestions: ArrayList<Question>? = null
    private var isFileUploaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        uploadedQuestions = null
        isFileUploaded = false

        val btnUpload = findViewById<MaterialButton>(R.id.btnUpload)
        val btnHistory = findViewById<MaterialButton>(R.id.btnHistory)
        val etSearch = findViewById<TextInputEditText>(R.id.etSearch)
        val searchBarLayout = findViewById<TextInputLayout>(R.id.searchBarLayout)
        val cardComputerScience = findViewById<MaterialCardView>(R.id.cardComputerScience)
        val btnCSQuiz = findViewById<MaterialButton>(R.id.btnCSQuiz)
        val btnProfileBottom = findViewById<MaterialButton>(R.id.btnProfileBottom)

        // 📂 Upload File
        btnUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf(
                    "text/plain",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                )
            )
            startActivityForResult(intent, FILE_REQUEST_CODE)
        }

        // 📜 History
        btnHistory.setOnClickListener {
            startActivity(Intent(this, QuizHistoryActivity::class.java))
        }

        // 🔍 Search → FETCH DIRECTLY FROM API
        etSearch.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                performSearch(etSearch.text.toString())
                true
            } else false
        }

        searchBarLayout.setEndIconOnClickListener {
            performSearch(etSearch.text.toString())
        }

        // 👤 Profile
        btnProfileBottom.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // 🎯 START QUIZ (Computer Science default)
        btnCSQuiz.setOnClickListener {

            if (isFileUploaded && uploadedQuestions != null && uploadedQuestions!!.isNotEmpty()) {

                Toast.makeText(this, "Using FILE questions", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, QuizActivity::class.java)
                intent.putExtra("questions", ArrayList(uploadedQuestions!!))
                startActivity(intent)

            } else {

                Toast.makeText(this, "Fetching Computer Science questions...", Toast.LENGTH_SHORT).show()
                fetchQuestionsFromAPI("computer science")
            }
        }

        cardComputerScience.setOnClickListener {
            btnCSQuiz.performClick()
        }
    }

    // 🌐 UPDATED API FUNCTION (SUBJECT + DIFFICULTY)
    private fun fetchQuestionsFromAPI(subject: String) {

        val category = getCategoryId(subject)
        val difficulty = getDifficulty()

        val url = "https://opentdb.com/api.php?amount=5&category=$category&difficulty=$difficulty&type=multiple"

        Thread {
            try {
                val json = java.net.URL(url).readText()
                val jsonObject = JSONObject(json)
                val results = jsonObject.getJSONArray("results")

                val apiQuestions = ArrayList<Question>()

                for (i in 0 until results.length()) {
                    val obj = results.getJSONObject(i)

                    val questionText = android.text.Html.fromHtml(
                        obj.getString("question"),
                        android.text.Html.FROM_HTML_MODE_LEGACY
                    ).toString()

                    val correctAnswer = obj.getString("correct_answer")
                    val incorrect = obj.getJSONArray("incorrect_answers")

                    val options = ArrayList<String>()
                    options.add(correctAnswer)
                    options.add(incorrect.getString(0))
                    options.add(incorrect.getString(1))
                    options.add(incorrect.getString(2))

                    options.shuffle()

                    val correctIndex = options.indexOf(correctAnswer)
                    val correctOption = listOf("A", "B", "C", "D")[correctIndex]

                    apiQuestions.add(
                        Question(
                            questionText,
                            options[0],
                            options[1],
                            options[2],
                            options[3],
                            correctOption
                        )
                    )
                }

                runOnUiThread {
                    val intent = Intent(this, QuizActivity::class.java)
                    intent.putExtra("questions", apiQuestions)
                    startActivity(intent)
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "API Failed! Check Internet", Toast.LENGTH_LONG).show()
                }
                e.printStackTrace()
            }
        }.start()
    }

    // 📂 FILE HANDLING
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            val uri: Uri? = data?.data

            try {
                val inputStream = contentResolver.openInputStream(uri!!)
                val fileType = contentResolver.getType(uri)

                val content = if (fileType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document") {
                    val document = XWPFDocument(inputStream)
                    val extractor = XWPFWordExtractor(document)
                    extractor.text
                } else {
                    inputStream?.bufferedReader()?.use { it.readText() }
                }

                val tvFileContent = findViewById<TextView>(R.id.tvFileContent)
                tvFileContent.text = content

                val questions = parseQuestions(content ?: "")

                if (questions.isNotEmpty()) {
                    uploadedQuestions = questions
                    isFileUploaded = true
                    Toast.makeText(this, "File loaded successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    uploadedQuestions = null
                    isFileUploaded = false
                    Toast.makeText(this, "No valid questions found", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                uploadedQuestions = null
                isFileUploaded = false
                Toast.makeText(this, "Error reading file", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    // 🧠 Parse Questions
    private fun parseQuestions(text: String): ArrayList<Question> {
        val list = ArrayList<Question>()
        val blocks = text.split("Question:")

        for (block in blocks) {
            val lines = block.trim().split("\n")

            if (lines.size >= 6) {
                val question = lines[0]
                val a = lines[1].removePrefix("A. ").trim()
                val b = lines[2].removePrefix("B. ").trim()
                val c = lines[3].removePrefix("C. ").trim()
                val d = lines[4].removePrefix("D. ").trim()
                val ans = lines[5].replace("Answer:", "").trim()

                list.add(Question(question, a, b, c, d, ans))
            }
        }

        return list
    }

    //  SEARCH → API CALL
    private fun performSearch(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isNotEmpty()) {
            fetchQuestionsFromAPI(trimmedQuery)
        } else {
            Toast.makeText(this, "Enter subject", Toast.LENGTH_SHORT).show()
        }
    }


    private fun getCategoryId(subject: String): Int {
        return when (subject.lowercase()) {
            "computer", "computer science", "cs" -> 18
            "math", "mathematics" -> 19
            "science" -> 17
            "general knowledge", "gk" -> 9
            "history" -> 23
            "geography" -> 22
            else -> 9
        }
    }

    // 🎚️ DIFFICULTY (CAN UPGRADE LATER)
    private fun getDifficulty(): String {
        return "easy"
    }
}