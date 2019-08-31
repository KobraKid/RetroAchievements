package com.kobrakid.retroachievements;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        setTheme(ThemeManager.getTheme(this, sharedPref));

        setContentView(R.layout.activity_login);
        final EditText login = findViewById(R.id.login_field);
        login.setOnEditorActionListener((textView, actionID, keyEvent) -> {
            if (actionID == EditorInfo.IME_ACTION_SEND) {
                login(textView);
                return true;
            }
            return false;
        });
    }

    @Override
    public void onBackPressed() {
        setResult(MainActivity.LOGIN_CANCELLED);
        super.onBackPressed();
    }

    public void login(@SuppressWarnings("unused") View view) {
        String ra_user = ((EditText) findViewById(R.id.login_field)).getText().toString();
        if (ra_user.length() > 0) {
            // Successfully logged in, save the new credentials and return
            this.getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE).edit().putString(getString(R.string.ra_user), ra_user).apply();
            setResult(MainActivity.LOGIN_SUCCESS);
            Toast.makeText(getApplicationContext(), getString(R.string.new_login_welcome, ra_user), Toast.LENGTH_SHORT).show();
        } else {
            setResult(MainActivity.LOGIN_FAILURE);
        }
        finish();
    }

}
