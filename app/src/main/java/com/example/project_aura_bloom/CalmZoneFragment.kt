package com.example.project_aura_bloom

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_aura_bloom.databinding.CalmZoneScreenBinding
import java.util.logging.Handler

class CalmZoneFragment : Fragment() {

    // Binding safely and access to the calm zone screen UI elements
    private var _binding: CalmZoneScreenBinding? = null
    private val binding get() = _binding!!

    //MediaPlayer instance
    private var mediaPlayer: MediaPlayer? = null

    // Inflating the layout, and setting up the view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = CalmZoneScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Setting up the view once everything has been created and displayed
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load the Welcome Message animation
        val fadeInLeftToRight = AnimationUtils.loadAnimation(activity, R.anim.fade_in_left_to_right)

        // Initialize MediaPlayer with audio
        mediaPlayer = MediaPlayer.create(context, R.raw.calming_intro)

        // Start playing the audio when animation starts
        mediaPlayer?.start()

        // Apply the animation to the welcome message TextView
        binding.calmZoneInto.startAnimation(fadeInLeftToRight)

        // Set listener to stop audio when animation stops
        fadeInLeftToRight.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // Stop and release media player when the animation ends
                fadeOutMediaPlayer()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        // If button below is clicked within calm zone hub then...
        binding.peacefulCreationsButton.setOnClickListener {
            // Screen switches to the peaceful creations hub
            findNavController().navigate(R.id.action_calm_zone_to_peaceful_creations)
        }

        binding.meditationButton.setOnClickListener {
            findNavController().navigate(R.id.action_calm_zone_to_Meditation)
        }
    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    private fun fadeOutMediaPlayer() {
        val fadeDuration = 3000L
        val fadeStepDuration = 100L
        val maxVolume = 0.5f
        val minVolume = 0.0f
        var currentVolume = maxVolume

        // Calculate the decrement step based on the number of steps
        val numberOfSteps = (fadeDuration / fadeStepDuration).toInt()
        val volumeDecrement = maxVolume / numberOfSteps

        val handler = android.os.Handler(Looper.getMainLooper())
        val fadeOutRunnable = object : Runnable {
            override fun run() {
                if (currentVolume > minVolume) {
                    currentVolume -= volumeDecrement
                    if (currentVolume < minVolume) currentVolume = minVolume
                    mediaPlayer?.setVolume(currentVolume, currentVolume)
                    handler.postDelayed(this, fadeStepDuration)
                } else {
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                    mediaPlayer = null
                }
            }
        }
        handler.post(fadeOutRunnable)
    }

    // Binding clean up when view is destroyed to avoid memory leaks
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        _binding = null
    }
}