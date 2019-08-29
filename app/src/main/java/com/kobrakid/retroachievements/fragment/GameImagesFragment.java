package com.kobrakid.retroachievements.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
    private boolean isActive = false;

    public GameImagesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_pager_game_images, container, false);
        box = view.findViewById(R.id.image_boxart);
        title = view.findViewById(R.id.image_title);
        ingame = view.findViewById(R.id.image_ingame);
        new RAAPIConnection(getContext()).GetGameInfo(getArguments().getString("GameID", "0"), this);
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
    public void callback(int responseCode, String response) {
        if (!isActive)
            return;
        if (responseCode == RAAPIConnection.RESPONSE_GET_GAME_INFO) {
            try {
                JSONObject reader = new JSONObject(response);
                Picasso.get()
                        .load(Consts.BASE_URL + "/" + reader.getString("ImageBoxArt"))
                        .into(box);
                Picasso.get()
                        .load(Consts.BASE_URL + "/" + reader.getString("ImageTitle"))
                        .into(title);
                Picasso.get()
                        .load(Consts.BASE_URL + "/" + reader.getString("ImageIngame"))
                        .into(ingame);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
