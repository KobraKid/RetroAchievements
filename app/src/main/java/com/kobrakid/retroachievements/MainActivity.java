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
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
        implements HomeFragment.OnFragmentInteractionListener,
        LeaderboardsFragment.OnFragmentInteractionListener,
        ListsFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener {

    private DrawerLayout myDrawer;
    private Toolbar myToolbar;
    private NavigationView navDrawer;

    // Request Codes
    static final int BEGIN_LOGIN = 0;
    // Response Codes
    static final int LOGIN_SUCCESS = 0;
    static final int LOGIN_FAILURE = 1;
    static final int LOGIN_CANCELLED = 2;

    private static String ra_user = null; //"KobraKid1337";
    private static String ra_api_key = null; // "LrY9UvdmckJWfgTsVC5SdTODrlTcHrkj";

    private RAAPIConnection apiConnection = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setTitle("Home");

        myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        myDrawer = findViewById(R.id.drawer_layout);
        navDrawer = findViewById(R.id.nav_view);
        setupDrawerContent(navDrawer);

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
        // Try to get saved preferences and log in
        Context context = MainActivity.this;
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.login_key), Context.MODE_PRIVATE);
        ra_user = sharedPref.getString(getString(R.string.ra_user), null);
        ra_api_key = sharedPref.getString(getString(R.string.ra_api_key), null);

        HomeFragment fragment = new HomeFragment();
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.ra_user), ra_user);
        bundle.putString(getString(R.string.ra_api_key), ra_api_key);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.flContent, fragment).commit();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        // If logged in, populate initial fragment with user's info
//        if (ra_user != null && ra_api_key != null) {
//            apiConnection = new RAAPIConnection(ra_user, ra_api_key, MainActivity.this);
//        }
//        if (ra_user != null) {
//            ((TextView) findViewById(R.id.nav_username)).setText(ra_user);
//        }
//        if (apiConnection == null) {
//            apiConnection = new RAAPIConnection(ra_user, ra_api_key, MainActivity.this);
//        }
//        apiConnection.GetUserRankAndScore(ra_user, new RAAPICallback((TextView) findViewById(R.id.nav_stats)));
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
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.ra_user), ra_user);
        bundle.putString(getString(R.string.ra_api_key), ra_api_key);
        fragment.setArguments(bundle);

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
                if (apiConnection == null) {
                    apiConnection = new RAAPIConnection(ra_user, ra_api_key, MainActivity.this);
                }
                apiConnection.GetUserRankAndScore(ra_user, new RAAPICallback((TextView) findViewById(R.id.nav_stats)));
//                apiConnection.GetUserRankAndScore(ra_user, new Runnable() {
//                    @Override
//                    public void run() {
//
//                    }
//                });
                findViewById(R.id.nav_stats).setVisibility(View.VISIBLE);

                // TODO Show toast to confirm login

                break;
            default:

                // TODO Show toast to explain login failure

                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        myDrawer.closeDrawers();
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
