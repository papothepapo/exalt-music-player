package com.papothepapo.exaltmusic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PlayerActivity extends Activity {
    private final Handler handler = new Handler();
    private TextView title;
    private TextView artist;
    private TextView progress;
    private TextView speed;
    private int index;
    private final BroadcastReceiver stateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refresh();
        }
    };
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            refresh();
            handler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        index = getIntent().getIntExtra("index", PlaybackService.currentIndex());
        buildUi();
        if (bundle == null && index >= 0) {
            startService(command(PlaybackService.ACTION_PLAY_INDEX).putExtra("index", index));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        index = intent.getIntExtra("index", PlaybackService.currentIndex());
        refresh();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.BG);
        root.setPadding(8, 6, 8, 6);

        title = Ui.text(this, "", 4, Ui.ACCENT);
        artist = Ui.text(this, "", 0, Ui.TEXT);
        progress = Ui.text(this, "", 1, Ui.TEXT);
        speed = Ui.text(this, "", -1, Ui.MUTED);
        TextView footer = Ui.text(this, "Center: play/pause   Menu: speed", -2, Ui.MUTED);

        root.addView(title, new LinearLayout.LayoutParams(-1, 60));
        root.addView(artist, new LinearLayout.LayoutParams(-1, 38));
        root.addView(progress, new LinearLayout.LayoutParams(-1, 40));
        root.addView(speed, new LinearLayout.LayoutParams(-1, 34));
        root.addView(footer, new LinearLayout.LayoutParams(-1, 28));
        setContentView(root);
        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stateReceiver, new IntentFilter(PlaybackService.ACTION_STATE), RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(stateReceiver, new IntentFilter(PlaybackService.ACTION_STATE));
        }
        handler.post(ticker);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(stateReceiver);
        handler.removeCallbacks(ticker);
        super.onPause();
    }

    private Intent command(String action) {
        Intent intent = new Intent(this, PlaybackService.class);
        intent.setAction(action);
        return intent;
    }

    private void refresh() {
        int current = PlaybackService.currentIndex();
        if (current >= 0) {
            index = current;
        }
        Track track = PlaybackQueue.get(index);
        if (track == null) {
            title.setText("No track");
            artist.setText("");
            progress.setText("");
            speed.setText("");
            return;
        }
        title.setText(track.title);
        artist.setText(track.artist + " / " + track.album);
        progress.setText((PlaybackService.isPlaying() ? "Playing  " : "Paused  ")
                + Ui.mmss(PlaybackService.positionMs()) + " / " + Ui.mmss(PlaybackService.durationMs()));
        speed.setText("Speed " + PlaybackService.currentSpeed(this) + "x");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            startService(command(PlaybackService.ACTION_TOGGLE));
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            handleSideKey(true, event.getRepeatCount());
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            handleSideKey(false, event.getRepeatCount());
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_SOFT_RIGHT) {
            showMenu();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_SOFT_LEFT) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void handleSideKey(boolean forward, int repeatCount) {
        if (!PlaybackService.isPlaying()) {
            if (repeatCount == 0) {
                startService(command(forward ? PlaybackService.ACTION_NEXT : PlaybackService.ACTION_PREV));
            }
            return;
        }
        int base = forward ? AppPrefs.skipForward(this) : AppPrefs.skipBack(this);
        int multiplier = repeatCount < 4 ? 1 : Math.min(8, 1 + repeatCount / 3);
        int delta = base * multiplier * 1000;
        startService(command(PlaybackService.ACTION_SEEK).putExtra("delta", forward ? delta : -delta));
    }

    private void showMenu() {
        final String[] items = {
                "Speed 1.0x",
                "Speed 1.25x",
                "Speed 1.5x",
                "Speed 1.75x",
                "Speed 2.0x",
                "Speed 2.25x",
                "Speed 2.5x",
                "Settings"
        };
        final float[] speeds = {1f, 1.25f, 1.5f, 1.75f, 2f, 2.25f, 2.5f};
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Player")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which < speeds.length) {
                            startService(command(PlaybackService.ACTION_SPEED).putExtra("speed", speeds[which]));
                        } else {
                            startActivity(new Intent(PlayerActivity.this, SettingsActivity.class));
                        }
                    }
                })
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                AlertDialog alert = (AlertDialog) dialogInterface;
                alert.getListView().setTextDirection(android.view.View.TEXT_DIRECTION_ANY_RTL);
            }
        });
        dialog.show();
    }
}
