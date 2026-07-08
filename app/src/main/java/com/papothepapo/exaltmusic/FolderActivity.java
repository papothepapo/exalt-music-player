package com.papothepapo.exaltmusic;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class FolderActivity extends Activity {
    private String folder;
    private String label;
    private ListView folderList;
    private ListView trackList;
    private List<MusicRepository.GroupItem> children = new ArrayList<>();
    private List<Track> tracks = new ArrayList<>();
    private boolean onTracks;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        folder = getIntent().getStringExtra("folder");
        label = Track.clean(getIntent().getStringExtra("label"), "Folder");
        buildUi();
        load();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.bg(this));
        root.addView(Ui.label(this, label, 2, Ui.accent(this), Typeface.BOLD), new LinearLayout.LayoutParams(-1, 36));

        folderList = new ListView(this);
        trackList = new ListView(this);
        Ui.tuneList(this, folderList);
        Ui.tuneList(this, trackList);
        root.addView(folderList, new LinearLayout.LayoutParams(-1, 0, 1));
        root.addView(trackList, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        folderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openFolder(position);
            }
        });
        trackList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                play(position);
            }
        });
        trackList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                edit(position);
                return true;
            }
        });
    }

    private void load() {
        MusicRepository repo = new MusicRepository(this);
        children = repo.folderChildren(folder);
        tracks = repo.tracksInFolder(folder);
        folderList.setAdapter(new GroupAdapter(this, children));
        trackList.setAdapter(new TrackAdapter(this, tracks));
        folderList.setVisibility(children.isEmpty() ? View.GONE : View.VISIBLE);
        trackList.setVisibility(tracks.isEmpty() ? View.GONE : View.VISIBLE);
        onTracks = children.isEmpty();
        activeList().requestFocus();
        if (activeList().getCount() > 0) {
            activeList().setSelection(0);
        }
    }

    private ListView activeList() {
        return onTracks ? trackList : folderList;
    }

    private void openFolder(int position) {
        if (position < 0 || position >= children.size()) {
            return;
        }
        MusicRepository.GroupItem item = children.get(position);
        Intent intent = new Intent(this, FolderActivity.class);
        intent.putExtra("folder", item.key);
        intent.putExtra("label", item.label);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void play(int position) {
        if (position < 0 || position >= tracks.size()) {
            return;
        }
        PlaybackQueue.set(new MusicRepository(this).tracksUnderFolder(folder));
        int index = 0;
        List<Track> queue = PlaybackQueue.all();
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).path.equals(tracks.get(position).path)) {
                index = i;
                break;
            }
        }
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("index", index);
        intent.putExtra("autoplay", true);
        startActivity(intent);
    }

    private void edit(int position) {
        if (position < 0 || position >= tracks.size()) {
            return;
        }
        Intent intent = new Intent(this, EditTagsActivity.class);
        intent.putExtra("bulk", false);
        intent.putExtra("path", tracks.get(position).path);
        intent.putExtra("type", MusicRepository.GROUP_FOLDER);
        intent.putExtra("key", folder);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Ui.moveList(activeList(), keyCode, event.getRepeatCount())) {
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && !tracks.isEmpty()) {
            onTracks = true;
            trackList.requestFocus();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (onTracks && !children.isEmpty()) {
                onTracks = false;
                folderList.requestFocus();
            } else {
                finish();
            }
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            if (onTracks) {
                play(trackList.getSelectedItemPosition());
            } else {
                openFolder(folderList.getSelectedItemPosition());
            }
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_SOFT_RIGHT) {
            Ui.openNowPlaying(this);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_SOFT_LEFT) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
