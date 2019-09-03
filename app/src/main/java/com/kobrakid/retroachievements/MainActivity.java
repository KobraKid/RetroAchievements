package com.kobrakid.retroachievements;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.kobrakid.retroachievements.fragment.HomeFragment;
import com.kobrakid.retroachievements.fragment.LeaderboardsFragment;
import com.kobrakid.retroachievements.fragment.ListsFragment;
import com.kobrakid.retroachievements.fragment.SettingsFragment;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/**
 * The entry point for the app, and the Activity that manages most of the basic Fragments used
 * throughout the app.
 */
public class MainActivity extends AppCompatActivity implements RAAPICallback, SettingsFragment.OnFragmentInteractionListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    // Request Codes
    private static final int BEGIN_LOGIN = 0;
    private static final int SHOW_RECENT_GAMES = 1;
    // Response Codes
    public static final int LOGIN_SUCCESS = 0;
    public static final int LOGIN_FAILURE = 1;
    public static final int LOGIN_CANCELLED = 2;

    public RAAPIConnection apiConnection = null;

    public static String ra_user = null;
    static final String ra_api_user = "KobraKid1337";
    @SuppressWarnings("SpellCheckingInspection")
    static final String ra_api_key = "LrY9UvdmckJWfgTsVC5SdTODrlTcHrkj";

    private Fragment fragment;
    private String activeFragmentTag;
    private boolean isActive = false;
    private DrawerLayout myDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Try to get saved preferences and log in
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        setTheme(ThemeManager.getTheme(this, sharedPref));
        ra_user = sharedPref.getString(getString(R.string.ra_user), null);

        setContentView(R.layout.activity_main);
        setTitle("Home");

        // Set up title bar
        setSupportActionBar(findViewById(R.id.main_toolbar));
        final ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        // Set up navigation drawer
        myDrawer = findViewById(R.id.drawer_layout);
        ((NavigationView) findViewById(R.id.nav_view)).setNavigationItemSelectedListener(this::selectDrawerItem);

        // Initialize API connection
        apiConnection = new RAAPIConnection(MainActivity.this);

        apiConnection.GetUserRankAndScore(ra_user, this);

        if (savedInstanceState == null) {
            // Set up home fragment
            activeFragmentTag = "HomeFragment";
            getSupportFragmentManager().beginTransaction().replace(R.id.flContent, new HomeFragment(), activeFragmentTag).commit();
        } else {
            activeFragmentTag = savedInstanceState.getString("ActiveFragmentTag");
            fragment = getSupportFragmentManager().findFragmentByTag(activeFragmentTag);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case LOGIN_SUCCESS:
                Context context = MainActivity.this;
                SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
                ra_user = sharedPref.getString(getString(R.string.ra_user), null);

                if (ra_user != null) {
                    Log.v(TAG, "Logging in as " + ra_user);
                    ((TextView) findViewById(R.id.nav_username)).setText(ra_user);
                    apiConnection.GetUserRankAndScore(ra_user, this);
                }
                break;
            case LOGIN_CANCELLED:
                Log.d(TAG, "LOGIN CANCELLED");
            case LOGIN_FAILURE:
                Log.d(TAG, "NOT LOGGING IN");
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        myDrawer.closeDrawers();
        isActive = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("ActiveFragmentTag", activeFragmentTag);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (fragment instanceof ListsFragment && ((ListsFragment) fragment).isShowingGames) {
                ((ListsFragment) fragment).onBackPressed();
            } else {
                myDrawer.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (fragment instanceof ListsFragment && ((ListsFragment) fragment).consoleAdapter.isExpanded)
            ((ListsFragment) fragment).onBackPressed();
        else
            super.onBackPressed();
    }

    @Override
    public void callback(int responseCode, String response) {
        if (!isActive)
            return;
        // The user has logged in
        if (responseCode == RAAPIConnection.RESPONSE_GET_USER_RANK_AND_SCORE) {
            // Parse JSON and plug in information
            JSONObject reader;
            try {
                reader = new JSONObject(response);
                ((TextView) findViewById(R.id.nav_stats)).setText(getString(R.string.nav_rank_score,
                        reader.getString("Score"),
                        reader.getString("Rank")));
                ((TextView) findViewById(R.id.nav_username)).setText(ra_user);
                findViewById(R.id.nav_stats).setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(Consts.BASE_URL + "/" + Consts.USER_PIC_POSTFIX + "/" + ra_user + ".png")
                        .into((ImageView) findViewById(R.id.nav_profile_picture));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /* Navigation-related functions */

    private boolean selectDrawerItem(MenuItem item) {
        fragment = null;
        Class fragmentClass;

        // Determine selected Navigation Drawer item
        switch (item.getItemId()) {
            case R.id.nav_lists_fragment:
                fragmentClass = ListsFragment.class;
                activeFragmentTag = "ListsFragment";
                break;
            case R.id.nav_leaderboards_fragment:
                fragmentClass = LeaderboardsFragment.class;
                activeFragmentTag = "LeaderboardsFragment";
                break;
            case R.id.nav_settings_fragment:
                fragmentClass = SettingsFragment.class;
                activeFragmentTag = "SettingsFragment";
                break;
            case R.id.nav_home_fragment:
            default:
                fragmentClass = HomeFragment.class;
                activeFragmentTag = "HomeFragment";
                break;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // Show new Fragment in main view
        getSupportFragmentManager().beginTransaction().replace(R.id.flContent, fragment, activeFragmentTag).commit();

        item.setChecked(true);
        myDrawer.closeDrawers();
        return true;
    }

    public void showGameDetails(View view) {
        Intent intent = new Intent(this, GameDetailsActivity.class);
        Bundle extras = new Bundle();
        extras.putString("GameID",
                ((TextView) view.findViewById(R.id.game_summary_game_id)).getText().toString());
        intent.putExtras(extras);
        startActivity(intent);
    }

    /* Home Fragment Interface Implementation */

    public void showLogin(@SuppressWarnings("unused") View view) {
        myDrawer.closeDrawers();
        startActivityForResult(new Intent(this, LoginActivity.class), BEGIN_LOGIN);
    }

    public void showRecentGames(@SuppressWarnings("unused") View view) {
        startActivity(new Intent(this, RecentGamesActivity.class));
    }

    /* Leaderboards Fragment Interface Implementations */

    public void toggleUsers(View topTenUsersToggle) {
        final View topTenUsers = findViewById(R.id.leaderboards_users);
        topTenUsers.setZ(-1);
        if (topTenUsers.getVisibility() == View.GONE) {
            ((ImageButton) topTenUsersToggle).setImageDrawable(getDrawable(R.drawable.ic_arrow_drop_down));
            topTenUsers
                    .animate()
                    .alpha(1.0f)
                    .translationYBy(topTenUsers.getHeight())
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationEnd(animation);
                            topTenUsers.setVisibility(View.VISIBLE);
                        }
                    });
        } else {
            ((ImageButton) topTenUsersToggle).setImageDrawable(getDrawable(R.drawable.ic_arrow_drop_up));
            topTenUsers
                    .animate()
                    .alpha(0.0f)
                    .translationYBy(-topTenUsers.getHeight())
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            topTenUsers.setVisibility(View.GONE);
                        }
                    });
        }
    }

    /* Settings Fragment Interface implementations */

    @Override
    public void logout(View view) {
        if (fragment instanceof SettingsFragment) {
            ((SettingsFragment) fragment).logout();
        }
    }

    @Override
    public void applySettings(View view) {
        if (fragment instanceof SettingsFragment) {
            ((SettingsFragment) fragment).applySettings();
        }
    }
}