package com.kobrakid.retroachievements.adapter;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.transition.TransitionSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kobrakid.retroachievements.Consts;
import com.kobrakid.retroachievements.GameDetailsActivity;
import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.fragment.AchievementDetailsFragment;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {

    public String numDistinctCasual;
    private final ArrayList<String> ids;
    private final ArrayList<String> badges;
    private final ArrayList<String> titles;
    private final ArrayList<String> points;
    private final ArrayList<String> trueRatios;
    private final ArrayList<String> descriptions;
    private final ArrayList<String> datesEarned;
    private final ArrayList<String> numsAwarded;
    private final ArrayList<String> numsAwardedHC;
    private final ArrayList<String> authors;
    private final ArrayList<String> datesCreated;
    private final ArrayList<String> datesModified;
    private final Map<String, Boolean> hardcoreEarnings;

    private Fragment fragment;
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
                              ArrayList<String> authors,
                              ArrayList<String> datesCreated,
                              ArrayList<String> datesModified,
                              Map<String, Boolean> hardcoreEarnings,
                              String numDistinctCasual) {
        this.fragment = fragment;
        this.ids = ids;
        this.badges = badges;
        this.titles = titles;
        this.points = points;
        this.trueRatios = trueRatios;
        this.descriptions = descriptions;
        this.datesEarned = datesEarned;
        this.numsAwarded = numsAwarded;
        this.numsAwardedHC = numsAwardedHC;
        this.authors = authors;
        this.datesCreated = datesCreated;
        this.datesModified = datesModified;
        this.hardcoreEarnings = hardcoreEarnings;
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

        // Hidden Text Views
        ((TextView) holder.linearLayout.findViewById(R.id.recycler_view_position)).setText("" + position);
        ((TextView) holder.linearLayout.findViewById(R.id.achievement_summary_author)).setText(authors.get(position));
        ((TextView) holder.linearLayout.findViewById(R.id.achievement_summary_created)).setText(datesCreated.get(position));
        ((TextView) holder.linearLayout.findViewById(R.id.achievement_summary_modified)).setText(datesModified.get(position));

        // Badge
        if (hardcoreEarnings.containsKey(ids.get(position)) && hardcoreEarnings.get(ids.get(position))) {
            (holder.linearLayout.findViewById(R.id.achievement_summary_badge)).setBackground(fragment.getContext().getDrawable(R.drawable.image_view_border));
        } else {
            (holder.linearLayout.findViewById(R.id.achievement_summary_badge)).setBackground(null);
        }
        Picasso.get()
                .load(Consts.BASE_URL + "/" + Consts.GAME_BADGE_POSTFIX + "/" + badges.get(position) + ".png")
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
                    .setText(fragment.getContext().getString(R.string.date_earned, datesEarned.get(position)));
        }
        ((TextView) holder.linearLayout.findViewById(R.id.achievement_summary_stats))
                .setText(fragment.getContext().getString(R.string.won_by,
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

    /* Inner Classes and Interfaces */

    private interface AchievementViewHolderListener {
        void onItemClicked(View view, int adapterPosition);
    }

    private static class AchievementViewHolderListenerImpl implements AchievementViewHolderListener {

        private final Fragment fragment;
        private final AchievementAdapter adapter;

        AchievementViewHolderListenerImpl(Fragment fragment, AchievementAdapter adapter) {
            this.fragment = fragment;
            this.adapter = adapter;
        }

        @Override
        public void onItemClicked(View view, int adapterPosition) {
            GameDetailsActivity.currentPosition = adapterPosition;
            GameDetailsActivity context = ((GameDetailsActivity) fragment.getContext());

            // Create a new transition
            TransitionSet transitionSet = new TransitionSet();
            transitionSet.setOrdering(TransitionSet.ORDERING_TOGETHER);
            // Get the adapter position of the first child
            int firstChildIndex = Integer.parseInt(
                    ((TextView) context
                            .layoutManager
                            .getChildAt(0)
                            .findViewById(R.id.recycler_view_position))
                            .getText()
                            .toString());
            // FIXME Animation no longer occurring 😢 (but was already pretty janky)
            // Custom logic to slide higher achievements up, lower ones down
            for (int i = 0; i < adapter.getItemCount(); i++) {
                Slide slide = new Slide();
                slide.setDuration(fragment.getActivity().getResources().getInteger(R.integer.animation_duration));
                slide.addTarget(context.layoutManager.getChildAt(i));
                if (i + firstChildIndex < adapterPosition) {
                    slide.setSlideEdge(Gravity.TOP);
                    transitionSet.addTransition(slide);
                } else if (i + firstChildIndex > adapterPosition) {
                    slide.setSlideEdge(Gravity.BOTTOM);
                    transitionSet.addTransition(slide);
                }
            }
            fragment.setExitTransition(transitionSet);
            // Set up the target fragment
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
            bundle.putString("Author", adapter.authors.get(adapterPosition));
            bundle.putString("DateCreated", adapter.datesCreated.get(adapterPosition));
            bundle.putString("DateModified", adapter.datesModified.get(adapterPosition));
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