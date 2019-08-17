package com.kobrakid.retroachievements.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.common.collect.RowSortedTable;
import com.google.common.collect.TreeBasedTable;
import com.kobrakid.retroachievements.MainActivity;
import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.RAAPICallback;
import com.kobrakid.retroachievements.RAAPIConnection;
import com.kobrakid.retroachievements.adapter.LeaderboardsAdapter;

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

public class LeaderboardsFragment extends Fragment implements RAAPICallback {

    private RAAPIConnection apiConnection;
    private RecyclerView leaderboardsRecycler;
    private LeaderboardsAdapter adapter;
    private RecyclerView.LayoutManager manager;
    private RowSortedTable<Integer, String, String> table, tableFiltered;
    private boolean isActive = false;

    private Spinner consoleDropdown;
    private EditText leaderboardsFilter;
    private String filteredConsole = "", filteredTitle = "";

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

        // Set up Leaderboards List
        leaderboardsRecycler = view.findViewById(R.id.leaderboards_games);
        table = TreeBasedTable.create();
        tableFiltered = TreeBasedTable.create();
        adapter = new LeaderboardsAdapter(table, tableFiltered);
        leaderboardsRecycler.setAdapter(adapter);
        manager = new LinearLayoutManager(getContext());
        leaderboardsRecycler.setLayoutManager(manager);

        // Set up Filters
        consoleDropdown = view.findViewById(R.id.leaderboards_console_filter);
        leaderboardsFilter = view.findViewById(R.id.leaderboards_filter);
        consoleDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                filteredConsole = adapterView.getItemAtPosition(pos).toString();
                adapter.getFilter().filter(filteredConsole + "\t" + filteredTitle);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                filteredConsole = "";
                adapter.getFilter().filter(filteredConsole + "\t" + filteredTitle);
            }
        });
        leaderboardsFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                filteredTitle = charSequence.toString();
                adapter.getFilter().filter(filteredConsole + "\t" + filteredTitle);
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
            try {
                JSONArray reader = new JSONArray(response);

                TableLayout tableLayout = getActivity().findViewById(R.id.leaderboards_users);
                TableRow row, row1 = new TableRow(getContext()), row2 = new TableRow(getContext()), row3 = new TableRow(getContext()), row4 = new TableRow(getContext()), row5 = new TableRow(getContext());
                LinearLayout outerLayout, innerLayout;
                TextView username, score, ratio;
                for (int i = 0; i < reader.length(); i++) {
                    switch (i % 5) {
                        case 1:
                            row = row2;
                            break;
                        case 2:
                            row = row3;
                            break;
                        case 3:
                            row = row4;
                            break;
                        case 4:
                            row = row5;
                            break;
                        case 0:
                        default:
                            row = row1;
                            break;
                    }

                    outerLayout = new LinearLayout(getContext());
                    outerLayout.setOrientation(LinearLayout.HORIZONTAL);
                    LinearLayout.LayoutParams params = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f);
                    ((TableRow.LayoutParams) params).gravity = Gravity.START;
                    ((TableRow.LayoutParams) params).column = (i % 2) + 1;
                    if (i > 4)
                        params.setMarginStart(8);
                    outerLayout.setLayoutParams(params);

                    innerLayout = new LinearLayout(getContext());
                    innerLayout.setOrientation(LinearLayout.VERTICAL);
                    innerLayout.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.25f));

                    username = new TextView(getContext());
                    username.setText((i + 1) + ". " + ((JSONObject) reader.get(i)).getString("1"));
                    username.setTextSize(14);
                    username.setSingleLine(true);
                    params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.75f);
                    params.gravity = Gravity.CENTER_VERTICAL;
                    username.setLayoutParams(params);

                    params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.gravity = Gravity.END;

                    score = new TextView(getContext());
                    score.setText(((JSONObject) reader.get(i)).getString("2"));
                    score.setTextSize(12);
                    score.setLayoutParams(params);

                    ratio = new TextView(getContext());
                    ratio.setText(((JSONObject) reader.get(i)).getString("3"));
                    ratio.setTextSize(12);
                    ratio.setLayoutParams(params);

                    innerLayout.addView(score);
                    innerLayout.addView(ratio);
                    outerLayout.addView(username);
                    outerLayout.addView(innerLayout);
                    row.addView(outerLayout);
                }
                tableLayout.addView(row1);
                tableLayout.addView(row2);
                tableLayout.addView(row3);
                tableLayout.addView(row4);
                tableLayout.addView(row5);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (responseCode == RAAPIConnection.RESPONSE_GET_LEADERBOARDS) {
            ((ProgressBar) getActivity().findViewById(R.id.leaderboards_progress)).setSecondaryProgress(100);
            AsyncTask task = new LeaderboardsAsyncTask(getActivity(), getContext(), consoleDropdown, adapter, table, tableFiltered).execute(response);
        }
    }

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
                    leaderboards.put(i - 1, "GAME", row.select("td").get(1).text());
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
