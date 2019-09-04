package com.kobrakid.retroachievements.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.kobrakid.retroachievements.Consts;
import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.RAAPICallback;
import com.kobrakid.retroachievements.RAAPIConnection;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */
public class GameImagesFragment extends Fragment implements RAAPICallback {

    private ImageView box, title, ingame;
    private String boxURL, titleURL, ingameURL;
    private boolean isActive = false;

    public GameImagesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_pager_game_images, container, false);
        setRetainInstance(true);
        box = view.findViewById(R.id.image_boxart);
        title = view.findViewById(R.id.image_title);
        ingame = view.findViewById(R.id.image_ingame);
        if (savedInstanceState == null && getArguments() != null)
            new RAAPIConnection(getContext()).GetGameInfo(getArguments().getString("GameID", "0"), this);
        else
            populateViews();
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
        if (responseCode == RAAPIConnection.RESPONSE_GET_GAME_INFO) {
            try {
                JSONObject reader = new JSONObject(response);
                boxURL = reader.getString("ImageBoxArt");
                titleURL = reader.getString("ImageTitle");
                ingameURL = reader.getString("ImageIngame");
                populateViews();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void populateViews() {
        Picasso.get()
                .load(Consts.BASE_URL + "/" + boxURL)
                .into(box);
        Picasso.get()
                .load(Consts.BASE_URL + "/" + titleURL)
                .into(title);
        Picasso.get()
                .load(Consts.BASE_URL + "/" + ingameURL)
                .into(ingame);
    }
}
