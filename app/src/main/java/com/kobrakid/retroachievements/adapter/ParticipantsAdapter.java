package com.kobrakid.retroachievements.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kobrakid.retroachievements.Consts;
import com.kobrakid.retroachievements.MainActivity;
import com.kobrakid.retroachievements.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.ParticipantViewHolder> {

    private final Context context;
    public final List<String> users = new ArrayList<>();
    public final List<String> results = new ArrayList<>();
    public final List<String> dates = new ArrayList<>();

    public ParticipantsAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ParticipantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ParticipantViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_participants, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipantViewHolder holder, int position) {
        Picasso.get()
                .load(Consts.BASE_URL + "/" + Consts.USER_PIC_POSTFIX + "/" + users.get(position) + ".png")
                .into(((ImageView) holder.itemView.findViewById(R.id.participant_icon)));
        ((TextView) holder.itemView.findViewById(R.id.participant_rank)).setText(context.getString(R.string.participant, position + 1));
        ((TextView) holder.itemView.findViewById(R.id.participant_username)).setText(users.get(position));
        ((TextView) holder.itemView.findViewById(R.id.participant_result)).setText(results.get(position));
        ((TextView) holder.itemView.findViewById(R.id.participant_date)).setText(dates.get(position));
        if (MainActivity.ra_user != null && MainActivity.ra_user.equals(users.get(position)))
            holder.itemView.setBackground(context.getDrawable(R.drawable.border));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void addParticipant(String user, String result, String date) {
        users.add(user);
        results.add(result);
        dates.add(date);
        notifyItemInserted(users.size() - 1);
    }

    static class ParticipantViewHolder extends RecyclerView.ViewHolder {

        ParticipantViewHolder(View itemView) {
            super(itemView);
        }
    }
}
