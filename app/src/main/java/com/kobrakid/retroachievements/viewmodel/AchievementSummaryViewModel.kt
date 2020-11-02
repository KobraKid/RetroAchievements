package com.kobrakid.retroachievements.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.model.Game
import com.kobrakid.retroachievements.model.GameProgress
import com.kobrakid.retroachievements.model.IAchievement
import com.kobrakid.retroachievements.model.IGame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AchievementSummaryViewModel : ViewModel() {

    private val _achievements = MutableLiveData<List<IAchievement>>()
    private val _game = MutableLiveData<IGame>()
    private val _totalPoints = MutableLiveData<Float>()
    private val _totalTruePoints = MutableLiveData<Float>()
    private val _earnedPoints = MutableLiveData<Float>()
    private val _earnedTruePoints = MutableLiveData<Float>()
    private val _loading = MutableLiveData<Boolean>()

    val achievements: LiveData<List<IAchievement>> get() = _achievements
    val game: LiveData<IGame> get() = _game
    val totalPoints: LiveData<Float> get() = _totalPoints
    val totalTruePoints: LiveData<Float> get() = _totalTruePoints
    val earnedPoints: LiveData<Float> get() = _earnedPoints
    val earnedTruePoints: LiveData<Float> get() = _earnedTruePoints

    val loading: LiveData<Boolean> get() = _loading

    fun getGameInfoForUser(user: String?, id: String?, forceReload: Boolean = false) {
        if (!forceReload && achievements.value?.isNotEmpty() == true) return
        _loading.value = true
        CoroutineScope(IO).launch {
            Game.getGame(user, id) {
                withContext(Main) { (game as MutableLiveData).value = it }
            }
            GameProgress.getAchievementsForGame(user, id) {
                val totalPts = it
                        .map { achievement -> achievement.points.toFloat() * 2 } // account for all achievements earned hardcore (double the points earned)
                        .reduceRightOrNull { p, acc -> acc + p } ?: 0f
                val totalTruePts = it
                        .map { achievement -> achievement.truePoints.toFloat() } // worth the same regardless of whether earned hardcore
                        .reduceRightOrNull { p, acc -> acc + p } ?: 0f
                val earnedPts = it
                        .filter { achievement -> achievement.dateEarned.isNotEmpty() }
                        .map { achievement -> achievement.points.toFloat() * (if (achievement.dateEarnedHardcore.isNotEmpty()) 2 else 1) }
                        .reduceRightOrNull { p, acc -> acc + p } ?: 0f
                val earnedTruePts = it
                        .filter { achievement -> achievement.dateEarned.isNotEmpty() }
                        .map { achievement -> achievement.truePoints.toFloat() }
                        .reduceRightOrNull { p, acc -> acc + p } ?: 0f
                withContext(Main) {
                    _achievements.value = it.sortedBy { achievement -> if (achievement.dateEarned.isNotEmpty()) "" else achievement.displayOrder }
                    _totalPoints.value = totalPts
                    _totalTruePoints.value = totalTruePts
                    _earnedPoints.value = earnedPts
                    _earnedTruePoints.value = earnedTruePts
                    _loading.value = false
                }
            }
        }
    }
}