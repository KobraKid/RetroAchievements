package com.kobrakid.retroachievements;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.login_key), Context.MODE_PRIVATE);
        setTheme(ThemeManager.getTheme(this, sharedPref));

        setContentView(R.layout.activity_login);
        final EditText login = findViewById(R.id.login_field);
        login.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEND) {
                    login(textView);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(MainActivity.LOGIN_CANCELLED);
    }

    public void login(View view) {
        String ra_user = ((EditText) findViewById(R.id.login_field)).getText().toString();
        // Successfully logged in, save the new credentials and return
        this.getSharedPreferences(getString(R.string.login_key), Context.MODE_PRIVATE).edit().putString(getString(R.string.ra_user), ra_user).apply();

        setResult(MainActivity.LOGIN_SUCCESS);

        Toast.makeText(getApplicationContext(), getString(R.string.new_login_welcome, ra_user), Toast.LENGTH_SHORT).show();

        finish();
    }

}
