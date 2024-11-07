package com.example.project_aura_bloom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2

class MoodProgressFragment : Fragment() {

        // Inflating the layout, and setting up the view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.mood_progress_screen, container, false)
    }

    // Listing the order of the emotions
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

        // labeling each emotional animation
        val moodLabels = listOf("Happy", "Sad", "Angry", "Confused", "Calm", "Bothered", "Anxious")

        val moodViewPager = view.findViewById<ViewPager2>(R.id.mood_view_page)
        val emotionLabel = view.findViewById<TextView>(R.id.emotion_label)

        val adapter = MoodPageAdapter(moods)
        moodViewPager.adapter = adapter

        // Identify each emotion
        moodViewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                emotionLabel.text = moodLabels[position]
            }
        })
    }
}