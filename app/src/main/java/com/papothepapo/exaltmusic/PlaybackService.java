package com.papothepapo.exaltmusic;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import java.io.IOException;

public class PlaybackService extends Service {
    public static final String ACTION_PLAY_INDEX = "com.papothepapo.exaltmusic.PLAY_INDEX";
    public static final String ACTION_TOGGLE = "com.papothepapo.exaltmusic.TOGGLE";
    public static final String ACTION_SEEK = "com.papothepapo.exaltmusic.SEEK";
    public static final String ACTION_NEXT = "com.papothepapo.exaltmusic.NEXT";
    public static final String ACTION_PREV = "com.papothepapo.exaltmusic.PREV";
    public static final String ACTION_STOP = "com.papothepapo.exaltmusic.STOP";
    public static final String ACTION_SPEED = "com.papothepapo.exaltmusic.SPEED";
    public static final String ACTION_STATE = "com.papothepapo.exaltmusic.STATE";

    private static final String CHANNEL = "playback";
    private static final int NOTIFICATION_ID = 7;
    private static PlaybackService instance;
    private MediaPlayer player;
    private int index = -1;
    private float speed = 1f;

    public static boolean isPlaying() {
        return instance != null && instance.player != null && instance.player.isPlaying();
    }

    public static int currentIndex() {
        return instance == null ? -1 : instance.index;
    }

    public static int positionMs() {
        if (instance == null || instance.player == null) {
            return 0;
        }
        try {
            return instance.player.getCurrentPosition();
        } catch (IllegalStateException ex) {
            return 0;
        }
    }

    public static int durationMs() {
        if (instance == null || instance.player == null) {
            Track track = PlaybackQueue.get(currentIndex());
            return track == null ? 0 : (int) track.durationMs;
        }
        try {
            return instance.player.getDuration();
        } catch (IllegalStateException ex) {
            return 0;
        }
    }

    public static float currentSpeed(Context context) {
        return instance == null ? AppPrefs.speed(context) : instance.speed;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        speed = AppPrefs.speed(this);
        createChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            return START_NOT_STICKY;
        }
        String action = intent.getAction();
        if (ACTION_PLAY_INDEX.equals(action)) {
            playIndex(intent.getIntExtra("index", 0));
        } else if (ACTION_TOGGLE.equals(action)) {
            toggle();
        } else if (ACTION_SEEK.equals(action)) {
            seekBy(intent.getIntExtra("delta", 0));
        } else if (ACTION_NEXT.equals(action)) {
            moveBy(1);
        } else if (ACTION_PREV.equals(action)) {
            moveBy(-1);
        } else if (ACTION_SPEED.equals(action)) {
            setSpeed(intent.getFloatExtra("speed", speed));
        } else if (ACTION_STOP.equals(action)) {
            stopPlayback();
            stopSelf();
        }
        broadcastState();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopPlayback();
        if (instance == this) {
            instance = null;
        }
        super.onDestroy();
    }

    private void playIndex(int newIndex) {
        Track track = PlaybackQueue.get(newIndex);
        if (track == null) {
            return;
        }
        releasePlayer();
        index = newIndex;
        player = new MediaPlayer();
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build());
        try {
            player.setDataSource(track.path);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    moveBy(1);
                }
            });
            player.prepare();
            applySpeed();
            player.start();
            startForeground(NOTIFICATION_ID, notification(track));
        } catch (IOException | IllegalStateException ex) {
            releasePlayer();
        }
    }

    private void toggle() {
        if (player == null) {
            if (PlaybackQueue.size() > 0) {
                playIndex(Math.max(0, index));
            }
            return;
        }
        if (player.isPlaying()) {
            player.pause();
            stopForeground(false);
        } else {
            applySpeed();
            player.start();
            Track track = PlaybackQueue.get(index);
            if (track != null) {
                startForeground(NOTIFICATION_ID, notification(track));
            }
        }
    }

    private void seekBy(int deltaMs) {
        if (player == null) {
            return;
        }
        int duration = Math.max(0, player.getDuration());
        int target = Math.max(0, Math.min(duration, player.getCurrentPosition() + deltaMs));
        player.seekTo(target);
    }

    private void moveBy(int delta) {
        int size = PlaybackQueue.size();
        if (size == 0) {
            return;
        }
        int next = (index + delta + size) % size;
        playIndex(next);
    }

    private void setSpeed(float value) {
        speed = Math.max(0.5f, Math.min(2.5f, value));
        AppPrefs.setSpeed(this, speed);
        applySpeed();
        Track track = PlaybackQueue.get(index);
        if (track != null && player != null && player.isPlaying()) {
            startForeground(NOTIFICATION_ID, notification(track));
        }
    }

    private void applySpeed() {
        if (player == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        try {
            PlaybackParams params = player.getPlaybackParams();
            params.setSpeed(speed);
            player.setPlaybackParams(params);
        } catch (IllegalStateException ex) {
            PlaybackParams params = new PlaybackParams();
            params.setSpeed(speed);
            player.setPlaybackParams(params);
        }
    }

    private void stopPlayback() {
        releasePlayer();
        stopForeground(true);
        broadcastState();
    }

    private void releasePlayer() {
        if (player != null) {
            try {
                player.stop();
            } catch (IllegalStateException ignored) {
            }
            player.release();
            player = null;
        }
    }

    private Notification notification(Track track) {
        Intent stopIntent = new Intent(this, PlaybackService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPending = PendingIntent.getService(this, 10, stopIntent, pendingFlags());

        Intent openIntent = new Intent(this, PlayerActivity.class);
        openIntent.putExtra("index", index);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openPending = PendingIntent.getActivity(this, 11, openIntent, pendingFlags());

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL)
                : new Notification.Builder(this);
        builder.setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(track.title)
                .setContentText(track.artist + "  " + speed + "x")
                .setContentIntent(openPending)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPending);
        return builder.build();
    }

    private int pendingFlags() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL, "Playback", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void broadcastState() {
        sendBroadcast(new Intent(ACTION_STATE));
    }
}
