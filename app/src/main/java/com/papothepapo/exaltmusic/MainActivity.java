package com.papothepapo.exaltmusic;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final int REQ_STORAGE = 4;
    private final String[] tabLabels = {"Album Artists", "Albums", "Folders"};
    private final String[] tabTypes = {MusicRepository.GROUP_ARTIST, MusicRepository.GROUP_ALBUM, MusicRepository.GROUP_FOLDER};
    private int selectedTab = 0;
    private TextView title;
    private ListView listView;
    private List<MusicRepository.GroupItem> groups = new ArrayList<>();

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        buildUi();
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQ_STORAGE);
        } else {
            loadTab();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        loadTab();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.BG);

        title = Ui.text(this, "", 2, Ui.ACCENT);
        root.addView(title, new LinearLayout.LayoutParams(-1, 42));

        listView = new ListView(this);
        Ui.tuneList(this, listView);
        root.addView(listView, new LinearLayout.LayoutParams(-1, 0, 1));

        TextView footer = Ui.text(this, "Menu: settings   Hold: edit group", -2, Ui.MUTED);
        root.addView(footer, new LinearLayout.LayoutParams(-1, 28));
        setContentView(root);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openGroup(position);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                editGroup(position);
                return true;
            }
        });
    }

    private void loadTab() {
        title.setText(tabLabels[selectedTab]);
        groups = new MusicRepository(this).groups(tabTypes[selectedTab]);
        listView.setAdapter(Ui.adapter(this, groups));
        if (!groups.isEmpty()) {
            listView.setSelection(0);
        }
    }

    private void openGroup(int position) {
        if (position < 0 || position >= groups.size()) {
            return;
        }
        MusicRepository.GroupItem item = groups.get(position);
        Intent intent = new Intent(this, TrackListActivity.class);
        intent.putExtra("type", item.type);
        intent.putExtra("key", item.key);
        intent.putExtra("label", item.label);
        startActivity(intent);
    }

    private void editGroup(int position) {
        if (position < 0 || position >= groups.size()) {
            return;
        }
        MusicRepository.GroupItem item = groups.get(position);
        Intent intent = new Intent(this, EditTagsActivity.class);
        intent.putExtra("bulk", true);
        intent.putExtra("type", item.type);
        intent.putExtra("key", item.key);
        intent.putExtra("label", item.label);
        startActivity(intent);
    }

    private void moveTab(int delta) {
        selectedTab = (selectedTab + delta + tabLabels.length) % tabLabels.length;
        loadTab();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getRepeatCount() == 0 && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            moveTab(1);
            return true;
        }
        if (event.getRepeatCount() == 0 && keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            moveTab(-1);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            openGroup(listView.getSelectedItemPosition());
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
        if (title != null) {
            loadTab();
        }
    }
}
