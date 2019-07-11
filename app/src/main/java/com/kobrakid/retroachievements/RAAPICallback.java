package com.kobrakid.retroachievements;

import android.widget.TextView;

public class RAAPICallback implements Runnable {

    private final TextView destination;
    public String result = "";

    public RAAPICallback(TextView destination) {
        this.destination = destination;
    }

    @Override
    public void run() {
        destination.setText(result);
    }

}
