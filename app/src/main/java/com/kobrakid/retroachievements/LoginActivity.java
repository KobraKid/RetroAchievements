package com.kobrakid.retroachievements;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity implements RAAPICallback {

    private EditText ra_user;
    private EditText ra_api_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void login(View view) {
        ra_user = findViewById(R.id.login_field);
        ra_api_key = findViewById(R.id.api_field);
        testLogin();
    }

    private void testLogin() {
        RAAPIConnection apiConnection
                = new RAAPIConnection(ra_user.getText().toString(), ra_api_key.getText().toString(), LoginActivity.this);
        apiConnection.GetUserRankAndScore(ra_user.getText().toString(), this);
    }

    @Override
    public void callback(int responseCode, String response) {
        CharSequence text;
        Toast toast;
        if (!response.equals("Invalid API Key")) {
            // Successfully logged in, save the new credentials and return
            Context context = LoginActivity.this;
            SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.login_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.ra_user), ra_user.getText().toString());
            editor.putString(getString(R.string.ra_api_key), ra_api_key.getText().toString());
            editor.commit();
            setResult(MainActivity.LOGIN_SUCCESS);

            text = "Hello, " + ra_user.getText().toString() + ". Successfully logged in.";
            toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
            toast.show();

            finish();
        } else {
            setResult(MainActivity.LOGIN_FAILURE);
            text = "Error logging in. Please try again";
            toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(MainActivity.LOGIN_CANCELLED);
    }

    public void showHelp(View view) {
        Intent intent = new Intent(this, LoginHelpActivity.class);
        startActivity(intent);
    }
}
