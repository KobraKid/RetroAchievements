package com.kobrakid.retroachievements.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AchievementDistributionFragment extends Fragment implements RAAPICallback {

    private ProgressBar achievementDistroLoadingBar = null;
    private LineChart achievementDistro = null;
    private boolean isActive = false;

    public AchievementDistributionFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.view_pager_achievement_distribution, container, false);

        achievementDistroLoadingBar = view.findViewById(R.id.game_details_achievement_distro_loading);
        achievementDistro = view.findViewById(R.id.game_details_achievement_distribution);
        new RAAPIConnection(getContext()).GetAchievementDistribution(Objects.requireNonNull(getArguments()).getString("GameID"), this);
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
            new AchievementDistributionChartAsyncTask(getContext(), achievementDistro, achievementDistroLoadingBar).execute(response);
        }
    }

    private static class AchievementDistributionChartAsyncTask extends AsyncTask<String, Integer, Integer[][]> {

        private final WeakReference<Context> contextReference;
        private final WeakReference<LineChart> lineChartReference;
        private final WeakReference<View> loadingBarReference;

        AchievementDistributionChartAsyncTask(Context context, LineChart lineChart, View frameLayout) {
            this.contextReference = new WeakReference<>(context);
            this.lineChartReference = new WeakReference<>(lineChart);
            this.loadingBarReference = new WeakReference<>(frameLayout);
        }

        @Override
        protected Integer[][] doInBackground(String... strings) {
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

            // Initialize arrays
            Integer[] achievementCount = new Integer[achievementTotals.size()];
            Integer[] userCount = new Integer[achievementTotals.size()];
            for (int i = 0; i < achievementTotals.size(); i++) {
                achievementCount[i] = i + 1;
                userCount[i] = achievementTotals.get(i + 1);
            }

            return new Integer[][]{achievementCount, userCount};
        }

        @Override
        protected void onPostExecute(Integer[][] chartData) {
            super.onPostExecute(chartData);

            final Context context = contextReference.get();
            final LineChart chart = lineChartReference.get();
            if (context != null && chart != null) {
                if (chartData[0].length > 0) {
                    // Set chart data
                    List<Entry> entries = new ArrayList<>();
                    for (int i = 0; i < chartData[0].length; i++) {
                        entries.add(new Entry(chartData[0][i], chartData[1][i]));
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
                        chart.getAxisLeft().setTextColor(primaryColor.data);
                        chart.getXAxis().setTextColor(primaryColor.data);
                        dataSet.setCircleColor(accentColor.data);
                        dataSet.setColor(accentColor.data);
                        dataSet.setCircleHoleColor(accentColor.data);
                        dataSet.setFillColor(accentColor.data);
                    }

                    // Set chart axes
                    chart.getAxisRight().setEnabled(false);
                    chart.getLegend().setEnabled(false);
                    chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                    chart.getAxisLeft().setAxisMinimum(0f);
                    chart.setClickable(false);

                    // Set chart description
                    Description description = new Description();
                    description.setText("");
                    chart.setDescription(description);

                    // Set chart finalized data
                    chart.setData(lineData);

                    // Redraw chart
                    chart.invalidate();
                }
                chart.setVisibility(View.VISIBLE);
            }
            final View view = loadingBarReference.get();
            if (view != null)
                view.setVisibility(View.GONE);
        }
    }
}
