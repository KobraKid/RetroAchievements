package com.kobrakid.retroachievements;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
        implements HomeFragment.OnFragmentInteractionListener,
        LeaderboardsFragment.OnFragmentInteractionListener,
        ListsFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener {

    private DrawerLayout myDrawer;
    private Toolbar myToolbar;
    private NavigationView navDrawer;
    private ActionBarDrawerToggle drawerToggle;

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

    public void selectDrawerItem(MenuItem item) {
        Fragment fragment = null;
        Class fragmentClass;
        switch(item.getItemId()) {
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

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        item.setChecked(true);
        setTitle(item.getTitle());
        myDrawer.closeDrawers();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {}

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
