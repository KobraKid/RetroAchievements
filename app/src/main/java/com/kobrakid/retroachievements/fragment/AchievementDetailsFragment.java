package com.kobrakid.retroachievements.fragment;

import android.annotation.SuppressLint;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.kobrakid.retroachievements.Consts;
import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.viewpager.ToggleableViewPager;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.Objects;

/**
 * This class is responsible for showing more detailed information on a particular achievement.
 */
public class AchievementDetailsFragment extends Fragment {

    private GestureDetector tapDetector;

    public AchievementDetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_achievement_details, container, false);
        view.setTransitionName("achievement_" + Objects.requireNonNull(getArguments()).getString("Position"));

        // Set fields from transferred data
        ((TextView) view.findViewById(R.id.achievement_details_title)).setText(getArguments().getString("Title"));
        ((TextView) view.findViewById(R.id.achievement_details_description)).setText(getArguments().getString("Description"));
        if (!Objects.requireNonNull(getArguments().getString("DateEarned")).startsWith("NoDate")) {
            ((TextView) view.findViewById(R.id.achievement_details_date))
                    .setText(Objects.requireNonNull(getContext()).getString(R.string.date_earned, getArguments().getString("DateEarned")));
        }
        ((TextView) view.findViewById(R.id.achievement_details_completion_text))
                .setText(Objects.requireNonNull(getContext()).getString(
                        R.string.earned_by_details,
                        getArguments().getString("NumAwarded"),
                        (int) getArguments().getDouble("NumDistinctPlayersCasual"),
                        new DecimalFormat("@@@@")
                                .format(Double.parseDouble(Objects.requireNonNull(getArguments().getString("NumAwarded")))
                                        / getArguments().getDouble("NumDistinctPlayersCasual") * 100.0)));
        ((TextView) view.findViewById(R.id.achievement_details_completion_hardcore_text))
                .setText(getContext().getString(
                        R.string.earned_by_hc_details,
                        getArguments().getString("NumAwardedHardcore"),
                        new DecimalFormat("@@@@")
                                .format(Double.parseDouble(Objects.requireNonNull(getArguments().getString("NumAwardedHardcore")))
                                        / getArguments().getDouble("NumDistinctPlayersCasual") * 100.0)));
        ((TextView) view.findViewById(R.id.achievement_details_metadata))
                .setText(getString(R.string.metadata,
                        getArguments().getString("Author"),
                        getArguments().getString("DateCreated"),
                        getArguments().getString("DateModified")));

        ProgressBar progressBar = view.findViewById(R.id.achievement_details_completion_hardcore);
        progressBar.setProgress((int) (Double.parseDouble(Objects.requireNonNull(getArguments().getString("NumAwardedHardcore")))
                / getArguments().getDouble("NumDistinctPlayersCasual") * 10000.0));

        progressBar = view.findViewById(R.id.achievement_details_completion);
        progressBar.setProgress((int) (Double.parseDouble(Objects.requireNonNull(getArguments().getString("NumAwarded")))
                / getArguments().getDouble("NumDistinctPlayersCasual") * 10000.0));

//        postponeEnterTransition();

        final ImageView badge = view.findViewById(R.id.achievement_details_badge);
        Picasso.get()
                .load(Consts.BASE_URL + "/" + Consts.GAME_BADGE_POSTFIX + "/" + getArguments().getString("ImageIcon") + ".png")
                .placeholder(getResources().getDrawable(R.drawable.favicon, Objects.requireNonNull(getActivity()).getTheme()))
                .into(badge, new Callback() {
                    @Override
                    public void onSuccess() {
                        if (Objects.requireNonNull(Objects.requireNonNull(getArguments()).getString("DateEarned")).startsWith("NoDate")) {
                            ColorMatrix matrix = new ColorMatrix();
                            matrix.setSaturation(0);
                            ((ImageView) view.findViewById(R.id.achievement_details_badge)).setColorFilter(new ColorMatrixColorFilter(matrix));
                        } else {
                            ((ImageView) view.findViewById(R.id.achievement_details_badge)).clearColorFilter();
                        }
                        prepareSharedElementTransition(view);
//                        startPostponedEnterTransition();
                    }

                    @Override
                    public void onError(Exception e) {
                        prepareSharedElementTransition(view);
//                        startPostponedEnterTransition();
                    }
                });

        tapDetector = new GestureDetector(getContext(), new GestureTap());
        view.setOnTouchListener((v, e) -> {
            tapDetector.onTouchEvent(e);
            return true;
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((ToggleableViewPager) Objects.requireNonNull(getActivity()).findViewById(R.id.game_details_view_pager)).setPagingEnabled(false);
    }

    @Override
    public void onStop() {
        super.onStop();
        ((ToggleableViewPager) Objects.requireNonNull(getActivity()).findViewById(R.id.game_details_view_pager)).setPagingEnabled(true);
    }

    private void prepareSharedElementTransition(@SuppressWarnings("unused") final View view) {
        // TODO Figure out why transitions (and/or recycler views) are so awful and hard to work with
//        Transition transition = TransitionInflater.from(getContext()).inflateTransition(R.transition.image_shared_element_transition);
//        setSharedElementEnterTransition(transition);
//        setEnterSharedElementCallback(new SharedElementCallback() {
//            @Override
//            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
//                sharedElements.put(names.get(0), view);
//            }
//        });
    }

    private class GestureTap extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Objects.requireNonNull(getFragmentManager()).popBackStack();
            return true;
        }
    }

}
