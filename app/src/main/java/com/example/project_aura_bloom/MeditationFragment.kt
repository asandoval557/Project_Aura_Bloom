package com.example.project_aura_bloom

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Looper
import android.os.Handler
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*


class MeditationFragment : Fragment(R.layout.guided_meditation_player) {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var playButton: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var volumeSeekBar: SeekBar
    private var currentTrackIndex = 0
    private var sessionStartTime: Long = 0

    // Updating seekBar periodically
    private val handler = Handler(Looper.getMainLooper())

    // Current list of tracks
    private val trackList = listOf(R.raw.meditation)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Setting up the buttons and the seek bar
        val prevButton = view.findViewById<ImageButton>(R.id.prev_button)
        playButton = view.findViewById(R.id.play_button)
        val nextButton = view.findViewById<ImageButton>(R.id.next_button)
        seekBar = view.findViewById(R.id.guided_seekbar)
        volumeSeekBar = view.findViewById(R.id.volume_seekbar)

        // Setting the first track
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.meditation)

        // Setting the seekBar to the max duration of the track
        seekBar.max = mediaPlayer.duration

        // Setting the default volume level to 50%
        volumeSeekBar.max = 100
        volumeSeekBar.progress = 50
        mediaPlayer.setVolume(0.5f, 0.5f)

        // Adjust the volume based on the volume seek bar changes
        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val volume = progress / 100.0f
                mediaPlayer.setVolume(volume, volume)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Toggling between the play and pause button
        playButton.setOnClickListener {
            onPlayButtonClick()
        }

        // Resetting the seekBar to 0 when track is complete
        mediaPlayer.setOnCompletionListener {
            seekBar.progress = 0
            // Calculate the total duration in minutes (convert milliseconds to minutes)
            val durationInMinutes = mediaPlayer.duration / 1000 / 60
            // Log the meditation time (rounded to the nearest minute)
            logMeditationTime(durationInMinutes)
        }

        // Updating the media player position when the seekBar is dragged
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        prevButton.setOnClickListener {
            onPreviousButtonClick()
        }

        nextButton.setOnClickListener {
            onNextButtonClick()
        }
    }


    private fun onPreviousButtonClick() {
        if (mediaPlayer.currentPosition > 5000) {
            mediaPlayer.seekTo(0)
        } else {
            currentTrackIndex = if (currentTrackIndex > 0) currentTrackIndex - 1 else trackList.size - 1
            mediaPlayer.reset()
            mediaPlayer = MediaPlayer.create(requireContext(), trackList[currentTrackIndex])
            mediaPlayer.start()
            seekBar.max = mediaPlayer.duration
            updateSeekBar()
        }
    }

    private fun onPlayButtonClick() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
            sessionStartTime = System.currentTimeMillis() // Log start time
            playButton.setImageResource(R.drawable.ic_pause)
            updateSeekBar()
        } else {
            mediaPlayer.pause()
            val elapsedTimeInMinutes = ((System.currentTimeMillis() - sessionStartTime) / 1000 / 60).toInt()
            logMeditationTime(elapsedTimeInMinutes)
            playButton.setImageResource(R.drawable.ic_play)
        }
    }

    private fun onNextButtonClick() {
        currentTrackIndex = (currentTrackIndex + 1) % trackList.size
        mediaPlayer.reset()
        mediaPlayer = MediaPlayer.create(requireContext(), trackList[currentTrackIndex])
        mediaPlayer.start()
        seekBar.max = mediaPlayer.duration
        updateSeekBar()
    }

    private fun updateSeekBar() {
        seekBar.progress = mediaPlayer.currentPosition
        if (mediaPlayer.isPlaying) {
            handler.postDelayed({ updateSeekBar() }, 1000)
        }
    }

    private fun logMeditationTime(durationInMinutes: Int) {
        if (durationInMinutes <= 0) return

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(Calendar.getInstance().time)

        // Create or update the entry for today's meditation session
        db.collection("AuraBloomUserData").whereEqualTo("auth_uid", userId).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                    val documentId = querySnapshot.documents[0].id

                    val sessionDocRef = db.collection("AuraBloomUserData")
                        .document(documentId)
                        .collection("dailySessions")
                        .document(currentDate)

                    sessionDocRef.get().addOnSuccessListener { document ->
                        val currentDuration = document.getDouble("duration")?.toInt() ?: 0
                        val newTotalDuration = currentDuration + durationInMinutes

                        // Update Firestore with the new total duration
                        sessionDocRef.set(
                            mapOf("duration" to newTotalDuration)
                        ).addOnSuccessListener {
                                Toast.makeText(context, "Meditation session logged", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener { exception ->
                                Toast.makeText(context, "Error logging time: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener { exception ->
                            Toast.makeText(context, "Error fetching current duration: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                        Toast.makeText(context, "User data not found in Firestore.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                    Toast.makeText(context, "Firestore query failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            val elapsedTimeInMinutes = ((System.currentTimeMillis() - sessionStartTime) / 1000 / 60).toInt()
            logMeditationTime(elapsedTimeInMinutes)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer.release()
        handler.removeCallbacksAndMessages(null)
    }
}