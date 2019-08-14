package com.kobrakid.retroachievements.fragment;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.common.collect.RowSortedTable;
import com.google.common.collect.TreeBasedTable;
import com.kobrakid.retroachievements.MainActivity;
import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.RAAPICallback;
import com.kobrakid.retroachievements.RAAPIConnection;
import com.kobrakid.retroachievements.adapter.LeaderboardsAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.ref.WeakReference;

public class LeaderboardsFragment extends Fragment implements RAAPICallback {

    private RAAPIConnection apiConnection;
    private RecyclerView leaderboardsRecycler;
    private LeaderboardsAdapter adapter;
    private RecyclerView.LayoutManager manager;
    RowSortedTable<Integer, String, String> table;
    private boolean isActive = false;

    public LeaderboardsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Set up API connection
        apiConnection = ((MainActivity) getActivity()).apiConnection;

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_leaderboards, container, false);

        leaderboardsRecycler = view.findViewById(R.id.leaderboards_games);
        table = TreeBasedTable.create();
        adapter = new LeaderboardsAdapter(table);
        leaderboardsRecycler.setAdapter(adapter);
        manager = new LinearLayoutManager(getContext());
        leaderboardsRecycler.setLayoutManager(manager);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        isActive = true;
        apiConnection.GetTopTenUsers(this);
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
            ((TextView) getActivity().findViewById(R.id.leaderboards_users)).setText(response);
        } else if (responseCode == RAAPIConnection.RESPONSE_GET_LEADERBOARDS) {
            ((ProgressBar) getActivity().findViewById(R.id.leaderboards_progress)).setSecondaryProgress(100);
            AsyncTask task = new LeaderboardsAsyncTask(getActivity(), adapter, table).execute(response);
        }
    }

    private static class LeaderboardsAsyncTask extends AsyncTask<String, Integer, RowSortedTable<Integer, String, String>> {

        private WeakReference<Activity> mActivity;
        private WeakReference<LeaderboardsAdapter> mAdapter;
        private WeakReference<RowSortedTable<Integer, String, String>> mTable;

        LeaderboardsAsyncTask(Activity activity, LeaderboardsAdapter adapter, RowSortedTable<Integer, String, String> table) {
            this.mActivity = new WeakReference<>(activity);
            this.mAdapter = new WeakReference<>(adapter);
            this.mTable = new WeakReference<>(table);
        }

        @Override
        protected RowSortedTable<Integer, String, String> doInBackground(String... strings) {
            final RowSortedTable<Integer, String, String> leaderboards = mTable.get();
            if (leaderboards != null) {
                Document document = Jsoup.parse(strings[0]);
                Elements rows = document.select("div[class=detaillist] > table > tbody > tr");

                for (int i = 1; i < rows.size(); i++) {
                    Element row = rows.get(i);
                    leaderboards.put(i - 1, "ID", row.select("td").get(0).text());
                    leaderboards.put(i - 1, "IMAGE", row.select("td").get(1).selectFirst("img").attr("src"));
                    leaderboards.put(i - 1, "GAME", row.select("td").get(1).text());
                    leaderboards.put(i - 1, "CONSOLE", row.select("td").get(2).text());
                    leaderboards.put(i - 1, "TITLE", row.select("td").get(3).text());
                    leaderboards.put(i - 1, "DESCRIPTION", row.select("td").get(4).text());
                    leaderboards.put(i - 1, "TYPE", row.select("td").get(5).text());
                    leaderboards.put(i - 1, "NUMRESULTS", row.select("td").get(6).text());
                    publishProgress((i * 100) / (rows.size()));
                }
            }
            return leaderboards;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            final Activity activity = mActivity.get();
            if (activity != null) {
                ((ProgressBar) activity.findViewById(R.id.leaderboards_progress)).setProgress(values[0]);
            }
        }

        @Override
        protected void onPostExecute(RowSortedTable<Integer, String, String> result) {
            super.onPostExecute(result);
            final Activity activity = mActivity.get();
            if (activity != null) {
                activity.findViewById(R.id.leaderboards_progress).setVisibility(View.GONE);
            }
            final LeaderboardsAdapter adapter = mAdapter.get();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

}
