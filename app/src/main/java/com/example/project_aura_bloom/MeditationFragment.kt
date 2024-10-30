package com.example.project_aura_bloom

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Looper
import android.os.Handler
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.fragment.app.Fragment


class MeditationFragment : Fragment(R.layout.guided_meditation_player) {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private var currentTrackIndex = 0

    // Updating seekBar periodically
    private val handler = Handler(Looper.getMainLooper())

    // Current list of tracks
    private val trackList = listOf(R.raw.meditation)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Setting up the buttons and the seek bar
        val prevButton = view.findViewById<ImageButton>(R.id.prev_button)
        val playButton = view.findViewById<ImageButton>(R.id.play_button)
        val nextButton = view.findViewById<ImageButton>(R.id.next_button)
        seekBar = view.findViewById(R.id.guided_seekbar)

        // Setting the first track
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.meditation)

        // Setting the seekBar to the max duration of the track
        seekBar.max = mediaPlayer.duration

        // Toggling between the play and pause button
        playButton.setOnClickListener{
            onPlayButtonClick()
        }

        // Resetting the seekBar to 0 when track is complete
        mediaPlayer.setOnCompletionListener {
            seekBar.progress = 0
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

        prevButton.setOnClickListener{
            onPreviousButtonClick()
        }

        nextButton.setOnClickListener{
            onNextButtonClick()
        }
    }


    private fun onPreviousButtonClick(){
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

    private fun onPlayButtonClick(){
        if (!mediaPlayer.isPlaying){
            mediaPlayer.start()
            updateSeekBar()
        }else {
            mediaPlayer.pause()
        }
    }

    private fun onNextButtonClick(){
        currentTrackIndex = (currentTrackIndex + 1) % trackList.size
        mediaPlayer.reset()
        mediaPlayer = MediaPlayer.create(requireContext(), trackList[currentTrackIndex])
        mediaPlayer.start()
        seekBar.max = mediaPlayer.duration
        updateSeekBar()
    }

    private fun updateSeekBar() {
        seekBar.progress = mediaPlayer.currentPosition
        if (mediaPlayer.isPlaying){
            handler.postDelayed({ updateSeekBar() }, 1000)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer.release()
        handler.removeCallbacksAndMessages(null)
    }
}