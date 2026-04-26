package com.example.elearningapplication

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Find the UI Elements
        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)
        val btnNavHome = findViewById<MaterialButton>(R.id.btnNavHome)
        val switchDarkMode = findViewById<SwitchMaterial>(R.id.switchDarkMode)
        val switchNotifications = findViewById<SwitchMaterial>(R.id.switchNotifications)
        
        val tvProfileEmailHeader = findViewById<TextView>(R.id.tvProfileEmailHeader)
        val tvProfileEmailContent = findViewById<TextView>(R.id.tvProfileEmailContent)

        // 1. Get the current logged-in user from Firebase
        val currentUser = FirebaseAuth.getInstance().currentUser
        
        // 2. Display their email if it exists
        if (currentUser != null) {
            val userEmail = currentUser.email ?: "No email linked"
            tvProfileEmailHeader.text = userEmail
            tvProfileEmailContent.text = userEmail
        }

        // Switch Logic (Simulated)
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // This command actually tells the Android System to change the theme
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                    androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                )
                Toast.makeText(this, "Dark Atmosphere enabled", Toast.LENGTH_SHORT).show()
            } else {
                // This command switches it back to Light mode
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                    androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
                )
                recreate()
                Toast.makeText(this, "Light Atmosphere enabled", Toast.LENGTH_SHORT).show()
            }
        }

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notifications muted", Toast.LENGTH_SHORT).show()
            }
        }

        // Bottom Navigation - Go back to Home
        btnNavHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            // Prevent stacking multiple home screens on top of each other
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish() // Close profile
        }

        btnLogout.setOnClickListener {
            // 1. Sign out from Firebase
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()

            // 2. CLEAR GOOGLE CACHE (Crucial for your 50 students)
            val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
            ).build()
            com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso).signOut()

            // 3. Navigate and Clear History
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

            Toast.makeText(this, "Successfully logged out", Toast.LENGTH_SHORT).show()
        }
    }
}