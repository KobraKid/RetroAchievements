package com.kobrakid.retroachievements;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        setTheme(ThemeManager.getTheme(this, sharedPref));

        setContentView(R.layout.activity_login);
        final EditText login = findViewById(R.id.login_username);
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
        super.onBackPressed();
        setResult(Consts.CANCELLED);
    }

    public void login(@SuppressWarnings("unused") View view) {
        String ra_user = ((EditText) findViewById(R.id.login_username)).getText().toString();
        String ra_api = ((EditText) findViewById(R.id.login_api_key)).getText().toString();
        if (ra_user.length() > 0) {
            // Successfully logged in, save the new credentials and return
            this.getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE).edit().putString(getString(R.string.ra_user), ra_user).apply();
            this.getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE).edit().putString(getString(R.string.ra_api_key), ra_api).apply();
            setResult(Consts.SUCCESS);
            Toast.makeText(getApplicationContext(), getString(R.string.new_login_welcome, ra_user), Toast.LENGTH_SHORT).show();
        } else {
            setResult(Consts.FAILURE);
        }
        finish();
    }

    public void cancel(@SuppressWarnings("unused") View view) {
        onBackPressed();
    }

    public void help(@SuppressWarnings("Unused") View view) {
//        DialogFragment apiHelp = new APIHelpDialogFragment(this);
//        apiHelp.show(getSupportFragmentManager(), TAG);
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.api_detect_dialog_title))
                .setMessage(getString(R.string.api_detect_dialog_desc))
                .setPositiveButton(getString(R.string.api_detect_go), (dialogInterface, i) -> {
                    startActivityForResult(new Intent(this, ApiPullActivity.class), Consts.PULL_API_KEY);
                })
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> { /* Cancel */ })
                .create()
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Consts.PULL_API_KEY) {
            switch (resultCode) {
                case Consts.SUCCESS:
                    if (data != null) {
                        ((TextView) findViewById(R.id.login_username)).setText(data.getStringExtra(getString(R.string.ra_user)).substring(1).replaceFirst(".$",""));
                        ((TextView) findViewById(R.id.login_api_key)).setText(data.getStringExtra(getString(R.string.ra_api_key)).substring(1, 33));
                    }
                    break;
                case Consts.CANCELLED:
                    Log.d(TAG, "API SCRAPING CANCELLED");
                    break;
                case Consts.FAILURE:
                    Log.d(TAG, "FAILED TO PARSE API KEY");
                default:
                    break;
            }
        }
    }

}
