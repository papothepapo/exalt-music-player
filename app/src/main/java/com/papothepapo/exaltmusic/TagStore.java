package com.papothepapo.exaltmusic;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;

public final class TagStore {
    private static final String NAME = "exalt_music_tags";

    private TagStore() {
    }

    static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public static void apply(Context context, Track track) {
        SharedPreferences p = prefs(context);
        String prefix = "track:" + track.path + ":";
        track.title = p.getString(prefix + "title", track.title);
        track.artist = p.getString(prefix + "artist", track.artist);
        track.album = p.getString(prefix + "album", track.album);
        track.albumArtist = p.getString(prefix + "albumArtist", track.albumArtist);
    }

    public static void saveTrack(Context context, Track track, String title, String artist, String album, String albumArtist) {
        SharedPreferences.Editor editor = prefs(context).edit();
        String prefix = "track:" + track.path + ":";
        putIfPresent(editor, prefix + "title", title);
        putIfPresent(editor, prefix + "artist", artist);
        putIfPresent(editor, prefix + "album", album);
        putIfPresent(editor, prefix + "albumArtist", albumArtist);
        editor.apply();
    }

    public static void saveBulk(Context context, List<Track> tracks, String artist, String album, String albumArtist) {
        SharedPreferences.Editor editor = prefs(context).edit();
        for (Track track : tracks) {
            String prefix = "track:" + track.path + ":";
            putIfPresent(editor, prefix + "artist", artist);
            putIfPresent(editor, prefix + "album", album);
            putIfPresent(editor, prefix + "albumArtist", albumArtist);
        }
        editor.apply();
    }

    private static void putIfPresent(SharedPreferences.Editor editor, String key, String value) {
        if (value != null && value.trim().length() > 0) {
            editor.putString(key, value.trim());
        }
    }
}
