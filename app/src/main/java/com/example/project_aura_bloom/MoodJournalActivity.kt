package com.example.project_aura_bloom

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MoodJournalActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mood_journal_screen)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

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
                saveJournalEntry(selectedMood, inputText)
                Toast.makeText(this,"Journal saved successfully!",Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this,"Journal can't be empty, please type something before submitting.",Toast.LENGTH_SHORT).show()
            }
        }

    }


    // Function to get the current date
    private fun getCurrentDate(format: String = "MMMM dd, yyyy"): String {
        val dateFormat = java.text.SimpleDateFormat(format, java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date())
    }

    private fun saveJournalEntry(selectedMood: String, journalEntry: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val date = getCurrentDate("yyyy-MM-dd")

        //Check if user document exists
        db.collection("AuraBloomUserData").whereEqualTo("auth_uid", userId).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                    // User document exists
                    val userDocumentId = querySnapshot.documents[0].id
                    createJournalEntry(userDocumentId, date, selectedMood, journalEntry)
                } else {
                    // User document does not exist, create a new one
                    val newUserDocument = hashMapOf("auth_uid" to userId)
                    db.collection("AuraBloomUserData").add(newUserDocument)
                        .addOnSuccessListener { newDocumentRef ->
                            createJournalEntry(newDocumentRef.id, date, selectedMood, journalEntry)
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                this,
                                "Failed to create user document: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Failed to locate user document: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun createJournalEntry(userDocumentId: String, date: String, mood: String, entry: String) {
        val journalData = hashMapOf(
            "date" to date,
            "mood" to mood,
            "entry" to entry
        )

        //Save journal entry
        db.collection("AuraBloomUserData").document(userDocumentId)
            .collection("JournalEntry").add(journalData)
            .addOnSuccessListener {
                //Redirect only after success
                Toast.makeText(this, "Journal saved successfully!", Toast.LENGTH_SHORT).show()
                finish() // Exit the activity only after confirming the save
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Failed to save journal: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
