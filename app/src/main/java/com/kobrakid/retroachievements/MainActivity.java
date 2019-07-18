package com.kobrakid.retroachievements;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kobrakid.retroachievements.fragment.HomeFragment;
import com.kobrakid.retroachievements.fragment.LeaderboardsFragment;
import com.kobrakid.retroachievements.fragment.ListsFragment;
import com.kobrakid.retroachievements.fragment.SettingsFragment;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
        implements HomeFragment.OnFragmentInteractionListener,
        LeaderboardsFragment.OnFragmentInteractionListener,
        ListsFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener,
        RAAPICallback {

    private DrawerLayout myDrawer;

    // Request Codes
    public static final int BEGIN_LOGIN = 0;
    public static final int SHOW_RECENT_GAMES = 1;
    // Response Codes
    public static final int LOGIN_SUCCESS = 0;
    public static final int LOGIN_FAILURE = 1;
    public static final int LOGIN_CANCELLED = 2;

    public static String ra_user = null;
    static final String ra_api_user = "KobraKid1337";
    static final String ra_api_key = "LrY9UvdmckJWfgTsVC5SdTODrlTcHrkj";

    private boolean isActive = false;

    public RAAPIConnection apiConnection = null;
    public SharedPreferences sharedPref = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Try to get saved preferences and log in
        sharedPref = this.getSharedPreferences(getString(R.string.login_key), Context.MODE_PRIVATE);
        setTheme(ThemeToggler.getTheme(this, sharedPref));
        ra_user = sharedPref.getString(getString(R.string.ra_user), null);

        setContentView(R.layout.activity_main);
        setTitle("Home");

        // Set up title bar
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        TypedValue typedValue = new TypedValue();
        if (getTheme().resolveAttribute(R.drawable.ic_menu, typedValue, true)) {
            actionBar.setHomeAsUpIndicator(typedValue.resourceId);
        } else {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        // Set up navigation drawer
        myDrawer = findViewById(R.id.drawer_layout);
        setupDrawerContent((NavigationView) findViewById(R.id.nav_view));

        // Initialize API connection
        apiConnection = new RAAPIConnection(ra_api_user, ra_api_key, MainActivity.this);

        apiConnection.GetUserRankAndScore(ra_user, this);

        // Set up home fragment
        setupInitialFragment();
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        selectDrawerItem(item);
                        return true;
                    }
                }
        );
    }

    private void setupInitialFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.flContent, new HomeFragment()).commit();
    }

    public void selectDrawerItem(MenuItem item) {
        Fragment fragment = null;
        Class fragmentClass;

        // Determine selected Navigation Drawer item
        switch (item.getItemId()) {
            case R.id.nav_home_fragment:
                fragmentClass = HomeFragment.class;
                break;
            case R.id.nav_lists_fragment:
                fragmentClass = ListsFragment.class;
                break;
            case R.id.nav_leaderboards_fragment:
                fragmentClass = LeaderboardsFragment.class;
                break;
            case R.id.nav_settings_fragment:
                fragmentClass = SettingsFragment.class;
                break;
            default:
                fragmentClass = HomeFragment.class;
                break;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Show new Fragment in main view
        getSupportFragmentManager().beginTransaction().replace(R.id.flContent, fragment).commit();

        item.setChecked(true);
        setTitle(item.getTitle());
        myDrawer.closeDrawers();
    }

    public void showLogin(View view) {
        startActivityForResult(new Intent(this, LoginActivity.class), BEGIN_LOGIN);
    }

    public void showRecentGames(View view) {
        startActivityForResult(new Intent(this, RecentGamesActivity.class), SHOW_RECENT_GAMES);
    }

    public void showGameDetails(View view) {
        Intent intent = new Intent(this, GameDetailsActivity.class);
        Bundle extras = new Bundle();
        extras.putString("GameID",
                ((TextView) view.findViewById(R.id.game_summary_game_id)).getText().toString());
        intent.putExtras(extras);
        startActivity(intent);
    }

    public void changeTheme(View view) {
        // TODO Make a more elegant theme switcher
        String currTheme = sharedPref.getString(getString(R.string.theme_setting), "");
        if (currTheme.equals("Blank")) {
            sharedPref.edit().putString(getString(R.string.theme_setting), "TwentySixteen").apply();
        } else if (currTheme.equals("TwentySixteen")) {
            sharedPref.edit().putString(getString(R.string.theme_setting), "Green").apply();
        } else if (currTheme.equals("Green")) {
            sharedPref.edit().putString(getString(R.string.theme_setting), "Pony").apply();
        } else if (currTheme.equals("Pony")) {
            sharedPref.edit().putString(getString(R.string.theme_setting), "Red").apply();
        } else if (currTheme.equals("Red")) {
            sharedPref.edit().putString(getString(R.string.theme_setting), "Spooky").apply();
        } else {
            sharedPref.edit().putString(getString(R.string.theme_setting), "Blank").apply();
        }
        recreate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case LOGIN_SUCCESS:
                Context context = MainActivity.this;
                SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.login_key), Context.MODE_PRIVATE);
                ra_user = sharedPref.getString(getString(R.string.ra_user), null);

                ((TextView) findViewById(R.id.nav_username)).setText(ra_user);
                apiConnection.GetUserRankAndScore(ra_user, this);
                break;
            case LOGIN_FAILURE:
            case LOGIN_CANCELLED:
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
    public void callback(int responseCode, String response) {
        if (!isActive)
            return;
        if (responseCode == RAAPIConnection.RESPONSE_ERROR) {
            findViewById(R.id.nav_username).setVisibility(View.VISIBLE);
        }
        // The user has logged in
        if (responseCode == RAAPIConnection.RESPONSE_GET_USER_RANK_AND_SCORE) {
            // Parse JSON and plug in information
            JSONObject reader;
            try {
                reader = new JSONObject(response);
                ((TextView) findViewById(R.id.nav_stats)).setText(getString(R.string.nav_rank_score,
                        reader.getString("Score"),
                        reader.getString("Rank")));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ((TextView) findViewById(R.id.nav_username)).setText(ra_user);
            findViewById(R.id.nav_username).setVisibility(View.VISIBLE);
            findViewById(R.id.nav_stats).setVisibility(View.VISIBLE);
            Picasso.get()
                    .load("https://retroachievements.org/UserPic/" + ra_user + ".png")
                    .into((ImageView) findViewById(R.id.nav_profile_picture));
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                myDrawer.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}