package com.kobrakid.retroachievements.fragment;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.kobrakid.retroachievements.Consts;
import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.RAAPICallback;
import com.kobrakid.retroachievements.RAAPIConnection;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class GameImagesFragment extends Fragment implements RAAPICallback {

    private String boxURL, titleURL, ingameURL;
    private View view;
    private boolean isActive = false;

    public GameImagesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.view_pager_game_images, container, false);

        setRetainInstance(true);
        if (savedInstanceState == null && getArguments() != null)
            new RAAPIConnection(Objects.requireNonNull(getContext())).GetGameInfo(getArguments().getString("GameID", "0"), this);
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
        Resources res = this.view.getContext().getResources();
        Picasso.get()
                .load(Consts.BASE_URL + "/" + boxURL)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        Drawable drawable = new BitmapDrawable(res, bitmap);
                        int scale = (view.findViewById(R.id.card_0_boxart).getWidth() - 16) / drawable.getIntrinsicWidth();
                        drawable.setBounds(0, 0, drawable.getIntrinsicWidth() * scale, drawable.getIntrinsicHeight() * scale);
                        ((TextView) view.findViewById(R.id.image_boxart)).setCompoundDrawables(null, drawable, null, null);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                });
        Picasso.get()
                .load(Consts.BASE_URL + "/" + titleURL)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        Drawable drawable = new BitmapDrawable(res, bitmap);
                        int scale = (view.findViewById(R.id.card_1_title).getWidth() - 16) / drawable.getIntrinsicWidth();
                        drawable.setBounds(0, 0, drawable.getIntrinsicWidth() * scale, drawable.getIntrinsicHeight() * scale);
                        ((TextView) view.findViewById(R.id.image_title)).setCompoundDrawables(null, drawable, null, null);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                });
        Picasso.get()
                .load(Consts.BASE_URL + "/" + ingameURL)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        Drawable drawable = new BitmapDrawable(res, bitmap);
                        int scale = (view.findViewById(R.id.card_2_ingame).getWidth() - 16) / drawable.getIntrinsicWidth();
                        drawable.setBounds(0, 0, drawable.getIntrinsicWidth() * scale, drawable.getIntrinsicHeight() * scale);
                        ((TextView) view.findViewById(R.id.image_ingame)).setCompoundDrawables(null, drawable, null, null);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                });
    }
}
