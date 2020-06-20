package com.kobrakid.retroachievements.adapter;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.kobrakid.retroachievements.Consts;
import com.kobrakid.retroachievements.GameDetailsActivity;
import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.fragment.AchievementDetailsFragment;
import com.kobrakid.retroachievements.fragment.AchievementSummaryFragment;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {

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
    private final ArrayList<Boolean> hardcoreEarnings;
    private double numDistinctCasual = 1;

    private final Fragment fragment;
    private final AchievementViewHolderListener viewHolderListener;

    public AchievementAdapter(Fragment fragment) {
        this.fragment = fragment;
        ids = new ArrayList<>();
        badges = new ArrayList<>();
        titles = new ArrayList<>();
        points = new ArrayList<>();
        trueRatios = new ArrayList<>();
        descriptions = new ArrayList<>();
        datesEarned = new ArrayList<>();
        numsAwarded = new ArrayList<>();
        numsAwardedHC = new ArrayList<>();
        authors = new ArrayList<>();
        datesCreated = new ArrayList<>();
        datesModified = new ArrayList<>();
        hardcoreEarnings = new ArrayList<>();
        viewHolderListener = new AchievementViewHolderListenerImpl(fragment, this);

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
        ((TextView) holder.linearLayout.findViewById(R.id.recycler_view_position)).setText(String.valueOf(position));
        ((TextView) holder.linearLayout.findViewById(R.id.achievement_summary_author)).setText(authors.get(position));
        ((TextView) holder.linearLayout.findViewById(R.id.achievement_summary_created)).setText(datesCreated.get(position));
        ((TextView) holder.linearLayout.findViewById(R.id.achievement_summary_modified)).setText(datesModified.get(position));

        // Badge
        if (hardcoreEarnings.get(position)) {
            (holder.linearLayout.findViewById(R.id.achievement_summary_badge)).setBackground(Objects.requireNonNull(fragment.getContext()).getDrawable(R.drawable.image_view_border));
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
                .setText(fragment.getString(R.string.achievement_summary_title, titles.get(position), points.get(position), trueRatios.get(position)));
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
                    .setText(Objects.requireNonNull(fragment.getContext()).getString(R.string.date_earned, datesEarned.get(position)));
        }
        ((TextView) holder.linearLayout.findViewById(R.id.achievement_summary_stats))
                .setText(Objects.requireNonNull(fragment.getContext()).getString(R.string.won_by,
                        numsAwarded.get(position),
                        numsAwardedHC.get(position),
                        (int) numDistinctCasual,
                        new DecimalFormat("@@@@")
                                .format(Double.parseDouble(numsAwarded.get(position)) / numDistinctCasual * 100.0)));

        // Double-layered Progress Bar
        ProgressBar progressBar = holder.linearLayout.findViewById(R.id.achievement_summary_progress);
        progressBar.setProgress((int) (Double.parseDouble(numsAwardedHC.get(position)) / numDistinctCasual * 10000.0));
        progressBar.setSecondaryProgress((int) (Double.parseDouble(numsAwarded.get(position)) / numDistinctCasual * 10000.0));
    }

    @Override
    public int getItemCount() {
        return ids.size();
    }

    public void addAchievement(
            int index,
            String id,
            String badge,
            String title,
            String point,
            String trueRatio,
            String description,
            String dateEarned,
            boolean earnedHardcore,
            String numAwarded,
            String numAwardedHC,
            String author,
            String dateCreated,
            String dateModified) {
        ids.add(index, id);
        badges.add(index, badge);
        titles.add(index, title);
        points.add(index, point);
        trueRatios.add(index, trueRatio);
        descriptions.add(index, description);
        datesEarned.add(index, dateEarned);
        hardcoreEarnings.add(index, earnedHardcore);
        numsAwarded.add(index, numAwarded);
        numsAwardedHC.add(index, numAwardedHC);
        authors.add(index, author);
        datesCreated.add(index, dateCreated);
        datesModified.add(index, dateModified);
        notifyItemInserted(index);
    }

    public void setNumDistinctCasual(double n) {
        numDistinctCasual = n;
    }

    public void clear() {
        ids.clear();
        badges.clear();
        titles.clear();
        points.clear();
        trueRatios.clear();
        descriptions.clear();
        hardcoreEarnings.clear();
        datesEarned.clear();
        numsAwarded.clear();
        numsAwardedHC.clear();
        authors.clear();
        datesCreated.clear();
        datesModified.clear();
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
        public void onItemClicked(View view, int adapterPosition) {// Create a new transition
            TransitionSet transitionSet = new TransitionSet();
            transitionSet.setOrdering(TransitionSet.ORDERING_TOGETHER);
            // Get the adapter position of the first child
            int firstChildIndex = Integer.parseInt(
                    ((TextView) Objects.requireNonNull(((AchievementSummaryFragment) fragment)
                            .layoutManager
                            .getChildAt(0))
                            .findViewById(R.id.recycler_view_position))
                            .getText()
                            .toString());
            // FIXME Animation no longer occurring 😢 (but was already pretty janky)
            // Custom logic to slide higher achievements up, lower ones down
            for (int i = 0; i < adapter.getItemCount(); i++) {
                Slide slide = new Slide();
                slide.setDuration(Objects.requireNonNull(fragment.getActivity()).getResources().getInteger(R.integer.animation_duration));
                slide.addTarget(((AchievementSummaryFragment) fragment).layoutManager.getChildAt(i));
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
            bundle.putDouble("NumDistinctPlayersCasual", adapter.numDistinctCasual);
            detailsFragment.setArguments(bundle);
            Objects.requireNonNull(fragment
                    .getActivity())
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .addSharedElement(transitionBadge, transitionBadge.getTransitionName())
                    .replace(R.id.game_details_frame, detailsFragment, AchievementDetailsFragment.class.getSimpleName())
                    .addToBackStack(null)
                    .commit();
        }
    }

    public static class AchievementViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

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