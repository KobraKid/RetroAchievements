package com.kobrakid.retroachievements.fragment;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.transition.Transition;
import android.transition.TransitionInflater;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AchievementDetailsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AchievementDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AchievementDetailsFragment extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;

    public AchievementDetailsFragment() {
        // Required empty public constructor
    }

    public static AchievementDetailsFragment newInstance() {
        return new AchievementDetailsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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
            ((TextView) view.findViewById(R.id.achievement_details_date))
                    .setText(getContext().getString(R.string.date_earned, getArguments().getString("DateEarned")));
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
                        R.string.earned_by_details,
                        getArguments().getString("NumAwardedHardcore"),
                        getArguments().getString("NumDistinctPlayersCasual"),
                        new DecimalFormat("@@@@")
                                .format(Double.parseDouble(getArguments().getString("NumAwardedHardcore")) / Double.parseDouble(getArguments().getString("NumDistinctPlayersCasual")) * 100.0)));
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
        Transition transition = TransitionInflater.from(getContext()).inflateTransition(R.transition.image_shared_element_transition);
        setSharedElementEnterTransition(transition);
        // TODO Figure out why transitions (and/or recycler views) are so awful and hard to work with
//        setEnterSharedElementCallback(new SharedElementCallback() {
//            @Override
//            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
//                sharedElements.put(names.get(0), view);
//            }
//        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        this.getFragmentManager().popBackStack();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
