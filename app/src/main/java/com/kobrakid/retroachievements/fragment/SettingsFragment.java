package com.kobrakid.retroachievements.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.kobrakid.retroachievements.Consts;
import com.kobrakid.retroachievements.R;

import java.util.Arrays;

public class SettingsFragment extends Fragment {

    // Unused, but guarantees that the parent Activity implements OnFragmentInteractionListener
    private OnFragmentInteractionListener listener;
    private SharedPreferences sharedPref;
    private String theme = "";

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initialize preferences object
        sharedPref = getActivity().getSharedPreferences(getString(R.string.login_key), Context.MODE_PRIVATE);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Set up views
        this.theme = sharedPref.getString(getString(R.string.theme_setting), "");
        ((TextView) view.findViewById(R.id.settings_current_theme)).setText(getString(R.string.settings_current_theme, theme));

        ((Spinner) view.findViewById(R.id.settings_theme_dropdown)).setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, Consts.THEMES) {
            @Override
            public boolean isEnabled(int position) {
                return Consts.THEMES_ENABLE_ARRAY[position];
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
                if (isEnabled(position)) {
                    textView.setTextColor(Color.BLACK);
                } else {
                    textView.setTextColor(Color.GRAY);
                }
                return textView;
            }
        });
        ((Spinner) view.findViewById(R.id.settings_theme_dropdown)).setSelection(Arrays.asList(Consts.THEMES).indexOf(theme));
        ((Spinner) view.findViewById(R.id.settings_theme_dropdown)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                changeTheme(adapterView.getItemAtPosition(pos).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ((CheckBox) view.findViewById(R.id.settings_hide_consoles)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                hideConsoles(b);
            }
        });
        ((CheckBox) view.findViewById(R.id.settings_hide_games)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                hideGames(b);
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnFragmentInteractionListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    /* Settings-related Functions */

    private void changeTheme(String theme) {
        if (!(this.theme.equals(theme) || this.theme.equals(""))) {
            sharedPref.edit().putString(getString(R.string.theme_setting), theme).apply();
            getActivity().recreate();
        }
    }

    private void hideConsoles(boolean hide) {
        sharedPref.edit().putBoolean(getString(R.string.empty_console_hide_setting), hide).apply();
    }

    private void hideGames(boolean hide) {
        sharedPref.edit().putBoolean(getString(R.string.empty_game_hide_setting), hide).apply();
    }

    public void logout() {
        sharedPref.edit().putString(getString(R.string.ra_user), null).apply();
        getActivity().recreate();
    }

    /* Inner Classes and Interfaces */

    public interface OnFragmentInteractionListener {
        void logout(View view);
    }

}
