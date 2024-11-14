package com.example.project_aura_bloom

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.SeekBar
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import androidx.appcompat.app.AlertDialog

class MoodProgressFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.mood_progress_screen, container, false)
    }

    // Listing the order of the emotions
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val moods = listOf(
            R.raw.happy,
            R.raw.sad,
            R.raw.angry,
            R.raw.confused,
            R.raw.calm,
            R.raw.bothered,
            R.raw.anxious
        )

        // On a scale os 1-5 weighing each selected emotion
        val emotionLabels = mapOf(
            "Happy" to listOf("Slightly happy", "Moderately happy", "Very happy", "Extremely happy", "Blissful"),
            "Sad" to listOf("Slightly sad", "Moderately sad", "Very sad", "Extremely sad", "Devastated"),
            "Angry" to listOf("Slightly annoyed", "Moderately angry", "Very angry", "Extremely angry", "Furious"),
            "Confused" to listOf("Slightly confused", "Moderately confused", "Very confused", "Extremely confused", "Completely baffled"),
            "Calm" to listOf("Slightly calm", "Moderately calm", "Very calm", "Extremely calm", "Completely serene"),
            "Bothered" to listOf("Slightly bothered", "Moderately bothered", "Very bothered", "Extremely bothered", "Completely unsettled"),
            "Anxious" to listOf("Slightly anxious", "Moderately anxious", "Very anxious", "Extremely anxious", "Panicked")
        )

        val moodViewPager = view.findViewById<ViewPager2>(R.id.mood_view_page)
        val emotionLabel = view.findViewById<TextView>(R.id.emotion_label)

        val adapter = MoodPageAdapter(moods) { selectedEmotion ->
            val labels = emotionLabels[selectedEmotion] ?: listOf("Level 1", "Level 2", "Level 3", "Level 4", "Level 5")
            showEmotionIntensityDialog(selectedEmotion, labels)
        }
        moodViewPager.adapter = adapter

        // Identifying each emotion
        moodViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                val selectedEmotion = when (position) {
                    0 -> "Happy"
                    1 -> "Sad"
                    2 -> "Angry"
                    3 -> "Confused"
                    4 -> "Calm"
                    5 -> "Bothered"
                    6 -> "Anxious"
                    else -> "Emotion"
                }

                emotionLabel.text = selectedEmotion


                emotionLabel.setOnClickListener {
                    val labels = emotionLabels[selectedEmotion] ?: listOf("Level 1", "Level 2", "Level 3", "Level 4", "Level 5")
                    showEmotionIntensityDialog(selectedEmotion, labels)
                }
            }
        })
    }

    private fun showEmotionIntensityDialog(emotion: String, labels: List<String>) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.mood_scale, null)

        val emotionTitle = dialogView.findViewById<TextView>(R.id.emotion_title)
        val seekBar = dialogView.findViewById<SeekBar>(R.id.emotional_scale)
        val intensityLabel = dialogView.findViewById<TextView>(R.id.scale_label)
        val confirmButton = dialogView.findViewById<Button>(R.id.mood_confirm_button)

        // Dialog question
        emotionTitle.text = getString(R.string.how_are_you, emotion.replaceFirstChar { it.lowercase() })

        // Set initial scale label
        intensityLabel.text = labels[0]

        // Updating the label as the seek bar is moved
        seekBar.max = labels.size - 1 // Making sure the seek bar is matching the label count
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                intensityLabel.text = labels[progress]
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        confirmButton.setOnClickListener {

            val selectedIntensityText = labels[seekBar.progress]
            val moodWithIntensity = selectedIntensityText

            dialog.dismiss()


            showJournalPromptDialog(selectedIntensityText)
        }

        dialog.show()
    }

    // Asking if user would like to journal their emotions
    private fun showJournalPromptDialog(selectedMood: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Journal Your Emotions")
        builder.setMessage("Would you like to journal your emotions?")

        // If yes is clicked then open journal
        builder.setPositiveButton("Yes") { dialog, _ ->
            dialog.dismiss()
            val intent = Intent(requireContext(), MoodJournalActivity::class.java)
            intent.putExtra("selectedMood", selectedMood) // Putting the selected intensity mood onto the journal
            startActivity(intent)
        }

        // If no is clicked then close dialog and do not open journal
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

}