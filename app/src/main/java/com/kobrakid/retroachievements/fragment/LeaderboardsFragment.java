package com.kobrakid.retroachievements.fragment;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.google.common.collect.RowSortedTable;
import com.google.common.collect.TreeBasedTable;
import com.kobrakid.retroachievements.LeaderboardActivity;
import com.kobrakid.retroachievements.MainActivity;
import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.RAAPICallback;
import com.kobrakid.retroachievements.RAAPIConnection;
import com.kobrakid.retroachievements.adapter.LeaderboardsAdapter;
import com.kobrakid.retroachievements.adapter.UserRankingAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;

public class LeaderboardsFragment extends Fragment implements RAAPICallback {

    private static final String TAG = LeaderboardsFragment.class.getSimpleName();

    private RAAPIConnection apiConnection;
    private static boolean isActive = false;
    private boolean hasParsedUsers = false, hasParsedLeaderboards = false;

    private LeaderboardsAdapter leaderboardsAdapter;
    private RowSortedTable<Integer, String, String> table, tableFiltered;
    private UserRankingAdapter userRankingAdapter;
    private final ArrayList<String> userRankings = new ArrayList<>();
    private final ArrayList<String> userNames = new ArrayList<>();
    private final ArrayList<String> userScores = new ArrayList<>();
    private final ArrayList<String> userRatios = new ArrayList<>();

    private Spinner consoleDropdown;
    private String filteredConsole = "", filteredTitle = "";

    public LeaderboardsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Set up API connection
        apiConnection = ((MainActivity) Objects.requireNonNull(getActivity())).apiConnection;

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_leaderboards, container, false);

        // Set up user rankings
        RecyclerView topUsers = view.findViewById(R.id.leaderboards_users);
        userRankings.clear();
        userNames.clear();
        userScores.clear();
        userRatios.clear();
        userRankingAdapter = new UserRankingAdapter(userRankings, userNames, userScores, userRatios);
        topUsers.setAdapter(userRankingAdapter);
        topUsers.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up Leaderboards List
        RecyclerView leaderboardsRecycler = view.findViewById(R.id.leaderboards_games);
        table = TreeBasedTable.create();
        tableFiltered = TreeBasedTable.create();
        leaderboardsAdapter = new LeaderboardsAdapter(this, table, tableFiltered);
        leaderboardsRecycler.setAdapter(leaderboardsAdapter);
        leaderboardsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up Filters
        consoleDropdown = view.findViewById(R.id.leaderboards_console_filter);
        EditText leaderboardsFilter = view.findViewById(R.id.leaderboards_filter);
        consoleDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                filteredConsole = adapterView.getItemAtPosition(pos).toString();
                leaderboardsAdapter.getFilter().filter(filteredConsole + "\t" + filteredTitle);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                filteredConsole = "";
                leaderboardsAdapter.getFilter().filter(filteredConsole + "\t" + filteredTitle);
            }
        });
        leaderboardsFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                filteredTitle = charSequence.toString();
                leaderboardsAdapter.getFilter().filter(filteredConsole + "\t" + filteredTitle);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        isActive = true;
        if (!hasParsedUsers) {
            apiConnection.GetTopTenUsers(this);
        }
        if (!hasParsedLeaderboards)
            apiConnection.GetLeaderboards(true, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        isActive = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isActive = false;
    }

    @Override
    public void callback(int responseCode, String response) {
        if (!isActive)
            return;
        if (responseCode == RAAPIConnection.RESPONSE_GET_TOP_TEN_USERS) {
            try {
                JSONArray reader = new JSONArray(response);
                int count = userRankings.size();
                userRankings.clear();
                userNames.clear();
                userScores.clear();
                userRatios.clear();
                userRankingAdapter.notifyItemRangeRemoved(0, count);
                for (int i = 0; i < reader.length(); i++) {
                    userRankings.add("" + (i + 1));
                    userNames.add(((JSONObject) reader.get(i)).getString("1"));
                    userScores.add(((JSONObject) reader.get(i)).getString("2"));
                    userRatios.add(((JSONObject) reader.get(i)).getString("3"));
                    userRankingAdapter.notifyItemInserted(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (!userNames.contains(MainActivity.ra_user))
                apiConnection.GetUserSummary(MainActivity.ra_user, 0, this);
            hasParsedUsers = true;
        } else if (responseCode == RAAPIConnection.RESPONSE_GET_USER_SUMMARY) {
            try {
                JSONObject reader = new JSONObject(response);
                userRankings.add(reader.getString("Rank"));
                userNames.add(MainActivity.ra_user);
                userScores.add(reader.getString("TotalPoints"));
                userRatios.add(reader.getString("TotalTruePoints"));
                userRankingAdapter.notifyItemInserted(userNames.size() - 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (responseCode == RAAPIConnection.RESPONSE_GET_LEADERBOARDS) {
            ObjectAnimator animation = ObjectAnimator.ofInt(Objects.requireNonNull(getActivity()).findViewById(R.id.leaderboards_progress), "secondaryProgress", 100);
            animation.setDuration(1000);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            animation.start();
            new LeaderboardsAsyncTask(getActivity(), getContext(), consoleDropdown, leaderboardsAdapter, table, tableFiltered).execute(response);
            hasParsedLeaderboards = true;
        }
    }

    public void onClick(Map<String, String> leaderboard) {
        Intent intent = new Intent(this.getActivity(), LeaderboardActivity.class);
        Bundle extras = new Bundle();
        extras.putString("ID", leaderboard.get("ID"));
        extras.putString("GAME", leaderboard.get("GAME"));
        extras.putString("IMAGE", leaderboard.get("IMAGE"));
        extras.putString("CONSOLE", leaderboard.get("CONSOLE"));
        extras.putString("TITLE", leaderboard.get("TITLE"));
        extras.putString("DESCRIPTION", leaderboard.get("DESCRIPTION"));
        extras.putString("TYPE", leaderboard.get("TYPE"));
        extras.putString("NUMRESULTS", leaderboard.get("NUMRESULTS"));
        intent.putExtras(extras);
        Log.v(TAG, leaderboard.toString());
        startActivity(intent);
    }

    /* Inner Classes and Interfaces */

    private static class LeaderboardsAsyncTask extends AsyncTask<String, Integer, RowSortedTable<Integer, String, String>> {

        private final WeakReference<Activity> mActivity;
        private final WeakReference<Context> mContext;
        private final WeakReference<Spinner> mDropdown;
        private final WeakReference<LeaderboardsAdapter> mAdapter;
        private final WeakReference<RowSortedTable<Integer, String, String>> mTable, mTableFiltered;

        LeaderboardsAsyncTask(Activity activity, Context context, Spinner dropdown, LeaderboardsAdapter adapter, RowSortedTable<Integer, String, String> table, RowSortedTable<Integer, String, String> tableFiltered) {
            this.mActivity = new WeakReference<>(activity);
            this.mContext = new WeakReference<>(context);
            this.mDropdown = new WeakReference<>(dropdown);
            this.mAdapter = new WeakReference<>(adapter);
            this.mTable = new WeakReference<>(table);
            this.mTableFiltered = new WeakReference<>(tableFiltered);
        }

        @Override
        protected RowSortedTable<Integer, String, String> doInBackground(String... strings) {
            final RowSortedTable<Integer, String, String> leaderboards = mTable.get();
            final RowSortedTable<Integer, String, String> tableFiltered = mTableFiltered.get();
            if (leaderboards != null && tableFiltered != null) {
                Document document = Jsoup.parse(strings[0]);
                Elements rows = document.select("div[class=detaillist] > table > tbody > tr");

                for (int i = 1; i < rows.size(); i++) {
                    Element row = rows.get(i);
                    leaderboards.put(i - 1, "ID", row.select("td").get(0).text());
                    leaderboards.put(i - 1, "IMAGE", row.select("td").get(1).selectFirst("img").attr("src"));
                    String attr = row.select("td").get(1).selectFirst("div").attr("onmouseover");
                    leaderboards.put(i - 1, "GAME", attr.substring(attr.indexOf("<b>") + 3, attr.indexOf("</b>")));
                    leaderboards.put(i - 1, "CONSOLE", row.select("td").get(2).text());
                    leaderboards.put(i - 1, "TITLE", row.select("td").get(3).text());
                    leaderboards.put(i - 1, "DESCRIPTION", row.select("td").get(4).text());
                    leaderboards.put(i - 1, "TYPE", row.select("td").get(5).text());
                    leaderboards.put(i - 1, "NUMRESULTS", row.select("td").get(6).text());
                    publishProgress((i * 100) / (rows.size()));
                }
                tableFiltered.putAll(leaderboards);
            }
            return leaderboards;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            final Activity activity = mActivity.get();
            if (activity != null && isActive) {
                ((ProgressBar) activity.findViewById(R.id.leaderboards_progress)).setProgress(values[0]);
            }
        }

        @Override
        protected void onPostExecute(RowSortedTable<Integer, String, String> result) {
            super.onPostExecute(result);
            if (!isActive)
                return;
            final Activity activity = mActivity.get();
            if (activity != null && result.rowKeySet().size() > 0) {
                activity.findViewById(R.id.leaderboards_progress).setVisibility(View.GONE);
            }
            final Context context = mContext.get();
            final Spinner dropdown = mDropdown.get();
            if (context != null && dropdown != null) {
                // Ugly code used to remove dupes, preserve order, and append a blank option to the beginning
                ArrayList<Object> uniqueCols = new ArrayList<>(Arrays.asList(new LinkedHashSet<>(result.column("CONSOLE").values()).toArray()));
                uniqueCols.add(0, "");
                dropdown.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, uniqueCols.toArray()));
            }
            final LeaderboardsAdapter adapter = mAdapter.get();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

}
