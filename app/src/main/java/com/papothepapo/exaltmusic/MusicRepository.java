package com.papothepapo.exaltmusic;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MusicRepository {
    public static final String GROUP_ARTIST = "artist";
    public static final String GROUP_ALBUM = "album";
    public static final String GROUP_FOLDER = "folder";

    private final Context context;

    public MusicRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    public List<Track> allTracks() {
        ArrayList<Track> tracks = new ArrayList<>();
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        Cursor cursor;
        try {
            cursor = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    null,
                    MediaStore.Audio.Media.TITLE + " COLLATE LOCALIZED ASC"
            );
        } catch (SecurityException ex) {
            return tracks;
        }
        if (cursor == null) {
            return tracks;
        }
        try {
            while (cursor.moveToNext()) {
                String path = getString(cursor, MediaStore.Audio.Media.DATA);
                String artist = getString(cursor, MediaStore.Audio.Media.ARTIST);
                Track track = new Track(
                        getLong(cursor, MediaStore.Audio.Media._ID),
                        path,
                        getLong(cursor, MediaStore.Audio.Media.DURATION),
                        getString(cursor, MediaStore.Audio.Media.TITLE),
                        artist,
                        getString(cursor, MediaStore.Audio.Media.ALBUM),
                        readAlbumArtist(path, artist)
                );
                TagStore.apply(context, track);
                tracks.add(track);
            }
        } finally {
            cursor.close();
        }
        sortTracks(tracks);
        return tracks;
    }

    public List<GroupItem> groups(String type) {
        LinkedHashMap<String, GroupItem> map = new LinkedHashMap<>();
        for (Track track : allTracks()) {
            String key = keyFor(track, type);
            String label = labelFor(track, type);
            GroupItem item = map.get(key);
            if (item == null) {
                item = new GroupItem(type, key, label, 0);
                map.put(key, item);
            }
            item.count++;
        }
        ArrayList<GroupItem> groups = new ArrayList<>(map.values());
        final Collator collator = Collator.getInstance(Locale.getDefault());
        Collections.sort(groups, new Comparator<GroupItem>() {
            @Override
            public int compare(GroupItem left, GroupItem right) {
                return collator.compare(left.label, right.label);
            }
        });
        return groups;
    }

    public List<Track> tracksFor(String type, String key) {
        ArrayList<Track> result = new ArrayList<>();
        for (Track track : allTracks()) {
            if (keyFor(track, type).equals(key)) {
                result.add(track);
            }
        }
        sortTracks(result);
        return result;
    }

    private static String keyFor(Track track, String type) {
        if (GROUP_ALBUM.equals(type)) {
            return track.album;
        }
        if (GROUP_FOLDER.equals(type)) {
            return track.folderKey();
        }
        return track.displayArtist();
    }

    private static String labelFor(Track track, String type) {
        if (GROUP_FOLDER.equals(type)) {
            String key = track.folderKey();
            if (key.length() == 0) {
                return "Unknown Folder";
            }
            File file = new File(key);
            return Track.clean(file.getName(), key);
        }
        return keyFor(track, type);
    }

    private static void sortTracks(List<Track> tracks) {
        final Collator collator = Collator.getInstance(Locale.getDefault());
        Collections.sort(tracks, new Comparator<Track>() {
            @Override
            public int compare(Track left, Track right) {
                int byAlbum = collator.compare(left.album, right.album);
                if (byAlbum != 0) {
                    return byAlbum;
                }
                return collator.compare(left.title, right.title);
            }
        });
    }

    private static String getString(Cursor cursor, String column) {
        int index = cursor.getColumnIndex(column);
        return index < 0 ? "" : cursor.getString(index);
    }

    private static long getLong(Cursor cursor, String column) {
        int index = cursor.getColumnIndex(column);
        return index < 0 ? 0L : cursor.getLong(index);
    }

    private static String readAlbumArtist(String path, String fallback) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(path);
            return Track.clean(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST), fallback);
        } catch (RuntimeException ex) {
            return fallback;
        } finally {
            try {
                retriever.release();
            } catch (Exception ignored) {
            }
        }
    }

    public static class GroupItem {
        public final String type;
        public final String key;
        public final String label;
        public int count;

        GroupItem(String type, String key, String label, int count) {
            this.type = type;
            this.key = key;
            this.label = label;
            this.count = count;
        }

        @Override
        public String toString() {
            return label + "  (" + count + ")";
        }
    }
}
