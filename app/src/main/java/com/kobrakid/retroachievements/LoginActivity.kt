package com.kobrakid.retroachievements

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kobrakid.retroachievements.ThemeManager.getTheme

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE)
        setTheme(getTheme(this, sharedPref))
        setContentView(R.layout.activity_login)
        val login = findViewById<EditText>(R.id.login_username)
        login.setOnEditorActionListener { textView: TextView, actionID: Int, _: KeyEvent? ->
            if (actionID == EditorInfo.IME_ACTION_SEND) {
                login(textView)
                return@setOnEditorActionListener true
            }
            false
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Consts.CANCELLED)
    }

    fun login(view: View) {
        when (view.id) {
            R.id.login_button -> {
                val raUser = (findViewById<View>(R.id.login_username) as EditText).text.toString()
                val raApi = (findViewById<View>(R.id.login_api_key) as EditText).text.toString()
                if (raUser.isNotEmpty() && raApi.isNotEmpty()) {
                    // Successfully logged in, save the new credentials
                    getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE).edit().putString(getString(R.string.ra_user), raUser).apply()
                    getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE).edit().putString(getString(R.string.ra_api_key), raApi).apply()
                    setResult(Consts.SUCCESS)
                    Toast.makeText(applicationContext, getString(R.string.new_login_welcome, raUser), Toast.LENGTH_SHORT).show()
                } else {
                    setResult(Consts.FAILURE)
                }
            }
            R.id.cancel_button -> setResult(Consts.CANCELLED)
        }
        finish()
    }

    fun help(@Suppress("UNUSED_PARAMETER") view: View?) {
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.api_detect_dialog_title))
                .setMessage(getString(R.string.api_detect_dialog_desc))
                .setPositiveButton(getString(R.string.api_detect_go)) { _: DialogInterface?, _: Int -> startActivityForResult(Intent(this, ApiKeyDetectorActivity::class.java), Consts.PULL_API_KEY) }
                .setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int -> }
                .create()
                .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Consts.PULL_API_KEY) {
            when (resultCode) {
                Consts.SUCCESS -> if (data != null) {
                    findViewById<TextView>(R.id.login_username).text = data.getStringExtra(getString(R.string.ra_user))?.substring(1)?.replaceFirst(".$".toRegex(), "")
                    findViewById<TextView>(R.id.login_api_key).text = data.getStringExtra(getString(R.string.ra_api_key))?.substring(1, 33)
                    Log.i(TAG, "API SCRAPING SUCCESS")
                }
                Consts.CANCELLED -> Log.d(TAG, "API SCRAPING CANCELLED")
                Consts.FAILURE -> Log.d(TAG, "API SCRAPING FAILED")
                else -> {
                }
            }
        }
    }

    companion object {
        private val TAG = LoginActivity::class.java.simpleName
    }
}