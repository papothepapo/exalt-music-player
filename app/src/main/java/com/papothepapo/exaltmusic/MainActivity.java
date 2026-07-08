package com.papothepapo.exaltmusic;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final int REQ_STORAGE = 4;
    private final String[] tabLabels = {"Artists", "Albums", "Folders"};
    private final String[] tabTypes = {MusicRepository.GROUP_ARTIST, MusicRepository.GROUP_ALBUM, MusicRepository.GROUP_FOLDER};
    private int selectedTab = 0;
    private LinearLayout tabBar;
    private ListView listView;
    private List<MusicRepository.GroupItem> groups = new ArrayList<>();

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        buildUi();
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQ_STORAGE);
        } else {
            loadTab(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        loadTab(false);
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.bg(this));

        tabBar = new LinearLayout(this);
        tabBar.setOrientation(LinearLayout.HORIZONTAL);
        tabBar.setPadding(6, 6, 6, 2);
        root.addView(tabBar, new LinearLayout.LayoutParams(-1, 44));

        listView = new ListView(this);
        Ui.tuneList(this, listView);
        root.addView(listView, new LinearLayout.LayoutParams(-1, 0, 1));
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

    private void renderTabs() {
        tabBar.removeAllViews();
        for (int i = 0; i < tabLabels.length; i++) {
            TextView tab = Ui.label(this, tabLabels[i], -1, i == selectedTab ? Ui.bg(this) : Ui.textColor(this), Typeface.BOLD);
            tab.setGravity(android.view.Gravity.CENTER);
            tab.setBackground(Ui.rounded(i == selectedTab ? Ui.accent(this) : Ui.panel(this), 8));
            tabBar.addView(tab, new LinearLayout.LayoutParams(0, -1, 1));
        }
    }

    private void loadTab(boolean animate) {
        renderTabs();
        MusicRepository repo = new MusicRepository(this);
        groups = selectedTab == 2 ? repo.folderChildren(null) : repo.groups(tabTypes[selectedTab]);
        listView.setAdapter(new GroupAdapter(this, groups));
        if (!groups.isEmpty()) {
            listView.setSelection(0);
        }
        if (animate) {
            TranslateAnimation animation = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0.35f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f);
            animation.setDuration(140);
            listView.startAnimation(animation);
        }
    }

    private void openGroup(int position) {
        if (position < 0 || position >= groups.size()) {
            return;
        }
        MusicRepository.GroupItem item = groups.get(position);
        Intent intent;
        if (MusicRepository.GROUP_FOLDER.equals(item.type)) {
            intent = new Intent(this, FolderActivity.class);
            intent.putExtra("folder", item.key);
            intent.putExtra("label", item.label);
        } else {
            intent = new Intent(this, TrackListActivity.class);
            intent.putExtra("type", item.type);
            intent.putExtra("key", item.key);
            intent.putExtra("label", item.label);
        }
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
        loadTab(true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Ui.moveList(listView, keyCode, event.getRepeatCount())) {
            return true;
        }
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
        if (keyCode == KeyEvent.KEYCODE_SOFT_LEFT && Ui.hasNowPlaying()) {
            Ui.openNowPlaying(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (listView != null) {
            loadTab(false);
        }
    }
}
