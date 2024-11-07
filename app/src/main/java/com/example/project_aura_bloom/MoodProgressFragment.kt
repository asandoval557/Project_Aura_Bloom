package com.example.project_aura_bloom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2

class MoodProgressFragment : Fragment() {

        // Inflating the layout, and setting up the view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.mood_progress_screen, container, false)
    }

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

        val moodViewPager = view.findViewById<ViewPager2>(R.id.mood_view_page)
        moodViewPager.adapter = MoodPageAdapter(moods)
    }
}