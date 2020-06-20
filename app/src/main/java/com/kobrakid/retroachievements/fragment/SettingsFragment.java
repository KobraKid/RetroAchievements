package com.kobrakid.retroachievements.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kobrakid.retroachievements.AppExecutors;
import com.kobrakid.retroachievements.Consts;
import com.kobrakid.retroachievements.MainActivity;
import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.RAAPICallback;
import com.kobrakid.retroachievements.RAAPIConnection;
import com.kobrakid.retroachievements.database.Console;
import com.kobrakid.retroachievements.database.RetroAchievementsDatabase;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SettingsFragment extends Fragment implements RAAPICallback {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    // Unused, but guarantees that the parent Activity implements OnFragmentInteractionListener
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private OnFragmentInteractionListener listener;
    private RAAPIConnection apiConnection;
    private SharedPreferences sharedPref;
    private final StringBuilder consoleID = new StringBuilder();
    private final StringBuilder consoleName = new StringBuilder();

    private final SparseArray<Runnable> applicableSettings = new SparseArray<>();
    // Can be local, but used to index which settings have been modified
    @SuppressWarnings("FieldCanBeLocal")
    private final int logout_key = 0;
    private final int hide_consoles_key = 1;
    @SuppressWarnings("FieldCanBeLocal")
    private final int hide_games_key = 2;
    @SuppressWarnings("FieldCanBeLocal")
    private final int change_theme_key = 3;

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Objects.requireNonNull(getActivity()).setTitle("Settings");

        // Initialize preferences object
        sharedPref = Objects.requireNonNull(getContext()).getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Set up views
        String theme = sharedPref.getString(getString(R.string.theme_setting), "");
        ((TextView) view.findViewById(R.id.settings_current_theme)).setText(getString(R.string.settings_current_theme, theme));
        ((TextView) view.findViewById(R.id.settings_current_user)).setText(MainActivity.ra_user == null ? getString(R.string.settings_no_current_user) : getString(R.string.settings_current_user, MainActivity.ra_user));

        ((Spinner) view.findViewById(R.id.settings_theme_dropdown)).setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, Consts.THEMES) {
            @Override
            public boolean isEnabled(int position) {
                return Consts.THEMES_ENABLE_ARRAY[position];
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
                if (isEnabled(position)) {
                    TypedValue typedValue = new TypedValue();
                    getContext().getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
                    textView.setTextColor(getResources().getColor(typedValue.resourceId));
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
                if (pos > 0)
                    changeTheme(adapterView.getItemAtPosition(pos).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ((CheckBox) view.findViewById(R.id.settings_hide_consoles)).setChecked(sharedPref.getBoolean(getString(R.string.empty_console_hide_setting), false));
        if (sharedPref.getBoolean(getString(R.string.empty_console_hide_setting), false))
            view.findViewById(R.id.settings_hide_consoles_warning).setVisibility(View.GONE);
        ((CheckBox) view.findViewById(R.id.settings_hide_games)).setChecked(sharedPref.getBoolean(getString(R.string.empty_game_hide_setting), false));
        ((CheckBox) view.findViewById(R.id.settings_hide_consoles)).setOnCheckedChangeListener((compoundButton, b) -> hideConsoles(view, b));
        ((CheckBox) view.findViewById(R.id.settings_hide_games)).setOnCheckedChangeListener((compoundButton, b) -> hideGames(view, b));

        apiConnection = ((MainActivity) getActivity()).apiConnection;

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (OnFragmentInteractionListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(Objects.requireNonNull(getActivity()).toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    /* Settings-related Functions */

    private void changeTheme(@NonNull final String theme) {
        applicableSettings.remove(change_theme_key);
        applicableSettings.put(change_theme_key, () -> {
            Log.d(TAG, "Saving theme " + theme);
            sharedPref.edit().putString(getString(R.string.theme_setting), theme).apply();
        });
    }

    private void hideConsoles(View view, final boolean hide) {
        view.findViewById(R.id.settings_hide_consoles_warning).setVisibility(hide ? View.GONE : View.VISIBLE);
        if (applicableSettings.get(hide_consoles_key) != null) {
            applicableSettings.remove(hide_consoles_key);
        } else {
            final RAAPICallback callback = this;
            applicableSettings.put(hide_consoles_key, () -> {
                sharedPref.edit().putBoolean(getString(R.string.empty_console_hide_setting), hide).apply();
                if (hide) {
                    // Get all consoles and store their game counts
                    final RetroAchievementsDatabase db = RetroAchievementsDatabase.getInstance(getContext());
                    AppExecutors.getInstance().diskIO().execute(() -> {
                        db.consoleDao().clearTable();
                        Log.d(TAG, "Clearing table");
                    });
                    apiConnection.GetConsoleIDs(callback);
                } else {
                    Objects.requireNonNull(getActivity()).recreate();
                }
            });
        }
    }

    private void hideGames(View view, final boolean hide) {
        view.findViewById(R.id.settings_hide_games_warning).setVisibility(hide ? View.GONE : View.VISIBLE);
        if (applicableSettings.get(hide_games_key) != null) {
            applicableSettings.remove(hide_games_key);
        } else {
            applicableSettings.put(hide_games_key, () -> sharedPref.edit().putBoolean(getString(R.string.empty_game_hide_setting), hide).apply());
        }
    }

    public void logout() {
        ((TextView) Objects.requireNonNull(getActivity()).findViewById(R.id.settings_current_user)).setText(getString(R.string.settings_current_user, "none"));
        applicableSettings.put(logout_key, () -> sharedPref.edit().putString(getString(R.string.ra_user), null).apply());
    }

    public void applySettings() {
        Objects.requireNonNull(getActivity()).findViewById(R.id.settings_applying_fade).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.settings_applying).setVisibility(View.VISIBLE);
        for (int key = 0; key < applicableSettings.size(); key++) {
            applicableSettings.valueAt(key).run();
        }
        if (applicableSettings.get(hide_consoles_key) == null) {
            // Recreate activity now if no db operations are running
            getActivity().recreate();
        }
    }

    @Override
    public void callback(int responseCode, final String response) {
        if (responseCode == RAAPIConnection.RESPONSE_ERROR)
            return;
        if (responseCode == RAAPIConnection.RESPONSE_GET_CONSOLE_IDS) {
            final RetroAchievementsDatabase db = RetroAchievementsDatabase.getInstance(getContext());
            final RAAPIConnection connection = apiConnection;
            final RAAPICallback callback = this;
            AppExecutors.getInstance().diskIO().execute(() -> {
                try {
                    JSONArray reader = new JSONArray(response);
                    for (int i = 0; i < reader.length(); i++) {
                        List<Console> consoles = db.consoleDao().getConsoleWithID(Integer.parseInt(reader.getJSONObject(i).getString("ID")));
                        if (consoles.size() == 0) {
                            consoleID.delete(0, consoleID.length());
                            consoleName.delete(0, consoleName.length());
                            consoleID.append(reader.getJSONObject(i).getString("ID"));
                            consoleName.append(reader.getJSONObject(i).getString("Name"));
                            connection.GetGameList(reader.getJSONObject(i).getString("ID"), callback);
                            return;
                        }
                    }
                    AppExecutors.getInstance().mainThread().execute(() -> Objects.requireNonNull(getActivity()).recreate());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        } else if (responseCode == RAAPIConnection.RESPONSE_GET_GAME_LIST) {
            try {
                final JSONArray reader = new JSONArray(response);
                final RetroAchievementsDatabase db = RetroAchievementsDatabase.getInstance(getContext());
                AppExecutors.getInstance().diskIO().execute(() -> {
                    db.consoleDao().insertConsole(new Console(Integer.parseInt(consoleID.toString()), consoleName.toString(), reader.length()));
                    Log.d(TAG, "Adding console " + consoleName.toString() + "(" + consoleID.toString() + "): " + reader.length() + " games");
                });
                // Recurse until all consoles are added to db
                apiConnection.GetConsoleIDs(this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /* Inner Classes and Interfaces */

    public interface OnFragmentInteractionListener {
        void logout(@SuppressWarnings("unused") View view);

        void applySettings(@SuppressWarnings("unused") View view);
    }

}
