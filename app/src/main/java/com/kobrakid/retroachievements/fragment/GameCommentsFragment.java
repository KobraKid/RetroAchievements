package com.kobrakid.retroachievements.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.RAAPICallback;
import com.kobrakid.retroachievements.RAAPIConnection;
import com.kobrakid.retroachievements.adapter.GameCommentsAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Fragment to hold recent game comments.
 */
public class GameCommentsFragment extends Fragment implements RAAPICallback {

    private RecyclerView.Adapter gameCommentsAdapter;
    private Map<String, List<String>> comments = new HashMap<>();

    public GameCommentsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_game_comments, container, false);
        if (getArguments() != null) {
            RecyclerView commentsRecyclerView = view.findViewById(R.id.game_comments_recycler_view);
            commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            gameCommentsAdapter = new GameCommentsAdapter(getContext(), comments);
            commentsRecyclerView.setAdapter(gameCommentsAdapter);
            new RAAPIConnection(getContext()).ScrapeGameInfoFromWeb(getArguments().getString("GameID"), this);
        }
        return view;
    }

    @Override
    public void callback(int responseCode, String response) {
        if (responseCode == RAAPIConnection.RESPONSE_SCRAPE_GAME_PAGE) {
            new ParseCommentsAsyncTask(this).execute(response);
        }
    }

    private static class ParseCommentsAsyncTask extends AsyncTask<String, Void, Map<String, List<String>>> {

        final WeakReference<GameCommentsFragment> fragmentReference;

        ParseCommentsAsyncTask(GameCommentsFragment fragment) {
            fragmentReference = new WeakReference<>(fragment);
        }

        @Override
        protected Map<String, List<String>> doInBackground(String... strings) {
            String response = strings[0];
            Document document = Jsoup.parse(response);
            Elements elements = document.getElementsByClass("feed_comment");
            Map<String, List<String>> comments = new HashMap<>();
            List<String> text = new ArrayList<>(),
                    user = new ArrayList<>(),
                    acct = new ArrayList<>(),
                    score = new ArrayList<>(),
                    rank = new ArrayList<>(),
                    tag = new ArrayList<>(),
                    date = new ArrayList<>();
            for (int i = 0; i < elements.size(); i++) {
                Element e = elements.get(i);
                text.add(e.selectFirst("td.commenttext").html().trim());
                user.add(e.selectFirst("td.iconscommentsingle").selectFirst("img").attr("title").trim());
                String tooltip = e.selectFirst("td.iconscommentsingle").selectFirst("div").attr("onmouseover");
                Document userCard = Jsoup.parse(Parser.unescapeEntities(tooltip.substring(5, tooltip.length() - 2).replace("\\", ""), false));
                acct.add(userCard.selectFirst("td.usercardaccounttype").html().trim());
                score.add(userCard.select("td.usercardbasictext").get(0).html().substring(15));
                rank.add(userCard.select("td.usercardbasictext").get(1).html().substring(18));
                tag.add(userCard.selectFirst("span") != null ? userCard.selectFirst("span").html() : "RA_NO_TAG_" + i);
                date.add(e.selectFirst("td.smalldate").html().trim());
            }
            comments.put("text", text);
            comments.put("user", user);
            comments.put("acct", acct);
            comments.put("score", score);
            comments.put("rank", rank);
            comments.put("tag", tag);
            comments.put("date", date);
            return comments;
        }

        @Override
        protected void onPostExecute(Map<String, List<String>> strings) {
            final GameCommentsFragment fragment = fragmentReference.get();
            if (fragment != null && fragment.gameCommentsAdapter != null) {
                fragment.comments.putAll(strings);
                fragment.gameCommentsAdapter.notifyDataSetChanged();
            }
        }
    }
}
