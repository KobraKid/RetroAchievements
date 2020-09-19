package com.kobrakid.retroachievements.view.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

class UserSummaryFragment : Fragment() {

    private val args: UserSummaryFragmentArgs by navArgs()
    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        retainInstance = true
        activity?.title = "User Summary"
        return inflater.inflate(R.layout.fragment_user_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        activity?.title = "User Summary: ${args.username}"
        view.findViewById<TextView>(R.id.user_summary_username).text = args.username
        CoroutineScope(IO).launch {
            RetroAchievementsApi.GetUserSummary(context, args.username, 5) { parseUserSummary(it) }
        }
    }

    private suspend fun parseUserSummary(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_USER_SUMMARY -> {
                try {
                    withContext(Main) {
                        val reader = JSONObject(response.second)
                        view?.let {
                            Picasso.get()
                                    .load(Consts.BASE_URL + "/" + reader.getString("UserPic"))
                                    .placeholder(R.drawable.user_placeholder)
                                    .into(it.findViewById<ImageView>(R.id.user_summary_image))
                            val motto = "\"${reader.getString("Motto")}\""
                            it.findViewById<TextView>(R.id.user_summary_motto).text = if (motto.length > 2) motto else ""
                            it.findViewById<TextView>(R.id.user_summary_rank).text = getString(R.string.user_rank, reader.getString("Rank"))
                            it.findViewById<TextView>(R.id.user_summary_points).text = getString(R.string.user_points, reader.getString("TotalPoints"))
                            it.findViewById<TextView>(R.id.user_summary_ratio).text = getString(R.string.user_ratio, String.format("%.2f", reader.getString("TotalTruePoints").toFloat().div(reader.getString("TotalPoints").toFloat())))
                            it.findViewById<TextView>(R.id.user_summary_joined).text = getString(R.string.user_joined, reader.getString("MemberSince"))
                        }
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Unable to parse user summary", e)
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    companion object {
        private val TAG = Consts.BASE_TAG + UserSummaryFragment::class.java.simpleName
    }

}