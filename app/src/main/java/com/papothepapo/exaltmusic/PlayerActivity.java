package com.papothepapo.exaltmusic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PlayerActivity extends Activity {
    private final Handler handler = new Handler();
    private ImageView art;
    private ImageView stateIcon;
    private ProgressBar seek;
    private TextView title;
    private TextView meta;
    private TextView time;
    private TextView speed;
    private LinearLayout root;
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
            handler.postDelayed(this, 450);
        }
    };

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        index = getIntent().getIntExtra("index", PlaybackService.currentIndex());
        buildUi();
        boolean autoplay = getIntent().getBooleanExtra("autoplay", bundle == null && index >= 0 && index != PlaybackService.currentIndex());
        if (autoplay && index >= 0) {
            startService(command(PlaybackService.ACTION_PLAY_INDEX).putExtra("index", index));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        index = intent.getIntExtra("index", PlaybackService.currentIndex());
        if (intent.getBooleanExtra("autoplay", false) && index >= 0) {
            startService(command(PlaybackService.ACTION_PLAY_INDEX).putExtra("index", index));
        }
        refresh();
    }

    private void buildUi() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(10, 8, 10, 8);
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        art = new ImageView(this);
        art.setScaleType(ImageView.ScaleType.CENTER_CROP);
        top.addView(art, new LinearLayout.LayoutParams(82, 82));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setPadding(10, 0, 0, 0);
        title = Ui.label(this, "", 2, Ui.textColor(this), Typeface.BOLD);
        meta = Ui.label(this, "", -2, Ui.muted(this), Typeface.NORMAL);
        texts.addView(title, new LinearLayout.LayoutParams(-1, 32));
        texts.addView(meta, new LinearLayout.LayoutParams(-1, 24));
        top.addView(texts, new LinearLayout.LayoutParams(0, 82, 1));
        root.addView(top, new LinearLayout.LayoutParams(-1, 88));

        stateIcon = new ImageView(this);
        stateIcon.setPadding(0, 6, 0, 2);
        root.addView(stateIcon, new LinearLayout.LayoutParams(46, 46));

        seek = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        seek.setMax(1000);
        root.addView(seek, new LinearLayout.LayoutParams(-1, 20));

        time = Ui.label(this, "", -1, Ui.textColor(this), Typeface.NORMAL);
        time.setGravity(Gravity.CENTER);
        speed = Ui.label(this, "", -2, Ui.muted(this), Typeface.NORMAL);
        speed.setGravity(Gravity.CENTER);
        root.addView(time, new LinearLayout.LayoutParams(-1, 28));
        root.addView(speed, new LinearLayout.LayoutParams(-1, 24));
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
            meta.setText("");
            time.setText("");
            speed.setText("");
            seek.setProgress(0);
            stateIcon.setImageResource(android.R.drawable.ic_media_pause);
            root.setBackgroundColor(Ui.bg(this));
            return;
        }
        int artColor = AlbumArtCache.color(this, track);
        GradientDrawable background = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Ui.blend(artColor, Ui.bg(this), 0.25f), Ui.bg(this)});
        root.setBackground(background);
        art.setImageBitmap(AlbumArtCache.get(this, track, 82));
        title.setText(track.title);
        meta.setText(track.artist + " / " + track.album);
        int position = PlaybackService.positionMs();
        int duration = Math.max(1, PlaybackService.durationMs());
        seek.setProgress(Math.max(0, Math.min(1000, position * 1000 / duration)));
        time.setText(Ui.mmss(position) + " / " + Ui.mmss(duration));
        speed.setText(PlaybackService.currentSpeed(this) + "x");
        stateIcon.setImageResource(PlaybackService.isPlaying() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
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
        if (keyCode == KeyEvent.KEYCODE_SOFT_LEFT || keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
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
        int multiplier = Math.min(12, 1 + repeatCount);
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
        dialog.show();
    }
}
