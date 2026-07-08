package com.papothepapo.exaltmusic;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class TrackAdapter extends BaseAdapter {
    private final Context context;
    private final List<Track> tracks;

    public TrackAdapter(Context context, List<Track> tracks) {
        this.context = context;
        this.tracks = tracks;
    }

    @Override
    public int getCount() {
        return tracks.size();
    }

    @Override
    public Track getItem(int position) {
        return tracks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return tracks.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Row row;
        if (convertView == null) {
            row = new Row(context);
        } else {
            row = (Row) convertView;
        }
        row.bind(tracks.get(position));
        return row;
    }

    private static class Row extends LinearLayout {
        private final ImageView art;
        private final ImageView now;
        private final TextView title;
        private final TextView meta;
        private final TextView duration;

        Row(Context context) {
            super(context);
            setOrientation(HORIZONTAL);
            setGravity(Gravity.CENTER_VERTICAL);
            setPadding(8, 5, 8, 5);

            art = new ImageView(context);
            art.setScaleType(ImageView.ScaleType.CENTER_CROP);
            addView(art, new LinearLayout.LayoutParams(42, 42));

            LinearLayout texts = new LinearLayout(context);
            texts.setOrientation(VERTICAL);
            texts.setPadding(8, 0, 6, 0);
            title = Ui.label(context, "", 0, Ui.textColor(context), Typeface.BOLD);
            meta = Ui.label(context, "", -3, Ui.muted(context), Typeface.NORMAL);
            texts.addView(title, new LinearLayout.LayoutParams(-1, 22));
            texts.addView(meta, new LinearLayout.LayoutParams(-1, 18));
            addView(texts, new LinearLayout.LayoutParams(0, 46, 1));

            now = new ImageView(context);
            now.setImageResource(android.R.drawable.ic_media_play);
            addView(now, new LinearLayout.LayoutParams(24, 24));

            duration = Ui.label(context, "", -2, Ui.muted(context), Typeface.NORMAL);
            duration.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            addView(duration, new LinearLayout.LayoutParams(48, 42));
        }

        void bind(Track track) {
            setBackgroundColor(Ui.bg(getContext()));
            art.setImageBitmap(AlbumArtCache.get(getContext(), track, 42));
            title.setText(track.title);
            meta.setText(track.artist + " / " + track.album);
            duration.setText(Ui.mmss((int) track.durationMs));
            Track current = PlaybackQueue.get(PlaybackService.currentIndex());
            boolean playingThis = current != null && current.path.equals(track.path);
            now.setVisibility(playingThis ? VISIBLE : INVISIBLE);
        }
    }
}
