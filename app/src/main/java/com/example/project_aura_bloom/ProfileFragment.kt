package com.example.project_aura_bloom

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.project_aura_bloom.databinding.FragmentProfileBinding
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.charts.BarChart

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up Meditation Bar Chart
        setupMeditationBarChart(binding.meditationBarChart)
    }

    // Function to set up Meditation Bar Chart
    private fun setupMeditationBarChart(barChart: BarChart) {
        // Create a list of entries (data points)
        val entries = listOf(
            BarEntry(0f, 3f), // Sunday
            BarEntry(1f, 2f), // Monday
            BarEntry(2f, 1f), // Tuesday
            BarEntry(3f, 5f), // Wednesday
            BarEntry(4f, 3f), // Thursday
            BarEntry(5f, 4f), // Friday
            BarEntry(6f, 4f), // Saturday
        )

        // Create a dataset with the entries
        val barDataSet = BarDataSet(entries, "Meditation time (hours)")
        barDataSet.color = resources.getColor(R.color.dark_gray, null)

        // Create a BarData object with the dataset
        val data = BarData(barDataSet)

        // Set data to the bar chart and refresh
        barChart.data = data
        barChart.invalidate() // Refresh
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}