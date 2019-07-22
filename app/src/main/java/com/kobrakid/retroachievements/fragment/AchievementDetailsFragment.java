package com.kobrakid.retroachievements.fragment;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kobrakid.retroachievements.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;


public class AchievementDetailsFragment extends Fragment implements View.OnClickListener {

    public AchievementDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_achievement_details, container, false);
        view.setOnClickListener(this);
        view.setTransitionName("achievement_" + getArguments().getString("Position"));

        // Set fields from transferred data
        ((TextView) view.findViewById(R.id.achievement_details_title)).setText(getArguments().getString("Title"));
        ((TextView) view.findViewById(R.id.achievement_details_description)).setText(getArguments().getString("Description"));
        if (getArguments().getString("DateEarned").startsWith("NoDate")) {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            ((ImageView) view.findViewById(R.id.achievement_details_badge)).setColorFilter(new ColorMatrixColorFilter(matrix));
//            view.findViewById(R.id.achievement_details_date).setVisibility(View.GONE);
        } else {
            ((ImageView) view.findViewById(R.id.achievement_details_badge)).clearColorFilter();
            ((TextView) view.findViewById(R.id.achievement_details_date))
                    .setText(getContext().getString(R.string.date_earned, getArguments().getString("DateEarned")));
        }
        ((TextView) view.findViewById(R.id.achievement_details_completion_text))
                .setText(getContext().getString(
                        R.string.earned_by_details,
                        getArguments().getString("NumAwarded"),
                        getArguments().getString("NumDistinctPlayersCasual"),
                        new DecimalFormat("@@@@")
                                .format(Double.parseDouble(getArguments().getString("NumAwarded")) / Double.parseDouble(getArguments().getString("NumDistinctPlayersCasual")) * 100.0)));
        ((TextView) view.findViewById(R.id.achievement_details_completion_hardcore_text))
                .setText(getContext().getString(
                        R.string.earned_by_hc_details,
                        getArguments().getString("NumAwardedHardcore"),
                        new DecimalFormat("@@@@")
                                .format(Double.parseDouble(getArguments().getString("NumAwardedHardcore")) / Double.parseDouble(getArguments().getString("NumDistinctPlayersCasual")) * 100.0)));
        ((TextView) view.findViewById(R.id.achievement_details_metadata))
                .setText(getString(R.string.metadata,
                        getArguments().getString("Author"),
                        getArguments().getString("DateCreated"),
                        getArguments().getString("DateModified")));
        ProgressBar progressBar = view.findViewById(R.id.achievement_details_completion_hardcore);
        progressBar.setProgress((int) (Double.parseDouble(getArguments().getString("NumAwardedHardcore")) / Double.parseDouble(getArguments().getString("NumDistinctPlayersCasual")) * 10000.0));
        progressBar = view.findViewById(R.id.achievement_details_completion);
        progressBar.setProgress((int) (Double.parseDouble(getArguments().getString("NumAwarded")) / Double.parseDouble(getArguments().getString("NumDistinctPlayersCasual")) * 10000.0));


        postponeEnterTransition();

        final ImageView badge = view.findViewById(R.id.achievement_details_badge);
        Picasso.get()
                .load("http://retroachievements.org/Badge/" + getArguments().getString("ImageIcon") + ".png")
                .into(badge, new Callback() {
                    @Override
                    public void onSuccess() {
                        prepareSharedElementTransition(view);
                        startPostponedEnterTransition();
                    }

                    @Override
                    public void onError(Exception e) {
                        prepareSharedElementTransition(view);
                        startPostponedEnterTransition();
                    }
                });

        return view;
    }

    private void prepareSharedElementTransition(final View view) {
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

    @Override
    public void onClick(View view) {
        this.getFragmentManager().popBackStack();
    }

}
