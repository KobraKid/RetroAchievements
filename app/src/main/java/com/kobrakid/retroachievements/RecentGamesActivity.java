package com.kobrakid.retroachievements;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * This class will display a more comprehensive list of recent games, rather than the
 * quick 5-game summary present on the home screen.
 */
public class RecentGamesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_games);
    }
}
