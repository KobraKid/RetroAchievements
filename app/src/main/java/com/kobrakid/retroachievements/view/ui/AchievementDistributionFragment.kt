package com.kobrakid.retroachievements.view.ui

import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.databinding.FragmentAchievementDistributionBinding
import com.kobrakid.retroachievements.viewmodel.AchievementDistributionViewModel

class AchievementDistributionFragment : Fragment() {

    private val viewModel: AchievementDistributionViewModel by viewModels()
    private var _binding: FragmentAchievementDistributionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAchievementDistributionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.gameDetailsAchievementDistribution.apply {
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry, h: Highlight) {
                    binding.gameDetailsChartHints.text =
                            if (e.x.toInt() == 1)
                                resources.getQuantityString(R.plurals.achievement_chart_hints, e.y.toInt(), e.y.toInt())
                            else
                                resources.getQuantityString(R.plurals.achievements_chart_hints, e.y.toInt(), e.y.toInt(), e.x.toInt())
                }

                override fun onNothingSelected() {
                    binding.gameDetailsChartHints.text = ""
                }
            })
        }
        viewModel.chartData.observe(viewLifecycleOwner) { populateChartData(it) }
        viewModel.setId((arguments?.getString("GameID", "0") ?: "0"))
    }

    private fun populateChartData(chartData: List<Entry>) {
        val dataSet = LineDataSet(chartData, "").apply { setDrawFilled(true) }

        // Set chart colors
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val accentColor = TypedValue()
            val primaryColor = TypedValue()
            context?.theme?.resolveAttribute(R.attr.colorAccent, accentColor, true)
            context?.theme?.resolveAttribute(R.attr.colorPrimary, primaryColor, true)
            binding.gameDetailsAchievementDistribution.axisLeft.textColor = primaryColor.data
            binding.gameDetailsAchievementDistribution.xAxis.textColor = primaryColor.data
            dataSet.setCircleColor(accentColor.data)
            dataSet.color = accentColor.data
            dataSet.circleHoleColor = accentColor.data
            dataSet.fillColor = accentColor.data
        }

        // Set chart axes
        binding.gameDetailsAchievementDistribution.axisRight.isEnabled = false
        binding.gameDetailsAchievementDistribution.legend.isEnabled = false
        binding.gameDetailsAchievementDistribution.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.gameDetailsAchievementDistribution.axisLeft.axisMinimum = 0f

        // Set chart description
        binding.gameDetailsAchievementDistribution.description = Description().apply { text = "" }

        // Set chart finalized data
        binding.gameDetailsAchievementDistribution.data = LineData(dataSet).apply { setDrawValues(false) }

        // Redraw chart
        binding.gameDetailsAchievementDistribution.invalidate()

        binding.gameDetailsAchievementDistroLoading.visibility = View.GONE
        binding.gameDetailsAchievementDistribution.visibility = View.VISIBLE
    }
}