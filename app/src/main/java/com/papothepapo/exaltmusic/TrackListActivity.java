package com.papothepapo.exaltmusic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class TrackListActivity extends Activity {
    private String type;
    private String key;
    private String label;
    private ListView listView;
    private List<Track> tracks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        type = getIntent().getStringExtra("type");
        key = getIntent().getStringExtra("key");
        label = getIntent().getStringExtra("label");
        buildUi();
        loadTracks();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.BG);
        root.addView(Ui.text(this, Track.clean(label, "Tracks"), 2, Ui.ACCENT), new LinearLayout.LayoutParams(-1, 42));

        listView = new ListView(this);
        Ui.tuneList(this, listView);
        root.addView(listView, new LinearLayout.LayoutParams(-1, 0, 1));
        root.addView(Ui.text(this, "Center: play   Hold: edit track", -2, Ui.MUTED), new LinearLayout.LayoutParams(-1, 28));
        setContentView(root);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                play(position);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                edit(position);
                return true;
            }
        });
    }

    private void loadTracks() {
        tracks = new MusicRepository(this).tracksFor(type, key);
        List<String> rows = new ArrayList<>();
        for (Track track : tracks) {
            rows.add(track.title + " - " + track.artist);
        }
        listView.setAdapter(Ui.adapter(this, rows));
        if (!rows.isEmpty()) {
            listView.setSelection(0);
        }
    }

    private void play(int position) {
        if (position < 0 || position >= tracks.size()) {
            return;
        }
        PlaybackQueue.set(tracks);
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("index", position);
        startActivity(intent);
    }

    private void edit(int position) {
        if (position < 0 || position >= tracks.size()) {
            return;
        }
        Intent intent = new Intent(this, EditTagsActivity.class);
        intent.putExtra("bulk", false);
        intent.putExtra("path", tracks.get(position).path);
        intent.putExtra("type", type);
        intent.putExtra("key", key);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            play(listView.getSelectedItemPosition());
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_SOFT_LEFT) {
            finish();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_SOFT_RIGHT) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (listView != null) {
            loadTracks();
        }
    }
}
