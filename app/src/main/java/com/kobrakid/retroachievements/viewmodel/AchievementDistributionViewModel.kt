package com.kobrakid.retroachievements.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry
import com.kobrakid.retroachievements.model.AchievementChart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AchievementDistributionViewModel : ViewModel() {

    private val _chartData = MutableLiveData<List<Entry>>()

    val chartData: LiveData<List<Entry>> get() = _chartData

    fun setId(id: String, forceReload: Boolean = false) {
        if (!forceReload && _chartData.value?.isNotEmpty() == true) return
        CoroutineScope(IO).launch {
            AchievementChart.getAchievementDistributionChartForGame(id) {
                withContext(Main) {
                    _chartData.value = it
                }
            }
        }
    }
}