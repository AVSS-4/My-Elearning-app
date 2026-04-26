package com.example.elearningapplication

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

class ElearningApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 1. Force Light Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // 2. Initialize Firebase safely
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }

            val databaseUrl = "https://elearningapplication-e1ba188c-default-rtdb.asia-southeast1.firebasedatabase.app/"
            val database = FirebaseDatabase.getInstance(databaseUrl)

            // Try to set persistence, but don't crash if it fails
            try {
                database.setPersistenceEnabled(true)
            } catch (e: Exception) {
                Log.e("ElearningApp", "Persistence already set or failed: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e("ElearningApp", "Firebase initialization error: ${e.message}")
        }
    }
}