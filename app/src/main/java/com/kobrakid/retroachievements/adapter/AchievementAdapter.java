package com.kobrakid.retroachievements.adapter;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kobrakid.retroachievements.GameDetailsActivity;
import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.fragment.AchievementDetailsFragment;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {

    private Context context;

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

    private final AchievementViewHolderListener viewHolderListener;

    public AchievementAdapter(Fragment fragment,
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
        this.context = fragment.getContext();
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
        this.viewHolderListener = new AchievementViewHolderListenerImpl(fragment, this);

    }

    @NonNull
    @Override
    public AchievementAdapter.AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.view_holder_achievement_summary,
                        parent,
                        false);
        return new AchievementViewHolder(linearLayout, viewHolderListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {

        // Badge
        Picasso.get()
                .load("https://retroachievements.org/Badge/" + badges.get(position) + ".png")
                .into((ImageView) holder.linearLayout.findViewById(R.id.achievement_summary_badge));
        holder.linearLayout.findViewById(R.id.achievement_summary_badge).setTransitionName("achievement_" + position);
        ((TextView) holder.linearLayout.findViewById(R.id.achievement_summary_badge_id))
                .setText(badges.get(position));

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

    /****************************
     Inner Classes and Interfaces
     ****************************/

    private interface AchievementViewHolderListener {
        void onItemClicked(View view, int adapterPosition);
    }

    private static class AchievementViewHolderListenerImpl implements AchievementViewHolderListener {

        private Fragment fragment;
        private AchievementAdapter adapter;

        AchievementViewHolderListenerImpl(Fragment fragment, AchievementAdapter adapter) {
            this.fragment = fragment;
            this.adapter = adapter;
        }

        @Override
        public void onItemClicked(View view, int adapterPosition) {
            GameDetailsActivity.currentPosition = adapterPosition;

//            ((TransitionSet) fragment.getExitTransition()).excludeTarget(view, true);

            ImageView transitionBadge = view.findViewById(R.id.achievement_summary_badge);
            Fragment detailsFragment = new AchievementDetailsFragment();
            Bundle bundle = new Bundle();
            bundle.putString("Position", "" + adapterPosition);
            bundle.putString("GameID", adapter.ids.get(adapterPosition));
            bundle.putString("ImageIcon", adapter.badges.get(adapterPosition));
            bundle.putString("Title", adapter.titles.get(adapterPosition));
            bundle.putString("Points", adapter.points.get(adapterPosition));
            bundle.putString("TrueRatio", adapter.trueRatios.get(adapterPosition));
            bundle.putString("Description", adapter.descriptions.get(adapterPosition));
            bundle.putString("DateEarned", adapter.datesEarned.get(adapterPosition));
            bundle.putString("NumAwarded", adapter.numsAwarded.get(adapterPosition));
            bundle.putString("NumAwardedHardcore", adapter.numsAwardedHC.get(adapterPosition));
            bundle.putString("NumDistinctPlayersCasual", adapter.numDistinctCasual);
            detailsFragment.setArguments(bundle);
            fragment
                    .getActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .addSharedElement(transitionBadge, transitionBadge.getTransitionName())
                    .replace(R.id.game_details_frame, detailsFragment, AchievementDetailsFragment.class.getSimpleName())
                    .addToBackStack(null)
                    .commit();
        }
    }

    public class AchievementViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final LinearLayout linearLayout;
        private final AchievementViewHolderListener viewHolderListener;

        AchievementViewHolder(LinearLayout linearLayout, AchievementViewHolderListener viewHolderListener) {
            super(linearLayout);
            this.linearLayout = linearLayout;
            this.viewHolderListener = viewHolderListener;
            linearLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            viewHolderListener.onItemClicked(view, getAdapterPosition());
        }
    }

}