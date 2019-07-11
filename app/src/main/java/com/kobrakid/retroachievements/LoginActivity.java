package com.kobrakid.retroachievements;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    private EditText username;
    private EditText api_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void login(View view) {
        username = (EditText) findViewById(R.id.login_field);
        api_key = findViewById(R.id.api_field);
        Context context = LoginActivity.this;
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.login_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.ra_user), username.getText().toString());
        editor.putString(getString(R.string.ra_api_key), api_key.getText().toString());
        editor.commit();
        setResult(MainActivity.LOGIN_SUCCESS);
        finish();
    }

    public void showHelp(View view) {
        Intent intent = new Intent(this, LoginHelpActivity.class);
        startActivity(intent);
    }
}
