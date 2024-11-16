package com.example.project_aura_bloom

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import android.content.Context
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

        // Added functionality to the journal back button
        val backButton = findViewById<ImageButton>(R.id.journal_back_button)
        backButton.setOnClickListener {
            finish()
        }

        val journalInput = findViewById<EditText>(R.id.journal_input)
        val submitButton = findViewById<Button>(R.id.submit_journal)

        submitButton.setOnClickListener {
            val inputText = journalInput.text.toString()
            if (inputText.isNotBlank()) {
                saveJournalEntry(inputText)
                Toast.makeText(this,"Journal saved successfully!",Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this,"Journal can't be empty, please type something before submitting.",Toast.LENGTH_SHORT).show()
            }
        }

    }


    // Function to get the current date
    private fun getCurrentDate(): String {
        val dateFormat = java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date())
    }

    private fun saveJournalEntry(entry: String) {
        val sharedPreferences = getSharedPreferences("MoodJournal", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("lastJournalEntry", entry)
        editor.apply()
    }
}
