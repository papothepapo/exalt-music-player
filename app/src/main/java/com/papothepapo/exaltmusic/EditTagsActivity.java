package com.papothepapo.exaltmusic;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class EditTagsActivity extends Activity {
    private boolean bulk;
    private String type;
    private String key;
    private String path;
    private EditText titleField;
    private EditText artistField;
    private EditText albumField;
    private EditText albumArtistField;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        bulk = getIntent().getBooleanExtra("bulk", false);
        type = getIntent().getStringExtra("type");
        key = getIntent().getStringExtra("key");
        path = getIntent().getStringExtra("path");
        buildUi();
        loadValues();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.BG);
        root.setPadding(8, 4, 8, 4);
        root.addView(Ui.text(this, bulk ? "Edit Group Tags" : "Edit Track Tags", 2, Ui.ACCENT), new LinearLayout.LayoutParams(-1, 36));

        if (!bulk) {
            titleField = field("Title");
            root.addView(titleField, new LinearLayout.LayoutParams(-1, 36));
        }
        artistField = field("Artist");
        albumField = field("Album");
        albumArtistField = field("Album artist");
        root.addView(artistField, new LinearLayout.LayoutParams(-1, 36));
        root.addView(albumField, new LinearLayout.LayoutParams(-1, 36));
        root.addView(albumArtistField, new LinearLayout.LayoutParams(-1, 36));

        TextView footer = Ui.text(this, "Right/Menu: save   Left: cancel", -2, Ui.MUTED);
        root.addView(footer, new LinearLayout.LayoutParams(-1, 28));
        setContentView(root);
    }

    private EditText field(String hint) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setSingleLine(true);
        editText.setTextColor(Ui.TEXT);
        editText.setHintTextColor(Ui.MUTED);
        editText.setTextSize(AppPrefs.fontSize(this));
        editText.setTypeface(AppPrefs.typeface(this));
        editText.setTextDirection(android.view.View.TEXT_DIRECTION_ANY_RTL);
        editText.setSelectAllOnFocus(false);
        return editText;
    }

    private void loadValues() {
        List<Track> tracks = tracks();
        if (tracks.isEmpty()) {
            return;
        }
        Track track = tracks.get(0);
        if (!bulk && titleField != null) {
            titleField.setText(track.title);
        }
        artistField.setText(track.artist);
        albumField.setText(track.album);
        albumArtistField.setText(track.albumArtist);
    }

    private List<Track> tracks() {
        List<Track> source = new MusicRepository(this).tracksFor(type, key);
        if (bulk) {
            return source;
        }
        for (int i = 0; i < source.size(); i++) {
            if (source.get(i).path.equals(path)) {
                return source.subList(i, i + 1);
            }
        }
        return source.subList(0, 0);
    }

    private void save() {
        List<Track> selected = tracks();
        if (selected.isEmpty()) {
            finish();
            return;
        }
        if (bulk) {
            TagStore.saveBulk(this, selected, artistField.getText().toString(), albumField.getText().toString(), albumArtistField.getText().toString());
            Toast.makeText(this, "Updated " + selected.size() + " tracks", Toast.LENGTH_SHORT).show();
        } else {
            TagStore.saveTrack(this, selected.get(0), titleField.getText().toString(), artistField.getText().toString(), albumField.getText().toString(), albumArtistField.getText().toString());
            Toast.makeText(this, "Track tags updated", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_SOFT_RIGHT) {
            save();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_SOFT_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
