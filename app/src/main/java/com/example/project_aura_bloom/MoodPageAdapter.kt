package com.example.project_aura_bloom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView

class MoodPageAdapter(
    private val moods: List<Int>,
    private val onEmotionClick: (String) -> Unit
) : RecyclerView.Adapter<MoodPageAdapter.MoodViewHolder>() {

    inner class MoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val moodAnimationView: LottieAnimationView = view.findViewById(R.id.mood_animation_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mood_page_items, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val moodResId = moods[position]
        holder.moodAnimationView.setAnimation(moodResId)
        holder.moodAnimationView.playAnimation()

        // Adjusting the scaling for specified emotion animations
        when (moodResId) {
            R.raw.calm, R.raw.bothered, R.raw.anxious -> {
                holder.moodAnimationView.scaleX = 0.8f
                holder.moodAnimationView.scaleY = 0.8f
            }
            else -> {
                holder.moodAnimationView.scaleX = 1f
                holder.moodAnimationView.scaleY = 1f
            }
        }

        // Determining the emotion
        val emotionLabel = when (moodResId) {
            R.raw.happy -> "Happy"
            R.raw.sad -> "Sad"
            R.raw.angry -> "Angry"
            R.raw.confused -> "Confused"
            R.raw.calm -> "Calm"
            R.raw.bothered -> "Bothered"
            R.raw.anxious -> "Anxious"
            else -> "Emotion"
        }


        holder.moodAnimationView.setOnClickListener {
            onEmotionClick(emotionLabel)
        }
    }

    override fun getItemCount(): Int = moods.size
}