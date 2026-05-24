package com.papothepapo.exaltmusic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.BG);
        root.addView(Ui.text(this, "Settings", 2, Ui.ACCENT), new LinearLayout.LayoutParams(-1, 42));
        listView = new ListView(this);
        Ui.tuneList(this, listView);
        root.addView(listView, new LinearLayout.LayoutParams(-1, 0, 1));
        root.addView(Ui.text(this, "Center: change   Left: back", -2, Ui.MUTED), new LinearLayout.LayoutParams(-1, 28));
        setContentView(root);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openSetting(position);
            }
        });
        refresh();
    }

    private void refresh() {
        rows.clear();
        rows.add("Forward skip: " + AppPrefs.skipForward(this) + " sec");
        rows.add("Back skip: " + AppPrefs.skipBack(this) + " sec");
        rows.add("Font: " + AppPrefs.fontName(this));
        rows.add("Font size: " + Math.round(AppPrefs.fontSize(this)) + " sp");
        listView.setAdapter(Ui.adapter(this, rows));
    }

    private void openSetting(int position) {
        if (position == 0) {
            chooseInt("Forward skip", new int[]{5, 10, 15, 30, 45, 60}, true);
        } else if (position == 1) {
            chooseInt("Back skip", new int[]{5, 10, 15, 30, 45, 60}, false);
        } else if (position == 2) {
            chooseFont();
        } else if (position == 3) {
            chooseSize();
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

    private void chooseFont() {
        final String[] values = {"sans", "serif", "monospace"};
        new AlertDialog.Builder(this)
                .setTitle("Font")
                .setItems(values, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AppPrefs.setFontName(SettingsActivity.this, values[which]);
                        refresh();
                    }
                })
                .show();
    }

    private void chooseSize() {
        final float[] values = {14f, 16f, 18f, 20f, 22f};
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
                        refresh();
                    }
                })
                .show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            openSetting(listView.getSelectedItemPosition());
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_SOFT_LEFT) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
