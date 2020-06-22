package com.kobrakid.retroachievements.fragment

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.fragment.app.Fragment
import com.kobrakid.retroachievements.*
import com.kobrakid.retroachievements.AppExecutors.Companion.instance
import com.kobrakid.retroachievements.database.Console
import com.kobrakid.retroachievements.database.RetroAchievementsDatabase
import org.json.JSONArray
import org.json.JSONException
import java.util.*

class SettingsFragment : Fragment(), RAAPICallback {
    // Unused, but guarantees that the parent Activity implements OnFragmentInteractionListener
    private var listener: OnFragmentInteractionListener? = null
    private var apiConnection: RAAPIConnection? = null
    private var sharedPref: SharedPreferences? = null
    private val consoleID = StringBuilder()
    private val consoleName = StringBuilder()
    private val applicableSettings = SparseArray<Runnable?>()

    // Can be local, but used to index which settings have been modified
    private val logoutKey = 0
    private val hideConsolesKey = 1
    private val hideGamesKey = 2
    private val changeThemeKey = 3

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity?.title = "Settings"

        // Initialize preferences object
        sharedPref = context?.getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE)

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Set up views
        val theme = sharedPref?.getString(getString(R.string.theme_setting), "")
        (view.findViewById<View>(R.id.settings_current_theme) as TextView).text = getString(R.string.settings_current_theme, theme)
        (view.findViewById<View>(R.id.settings_current_user) as TextView).text = if (MainActivity.ra_user == null) getString(R.string.settings_no_current_user) else getString(R.string.settings_current_user, MainActivity.ra_user)
        (view.findViewById<View>(R.id.settings_theme_dropdown) as Spinner).adapter = object : ArrayAdapter<String?>(context!!, android.R.layout.simple_spinner_dropdown_item, Consts.THEMES) {
            override fun isEnabled(position: Int): Boolean {
                return Consts.THEMES_ENABLE_ARRAY[position]
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val textView = super.getDropDownView(position, convertView, parent) as TextView
                if (isEnabled(position)) {
                    val typedValue = TypedValue()
                    context.theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
                    textView.setTextColor(resources.getColor(typedValue.resourceId))
                } else {
                    textView.setTextColor(Color.GRAY)
                }
                return textView
            }
        }
        (view.findViewById<View>(R.id.settings_theme_dropdown) as Spinner).setSelection(listOf(*Consts.THEMES).indexOf(theme))
        (view.findViewById<View>(R.id.settings_theme_dropdown) as Spinner).onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, pos: Int, id: Long) {
                if (pos > 0) changeTheme(adapterView.getItemAtPosition(pos).toString())
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        (view.findViewById<View>(R.id.settings_hide_consoles) as CheckBox).isChecked = sharedPref?.getBoolean(getString(R.string.empty_console_hide_setting), false)!!
        if (sharedPref?.getBoolean(getString(R.string.empty_console_hide_setting), false)!!) view.findViewById<View>(R.id.settings_hide_consoles_warning).visibility = View.GONE
        (view.findViewById<View>(R.id.settings_hide_games) as CheckBox).isChecked = sharedPref?.getBoolean(getString(R.string.empty_game_hide_setting), false)!!
        (view.findViewById<View>(R.id.settings_hide_consoles) as CheckBox).setOnCheckedChangeListener { _: CompoundButton?, b: Boolean -> hideConsoles(view, b) }
        (view.findViewById<View>(R.id.settings_hide_games) as CheckBox).setOnCheckedChangeListener { _: CompoundButton?, b: Boolean -> hideGames(view, b) }
        apiConnection = (activity as MainActivity?)!!.apiConnection
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = try {
            activity as OnFragmentInteractionListener?
        } catch (e: ClassCastException) {
            throw ClassCastException(Objects.requireNonNull(activity).toString()
                    + " must implement OnHeadlineSelectedListener")
        }
    }

    /* Settings-related Functions */
    private fun changeTheme(theme: String) {
        applicableSettings.remove(changeThemeKey)
        applicableSettings.put(changeThemeKey, Runnable {
            Log.d(TAG, "Saving theme $theme")
            sharedPref!!.edit().putString(getString(R.string.theme_setting), theme).apply()
        })
    }

    private fun hideConsoles(view: View, hide: Boolean) {
        view.findViewById<View>(R.id.settings_hide_consoles_warning).visibility = if (hide) View.GONE else View.VISIBLE
        if (applicableSettings[hideConsolesKey] != null) {
            applicableSettings.remove(hideConsolesKey)
        } else {
            val callback: RAAPICallback = this
            applicableSettings.put(hideConsolesKey, Runnable {
                sharedPref!!.edit().putBoolean(getString(R.string.empty_console_hide_setting), hide).apply()
                if (hide) {
                    // Get all consoles and store their game counts
                    val db = RetroAchievementsDatabase.getInstance(context)
                    instance!!.diskIO().execute {
                        db.consoleDao().clearTable()
                        Log.d(TAG, "Clearing table")
                    }
                    apiConnection!!.GetConsoleIDs(callback)
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
            applicableSettings.put(hideGamesKey, Runnable { sharedPref!!.edit().putBoolean(getString(R.string.empty_game_hide_setting), hide).apply() })
        }
    }

    fun logout() {
        (activity?.findViewById<View>(R.id.settings_current_user) as TextView).text = getString(R.string.settings_current_user, "none")
        applicableSettings.put(logoutKey, Runnable { sharedPref!!.edit().putString(getString(R.string.ra_user), null).apply() })
    }

    fun applySettings() {
        activity?.findViewById<View>(R.id.settings_applying_fade)?.visibility = View.VISIBLE
        activity?.findViewById<View>(R.id.settings_applying)?.visibility = View.VISIBLE
        for (key in 0 until applicableSettings.size()) {
            applicableSettings.valueAt(key)!!.run()
        }
        if (applicableSettings[hideConsolesKey] == null) {
            // Recreate activity now if no db operations are running
            activity!!.recreate()
        }
    }

    override fun callback(responseCode: Int, response: String) {
        if (responseCode == RAAPIConnection.RESPONSE_ERROR) return
        if (responseCode == RAAPIConnection.RESPONSE_GET_CONSOLE_IDS) {
            val db = RetroAchievementsDatabase.getInstance(context)
            val connection = apiConnection
            val callback: RAAPICallback = this
            instance?.diskIO()?.execute {
                try {
                    val reader = JSONArray(response)
                    for (i in 0 until reader.length()) {
                        val consoles = db.consoleDao().getConsoleWithID(reader.getJSONObject(i).getString("ID").toInt())
                        if (consoles.size == 0) {
                            consoleID.delete(0, consoleID.length)
                            consoleName.delete(0, consoleName.length)
                            consoleID.append(reader.getJSONObject(i).getString("ID"))
                            consoleName.append(reader.getJSONObject(i).getString("Name"))
                            connection!!.GetGameList(reader.getJSONObject(i).getString("ID"), callback)
                            return@execute
                        }
                    }
                    instance!!.mainThread().execute { activity?.recreate() }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        } else if (responseCode == RAAPIConnection.RESPONSE_GET_GAME_LIST) {
            try {
                val reader = JSONArray(response)
                val db = RetroAchievementsDatabase.getInstance(context)
                instance!!.diskIO().execute {
                    db.consoleDao().insertConsole(Console(consoleID.toString().toInt(), consoleName.toString(), reader.length()))
                    Log.d(TAG, "Adding console " + consoleName.toString() + "(" + consoleID.toString() + "): " + reader.length() + " games")
                }
                // Recurse until all consoles are added to db
                apiConnection!!.GetConsoleIDs(this)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    /* Inner Classes and Interfaces */
    interface OnFragmentInteractionListener {
        fun logout(view: View?)
        fun applySettings(view: View?)
    }

    companion object {
        private val TAG = SettingsFragment::class.java.simpleName
    }
}