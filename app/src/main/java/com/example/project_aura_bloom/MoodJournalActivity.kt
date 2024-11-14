package com.example.project_aura_bloom

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MoodJournalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mood_journal_screen)

        // Getting and setting the current date
        val journalDate = findViewById<TextView>(R.id.journal_date)
        journalDate.text = getCurrentDate()

        // Getting and setting the selected mood and intensity
        val selectedMoodTextView = findViewById<TextView>(R.id.selected_mood)
        val selectedMood = intent.getStringExtra("selectedMood") ?: "No mood selected"
        selectedMoodTextView.text = selectedMood

        val backButton = findViewById<ImageButton>(R.id.journal_back_button)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }


    // Function to get the current date
    private fun getCurrentDate(): String {
        val dateFormat = java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date())
    }
}
