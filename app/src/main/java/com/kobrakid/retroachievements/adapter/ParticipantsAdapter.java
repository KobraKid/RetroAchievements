package com.kobrakid.retroachievements.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kobrakid.retroachievements.Consts;
import com.kobrakid.retroachievements.MainActivity;
import com.kobrakid.retroachievements.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ParticipantsAdapter extends RecyclerView.Adapter {

    private Context context;
    private ArrayList<String> users, results, dates;

    public ParticipantsAdapter(Context context, ArrayList<String> users, ArrayList<String> results, ArrayList<String> dates) {
        this.context = context;
        this.users = users;
        this.results = results;
        this.dates = dates;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ParticipantViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_participants, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Picasso.get()
                .load(Consts.BASE_URL + "/" + Consts.USER_PIC_POSTFIX + "/" + users.get(position) + ".png")
                .into(((ImageView) holder.itemView.findViewById(R.id.participant_icon)));
        ((TextView) holder.itemView.findViewById(R.id.participant_rank)).setText("#" + (position + 1));
        ((TextView) holder.itemView.findViewById(R.id.participant_username)).setText(users.get(position));
        ((TextView) holder.itemView.findViewById(R.id.participant_result)).setText(results.get(position));
        ((TextView) holder.itemView.findViewById(R.id.participant_date)).setText(dates.get(position));
        if (MainActivity.ra_user.equals(users.get(position)))
            holder.itemView.setBackground(context.getDrawable(R.drawable.border));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    private class ParticipantViewHolder extends RecyclerView.ViewHolder {

        public ParticipantViewHolder(View itemView) {
            super(itemView);
        }
    }
}
