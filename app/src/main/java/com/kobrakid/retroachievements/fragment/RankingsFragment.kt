package com.kobrakid.retroachievements.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.activity.MainActivity
import com.kobrakid.retroachievements.adapter.UserRankingAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class RankingsFragment : Fragment(), View.OnClickListener {

    private val userRankingAdapter = UserRankingAdapter(this)
    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity?.title = "User Rankings"
        return inflater.inflate(R.layout.fragment_rankings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        view.findViewById<RecyclerView>(R.id.rankings_users).apply {
            adapter = userRankingAdapter
            layoutManager = LinearLayoutManager(context)
        }
        CoroutineScope(IO).launch {
            RetroAchievementsApi.GetTopTenUsers(context) { parseTopTenUsers(it) }
        }
    }

    override fun onClick(view: View?) {
        if (view != null) {
            navController.navigate(
                    RankingsFragmentDirections.actionRankingsFragmentToUserSummaryFragment(
                            view.findViewById<TextView>(R.id.participant_username).text.toString()))
        }
    }

    private suspend fun parseTopTenUsers(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_TOP_TEN_USERS -> {
                try {
                    val reader = JSONArray(response.second)
                    var loggedInUserIncluded = false
                    for (i in 0 until reader.length()) {
                        if ((reader[i] as JSONObject).getString("1") == MainActivity.raUser)
                            loggedInUserIncluded = true
                        userRankingAdapter.addUser(
                                (i + 1).toString(),
                                (reader[i] as JSONObject).getString("1"),
                                (reader[i] as JSONObject).getString("2"),
                                (reader[i] as JSONObject).getString("3"))
                    }
                    // Adjust for logged in user not being in the Top Ten
                    if (!loggedInUserIncluded) {
                        withContext(IO) {
                            RetroAchievementsApi.GetUserSummary(context, MainActivity.raUser, 0) { parseTopTenUsers(it) }
                        }
                    }

                } catch (e: JSONException) {
                    Log.e(TAG, "Couldn't parse top ten users", e)
                }
            }
            RetroAchievementsApi.RESPONSE.GET_USER_SUMMARY -> {
                try {
                    val reader = JSONObject(response.second)
                    userRankingAdapter.addUser(
                            reader.getString("Rank"),
                            MainActivity.raUser,
                            reader.getString("TotalPoints"),
                            reader.getString("TotalTruePoints"))
                } catch (e: JSONException) {
                    Log.e(TAG, "Couldn't parse user summary", e)
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    companion object {
        private val TAG = Consts.BASE_TAG + RankingsFragment::class.java.simpleName
    }
}