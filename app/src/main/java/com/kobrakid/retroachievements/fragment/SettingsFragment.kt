package com.kobrakid.retroachievements.fragment

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.activity.MainActivity
import com.kobrakid.retroachievements.database.Console
import com.kobrakid.retroachievements.database.RetroAchievementsDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException

class SettingsFragment : Fragment(), View.OnClickListener {

    private var sharedPref: SharedPreferences? = null
    private val applicableSettings = SparseArray<Runnable?>()
    private var counter = 0

    // Can be local, but used to index which settings have been modified
    private val logoutKey = 0
    private val hideConsolesKey = 1
    private val hideGamesKey = 2
    private val changeThemeKey = 3

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        requireActivity().title = "Settings"
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize preferences object
        sharedPref = context?.getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE)

        // Set up views
        val theme = Consts.Theme.values().indexOfFirst { it.themeAttr == sharedPref?.getInt(getString(R.string.theme_setting), R.style.BlankTheme) }.coerceAtLeast(1)
        view.findViewById<TextView>(R.id.settings_current_theme).text = getString(R.string.settings_current_theme, Consts.Theme.values()[theme].themeName)
        view.findViewById<TextView>(R.id.settings_current_user).text = if (MainActivity.raUser == "") getString(R.string.settings_no_current_user) else getString(R.string.settings_current_user, MainActivity.raUser)
        view.findViewById<Spinner>(R.id.settings_theme_dropdown).adapter = object : ArrayAdapter<Consts.Theme>(requireContext(), android.R.layout.simple_spinner_dropdown_item, Consts.Theme.values()) {
            override fun isEnabled(position: Int): Boolean {
                return Consts.Theme.values()[position].enabled
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val textView = super.getDropDownView(position, convertView, parent) as TextView
                if (isEnabled(position)) {
                    val typedValue = TypedValue()
                    context.theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        textView.setTextColor(context.resources.getColor(typedValue.resourceId, activity?.theme))
                    else
                        textView.setTextColor(ContextCompat.getColor(context, typedValue.resourceId))
                } else {
                    textView.setTextColor(Color.GRAY)
                }
                return textView
            }
        }
        view.findViewById<Spinner>(R.id.settings_theme_dropdown).setSelection(theme)
        view.findViewById<Spinner>(R.id.settings_theme_dropdown).onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, pos: Int, id: Long) {
                if (pos > 0) changeTheme(adapterView.getItemAtPosition(pos).toString())
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        view.findViewById<CheckBox>(R.id.settings_hide_consoles).isChecked = sharedPref?.getBoolean(getString(R.string.empty_console_hide_setting), false)!!
        if (sharedPref?.getBoolean(getString(R.string.empty_console_hide_setting), false) != false) view.findViewById<View>(R.id.settings_hide_consoles_warning).visibility = View.GONE
        view.findViewById<CheckBox>(R.id.settings_hide_games).isChecked = sharedPref?.getBoolean(getString(R.string.empty_game_hide_setting), false)!!
        view.findViewById<CheckBox>(R.id.settings_hide_consoles).setOnCheckedChangeListener { _: CompoundButton?, b: Boolean -> hideConsoles(view, b) }
        view.findViewById<CheckBox>(R.id.settings_hide_games).setOnCheckedChangeListener { _: CompoundButton?, b: Boolean -> hideGames(view, b) }

        view.findViewById<Button>(R.id.settings_logout).setOnClickListener(this)
        view.findViewById<Button>(R.id.settings_apply).setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.settings_logout -> logout()
            R.id.settings_apply -> applySettings()
        }
    }

    /* Settings-related Functions */
    private fun changeTheme(theme: String) {
        applicableSettings.remove(changeThemeKey)
        applicableSettings.put(changeThemeKey, Runnable {
            Log.d(TAG, "Saving theme $theme with theme attribute ${Consts.Theme.values()[Consts.Theme.values().indexOfFirst { it.themeName == theme }].themeAttr}")
            sharedPref?.edit()?.putInt(getString(R.string.theme_setting), Consts.Theme.values()[Consts.Theme.values().indexOfFirst { it.themeName == theme }].themeAttr)?.apply()
        })
    }

    private fun hideConsoles(view: View, hide: Boolean) {
        view.findViewById<View>(R.id.settings_hide_consoles_warning).visibility = if (hide) View.GONE else View.VISIBLE
        if (applicableSettings[hideConsolesKey] != null) {
            applicableSettings.remove(hideConsolesKey)
        } else {
            applicableSettings.put(hideConsolesKey, Runnable {
                sharedPref?.edit()?.putBoolean(getString(R.string.empty_console_hide_setting), hide)?.apply()
                if (hide) {
                    // Get all consoles and store their game counts
                    val db = context?.let { RetroAchievementsDatabase.getInstance(it) }
                    db?.let {
                        CoroutineScope(IO).launch {
                            db.consoleDao()?.clearTable()
                            Log.d(TAG, "Clearing console table")
                        }
                    }
                    val ctx = context?.applicationContext
                    CoroutineScope(IO).launch {
                        if (ctx != null)
                            RetroAchievementsApi.GetConsoleIDs(ctx) { removeConsoles(view, it) }
                    }
                } else {
                    activity?.recreate()
                }
            })
        }
    }

    private fun hideGames(view: View, hide: Boolean) {
        view.findViewById<View>(R.id.settings_hide_games_warning).visibility = if (hide) View.GONE else View.VISIBLE
        if (applicableSettings[hideGamesKey] != null) {
            applicableSettings.remove(hideGamesKey)
        } else {
            applicableSettings.put(hideGamesKey, Runnable { sharedPref?.edit()?.putBoolean(getString(R.string.empty_game_hide_setting), hide)?.apply() })
        }
    }

    private fun logout() {
        (activity?.findViewById<View>(R.id.settings_current_user) as TextView).text = getString(R.string.settings_current_user, "none")
        applicableSettings.put(logoutKey, Runnable { sharedPref?.edit()?.putString(getString(R.string.ra_user), "")?.apply() })
    }

    private fun applySettings() {
        activity?.findViewById<View>(R.id.settings_applying_fade)?.visibility = View.VISIBLE
        activity?.findViewById<View>(R.id.settings_applying)?.visibility = View.VISIBLE
        for (key in 0 until applicableSettings.size()) {
            applicableSettings.valueAt(key)?.run()
        }
        if (applicableSettings[hideConsolesKey] == null) {
            // Recreate activity now if no db operations are running
            activity?.recreate()
        }
    }

    private suspend fun removeConsoles(view: View, response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> {
                Log.w(TAG, response.second)
            }
            RetroAchievementsApi.RESPONSE.GET_CONSOLE_IDS -> {
                val db = context?.let { RetroAchievementsDatabase.getInstance(it) }
                try {
                    val reader = JSONArray(response.second)
                    counter = reader.length()
                    for (i in 0 until counter) {
                        val ctx = context?.applicationContext
                        if (db != null) {
                            CoroutineScope(IO).launch {
                                // Set each console to have 0 games
                                db.consoleDao()?.insertConsole(Console(reader.getJSONObject(i).getString("ID").toInt(), reader.getJSONObject(i).getString("Name"), 0))
                                if (ctx != null)
                                    RetroAchievementsApi.GetGameList(ctx, reader.getJSONObject(i).getString("ID")) {
                                        removeConsoles(view, it)
                                    }
                            }
                        }
                        Log.i(TAG, "calling api on $i")
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Couldn't parse console IDs", e)
                }
            }
            RetroAchievementsApi.RESPONSE.GET_GAME_LIST -> {
                try {
                    val reader = JSONArray(response.second)
                    val db = context?.let { RetroAchievementsDatabase.getInstance(it) }
                    if (reader.length() > 0 && db != null) {
                        CoroutineScope(IO).launch {
                            // Only update a console if it has more than 0 games
                            val id = reader.getJSONObject(0).getString("ConsoleID").toInt()
                            val name = reader.getJSONObject(0).getString("ConsoleName")
                            db.consoleDao()?.updateConsole(Console(id, name, reader.length()))
                            Log.d(TAG, "Updating console $id ($name): ${reader.length()} games")
                        }
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Couldn't parse game list", e)
                } finally {
                    counter--
                }
                if (counter == 0)
                    withContext(Main) { activity?.recreate() }
            }
            else -> {
                Log.v(TAG, "${response.first}: ${response.second}")
            }
        }
    }

    companion object {
        private val TAG = SettingsFragment::class.java.simpleName
    }
}