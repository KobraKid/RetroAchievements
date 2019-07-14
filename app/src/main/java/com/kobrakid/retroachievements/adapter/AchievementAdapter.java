package com.kobrakid.retroachievements.adapter;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kobrakid.retroachievements.R;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {

    private Context context;

    private String gameID;
    private String forumTopicID;
    public String numDistinctCasual;
    private ArrayList<String>
            ids,
            badges,
            titles,
            points,
            trueRatios,
            descriptions,
            datesEarned,
            numsAwarded,
            numsAwardedHC;

    class AchievementViewHolder extends RecyclerView.ViewHolder {

        LinearLayout linearLayout;

        AchievementViewHolder(LinearLayout l) {
            super(l);
            linearLayout = l;
        }
    }

    public AchievementAdapter(Context context,
                              ArrayList<String> ids,
                              ArrayList<String> badges,
                              ArrayList<String> titles,
                              ArrayList<String> points,
                              ArrayList<String> trueRatios,
                              ArrayList<String> descriptions,
                              ArrayList<String> datesEarned,
                              ArrayList<String> numsAwarded,
                              ArrayList<String> numsAwardedHC,
                              String numDistinctCasual) {
        this.context = context;
        this.ids = ids;
        this.badges = badges;
        this.titles = titles;
        this.points = points;
        this.trueRatios = trueRatios;
        this.descriptions = descriptions;
        this.datesEarned = datesEarned;
        this.numsAwarded = numsAwarded;
        this.numsAwardedHC = numsAwardedHC;
        this.numDistinctCasual = numDistinctCasual;

    }

    @NonNull
    @Override
    public AchievementAdapter.AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.achievement_summary,
                        parent,
                        false);
        return new AchievementViewHolder(linearLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        // Badge
        Picasso.get()
                .load("https://retroachievements.org/Badge/" + badges.get(position) + ".png")
                .into((ImageView) holder.linearLayout.findViewById(R.id.achievement_summary_badge));

        // Text descriptions
        ((TextView) holder.linearLayout.findViewById(R.id.achievement_summary_id))
                .setText(ids.get(position));
        ((TextView) holder.linearLayout.findViewById(R.id.achievement_summary_title))
                .setText(titles.get(position) + " (" + points.get(position) + ") (" + trueRatios.get(position) + ")");
        ((TextView) holder.linearLayout.findViewById(R.id.achievement_summary_desc))
                .setText(descriptions.get(position));
        if (datesEarned.get(position).startsWith("NoDate")) {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            ((ImageView) holder.linearLayout.findViewById(R.id.achievement_summary_badge)).setColorFilter(new ColorMatrixColorFilter(matrix));
            ((TextView) holder.linearLayout.findViewById(R.id.achievement_summary_date))
                    .setText("");
        } else {
            ((ImageView) holder.linearLayout.findViewById(R.id.achievement_summary_badge)).clearColorFilter();
            ((TextView) holder.linearLayout.findViewById(R.id.achievement_summary_date))
                    .setText(context.getString(R.string.date_earned, datesEarned.get(position)));
        }
        ((TextView) holder.linearLayout.findViewById(R.id.achievement_summary_stats))
                .setText(context.getString(R.string.won_by,
                        numsAwarded.get(position),
                        numsAwardedHC.get(position),
                        numDistinctCasual,
                        new DecimalFormat("@@@@")
                                .format(Double.parseDouble(numsAwarded.get(position)) / Double.parseDouble(numDistinctCasual) * 100.0)));

        // Double-layered Progress Bar
        ProgressBar progressBar = holder.linearLayout.findViewById(R.id.achievement_summary_progress);
        progressBar.setProgress((int) (Double.parseDouble(numsAwardedHC.get(position)) / Double.parseDouble(numDistinctCasual) * 10000.0));
        progressBar.setSecondaryProgress((int) (Double.parseDouble(numsAwarded.get(position)) / Double.parseDouble(numDistinctCasual) * 10000.0));
    }

    @Override
    public int getItemCount() {
        return ids.size();
    }

}