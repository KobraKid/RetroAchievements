package com.kobrakid.retroachievements.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.RAAPICallback;
import com.kobrakid.retroachievements.RAAPIConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AchievementDistributionFragment extends Fragment implements RAAPICallback {

    private LineChart achievementDistro = null;
    private SortedMap<Integer, Integer> data;
    private boolean isActive = false, isAPIActive = false;

    public AchievementDistributionFragment() {
    }

    @SuppressLint("UseSparseArrays")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setRetainInstance(true);

        final View view = inflater.inflate(R.layout.view_pager_achievement_distribution, container, false);

        achievementDistro = view.findViewById(R.id.game_details_achievement_distribution);
        achievementDistro.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (isActive) {
                    ((TextView) view.findViewById(R.id.game_details_chart_hints))
                            .setText(getResources().getQuantityString(
                                    R.plurals.achievement_chart_hints,
                                    (int) e.getY(),
                                    (int) e.getY(),
                                    (int) e.getX()));
                }
            }

            @Override
            public void onNothingSelected() {
                if (isActive) {
                    ((TextView) view.findViewById(R.id.game_details_chart_hints)).setText("");
                }
            }
        });

        if (savedInstanceState == null) {
            data = new TreeMap<>();
            isAPIActive = true;
            new RAAPIConnection(getContext()).GetAchievementDistribution(Objects.requireNonNull(getArguments()).getString("GameID"), this);
        } else if (!isAPIActive) {
            populateChartData(view);
        }

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        isActive = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        isActive = true;
    }

    @Override
    public void onStart() {
        super.onStart();
        isActive = true;
    }

    @Override
    public void callback(int responseCode, String response) {
        if (!isActive)
            return;
        if (responseCode == RAAPIConnection.RESPONSE_GET_ACHIEVEMENT_DISTRIBUTION) {
            new AchievementDistributionChartAsyncTask(this, data).execute(response);
        }
        isAPIActive = false;
    }

    private void populateChartData(View view) {
        final Context context = getContext();
        if (context != null) {
            if (data.size() > 0) {
                // Set chart data
                List<Entry> entries = new ArrayList<>();
                for (Integer key : data.keySet()) {
                    entries.add(new Entry(key, data.get(key)));
                }
                LineDataSet dataSet = new LineDataSet(entries, "");
                dataSet.setDrawFilled(true);
                LineData lineData = new LineData(dataSet);
                lineData.setDrawValues(false);

                // Set chart colors
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    TypedValue accentColor = new TypedValue(), primaryColor = new TypedValue();
                    context.getTheme().resolveAttribute(R.attr.colorAccent, accentColor, true);
                    context.getTheme().resolveAttribute(R.attr.colorPrimary, primaryColor, true);
                    achievementDistro.getAxisLeft().setTextColor(primaryColor.data);
                    achievementDistro.getXAxis().setTextColor(primaryColor.data);
                    dataSet.setCircleColor(accentColor.data);
                    dataSet.setColor(accentColor.data);
                    dataSet.setCircleHoleColor(accentColor.data);
                    dataSet.setFillColor(accentColor.data);
                }

                // Set chart axes
                achievementDistro.getAxisRight().setEnabled(false);
                achievementDistro.getLegend().setEnabled(false);
                achievementDistro.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                achievementDistro.getAxisLeft().setAxisMinimum(0f);

                // Set chart description
                Description description = new Description();
                description.setText("");
                achievementDistro.setDescription(description);

                // Set chart finalized data
                achievementDistro.setData(lineData);

                // Redraw chart
                achievementDistro.invalidate();
            }
            view.findViewById(R.id.game_details_achievement_distro_loading).setVisibility(View.GONE);
            achievementDistro.setVisibility(View.VISIBLE);
        }
    }

    private static class AchievementDistributionChartAsyncTask extends AsyncTask<String, Integer, SortedMap<Integer, Integer>> {

        private final WeakReference<AchievementDistributionFragment> fragmentReference;
        private final WeakReference<SortedMap<Integer, Integer>> dataReference;

        AchievementDistributionChartAsyncTask(AchievementDistributionFragment fragment, SortedMap<Integer, Integer> data) {
            this.fragmentReference = new WeakReference<>(fragment);
            this.dataReference = new WeakReference<>(data);
        }

        @Override
        protected SortedMap<Integer, Integer> doInBackground(String... strings) {
            String response = strings[0];
            Document document = Jsoup.parse(response);
            Elements scripts = document.getElementsByTag("script");
            String rows = scripts.get(1).dataNodes().get(0).getWholeData();
            rows = rows.substring(rows.indexOf("dataTotalScore.addRows("));
            rows = rows.substring(0, rows.indexOf(");"));

            Pattern p1 = Pattern.compile("v:(\\d+),");
            Matcher m1 = p1.matcher(rows);
            Pattern p2 = Pattern.compile(",\\s(\\d+)\\s]");
            Matcher m2 = p2.matcher(rows);
            SparseIntArray achievementTotals = new SparseIntArray();
            while (m1.find() && m2.find()) {
                achievementTotals.put(Integer.parseInt(m1.group(1)), Integer.parseInt(m2.group(1)));
            }

            @SuppressLint("UseSparseArrays") SortedMap<Integer, Integer> chartData = new TreeMap<>();
            for (int i = 0; i < achievementTotals.size(); i++) {
                chartData.put(i + 1, achievementTotals.get(i + 1));
            }
            return chartData;
        }

        @Override
        protected void onPostExecute(SortedMap<Integer, Integer> chartData) {
            super.onPostExecute(chartData);
            final AchievementDistributionFragment fragment = fragmentReference.get();
            final SortedMap<Integer, Integer> data = dataReference.get();
            if (fragment != null && data != null) {
                data.putAll(chartData);
                fragment.populateChartData(fragment.getView());
            }
        }
    }
}
