package com.papothepapo.exaltmusic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

public class SettingsActivity extends Activity {
    private ListView listView;
    private final List<String> rows = new ArrayList<>();

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        buildUi();
        refresh();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.bg(this));
        root.addView(Ui.label(this, "Settings", 2, Ui.accent(this), Typeface.BOLD), new LinearLayout.LayoutParams(-1, 42));
        listView = new ListView(this);
        Ui.tuneList(this, listView);
        root.addView(listView, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openSetting(position);
            }
        });
    }

    private void refresh() {
        rows.clear();
        rows.add("Now Playing");
        rows.add("Forward skip: " + AppPrefs.skipForward(this) + " sec");
        rows.add("Back skip: " + AppPrefs.skipBack(this) + " sec");
        rows.add("Theme: " + AppPrefs.themeName(this));
        rows.add("Font: " + AppPrefs.fontName(this));
        rows.add("Font size: " + Math.round(AppPrefs.fontSize(this)) + " sp");
        rows.add("Help");
        listView.setAdapter(Ui.adapter(this, rows));
    }

    private void openSetting(int position) {
        if (position == 0) {
            if (Ui.hasNowPlaying()) {
                Ui.openNowPlaying(this);
            }
        } else if (position == 1) {
            chooseInt("Forward skip", new int[]{3, 5, 10, 15, 30, 45, 60}, true);
        } else if (position == 2) {
            chooseInt("Back skip", new int[]{3, 5, 10, 15, 30, 45, 60}, false);
        } else if (position == 3) {
            chooseTheme();
        } else if (position == 4) {
            chooseFont();
        } else if (position == 5) {
            chooseSize();
        } else if (position == 6) {
            startActivity(new Intent(this, HelpActivity.class));
        }
    }

    private void chooseInt(String title, final int[] values, final boolean forward) {
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = values[i] + " sec";
        }
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(labels, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (forward) {
                            AppPrefs.setSkipForward(SettingsActivity.this, values[which]);
                        } else {
                            AppPrefs.setSkipBack(SettingsActivity.this, values[which]);
                        }
                        refresh();
                    }
                })
                .show();
    }

    private void chooseTheme() {
        final String[] values = {"charcoal", "midnight", "olive", "paper"};
        new AlertDialog.Builder(this)
                .setTitle("Theme")
                .setItems(values, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AppPrefs.setThemeName(SettingsActivity.this, values[which]);
                        buildUi();
                        refresh();
                    }
                })
                .show();
    }

    private void chooseFont() {
        final String[] values = {"sans-serif", "sans-serif-light", "sans-serif-condensed", "sans-serif-medium", "casual"};
        new AlertDialog.Builder(this)
                .setTitle("Font")
                .setItems(values, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AppPrefs.setFontName(SettingsActivity.this, values[which]);
                        buildUi();
                        refresh();
                    }
                })
                .show();
    }

    private void chooseSize() {
        final float[] values = {13f, 14f, 16f, 18f, 20f};
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = Math.round(values[i]) + " sp";
        }
        new AlertDialog.Builder(this)
                .setTitle("Font size")
                .setItems(labels, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AppPrefs.setFontSize(SettingsActivity.this, values[which]);
                        buildUi();
                        refresh();
                    }
                })
                .show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Ui.moveList(listView, keyCode, event.getRepeatCount())) {
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            openSetting(listView.getSelectedItemPosition());
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_SOFT_LEFT || keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
