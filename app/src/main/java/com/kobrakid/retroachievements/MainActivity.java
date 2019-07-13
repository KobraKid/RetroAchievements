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
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity
        implements HomeFragment.OnFragmentInteractionListener,
        LeaderboardsFragment.OnFragmentInteractionListener,
        ListsFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener,
        RAAPICallback {

    private DrawerLayout myDrawer;

    // Request Codes
    static final int BEGIN_LOGIN = 0;
    // Response Codes
    static final int LOGIN_SUCCESS = 0;
    static final int LOGIN_FAILURE = 1;
    static final int LOGIN_CANCELLED = 2;

    private static String ra_user = null;
    private static String ra_api_key = null; // "LrY9UvdmckJWfgTsVC5SdTODrlTcHrkj";

    public RAAPIConnection apiConnection = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setTitle("Home");

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        myDrawer = findViewById(R.id.drawer_layout);
        setupDrawerContent((NavigationView) findViewById(R.id.nav_view));

        // Try to get saved preferences and log in
        Context context = MainActivity.this;
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.login_key), Context.MODE_PRIVATE);
        ra_user = sharedPref.getString(getString(R.string.ra_user), null);
        ra_api_key = sharedPref.getString(getString(R.string.ra_api_key), null);
        apiConnection = new RAAPIConnection(ra_user, ra_api_key, MainActivity.this);
        apiConnection.GetUserRankAndScore(ra_user, this);

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
        HomeFragment fragment = new HomeFragment();
//        Bundle bundle = new Bundle();
//        bundle.putString(getString(R.string.ra_user), ra_user);
//        bundle.putString(getString(R.string.ra_api_key), ra_api_key);
//        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.flContent, fragment).commit();
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

        // Pass login information to new Fragment
//        Bundle bundle = new Bundle();
//        bundle.putString(getString(R.string.ra_user), ra_user);
//        bundle.putString(getString(R.string.ra_api_key), ra_api_key);
//        fragment.setArguments(bundle);

        // Show new Fragment in main view
        getSupportFragmentManager().beginTransaction().replace(R.id.flContent, fragment).commit();

        item.setChecked(true);
        setTitle(item.getTitle());
        myDrawer.closeDrawers();
    }

    public void showLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, BEGIN_LOGIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case LOGIN_SUCCESS:
                Context context = MainActivity.this;
                SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.login_key), Context.MODE_PRIVATE);
                ra_user = sharedPref.getString(getString(R.string.ra_user), null);
                ra_api_key = sharedPref.getString(getString(R.string.ra_api_key), null);

                ((TextView) findViewById(R.id.nav_username)).setText(ra_user);
                apiConnection.updateCredentials(ra_user, ra_api_key);
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
    }

    @Override
    public void callback(int responseCode, String response) {
        if (response.equals("Invalid API Key")) {
            return;
        }
        // The user has logged in
        if (responseCode == RAAPIConnection.RESPONSE_GET_USER_RANK_AND_SCORE) {
            ((TextView) findViewById(R.id.nav_username)).setText(ra_user);
            ((TextView) findViewById(R.id.nav_stats)).setText(response);
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

interface RAAPICallback {
    void callback(int responseCode, String response);
}