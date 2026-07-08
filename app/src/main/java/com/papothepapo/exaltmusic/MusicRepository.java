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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
        Cursor cursor;
        try {
            cursor = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    MediaStore.Audio.Media.IS_MUSIC + "!=0",
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
        LinkedHashMap<String, Set<String>> albumCounts = new LinkedHashMap<>();
        for (Track track : allTracks()) {
            String key = keyFor(track, type);
            String label = labelFor(track, type);
            GroupItem item = map.get(key);
            if (item == null) {
                item = new GroupItem(type, key, label, "", "", 0, track);
                map.put(key, item);
                albumCounts.put(key, new HashSet<String>());
            }
            item.count++;
            albumCounts.get(key).add(track.album);
        }
        for (Map.Entry<String, GroupItem> entry : map.entrySet()) {
            GroupItem item = entry.getValue();
            int albums = albumCounts.get(entry.getKey()).size();
            if (GROUP_ARTIST.equals(type)) {
                item.subtitle = item.count + (item.count == 1 ? " song" : " songs");
                item.trailing = albums + (albums == 1 ? " album" : " albums");
            } else if (GROUP_ALBUM.equals(type)) {
                item.subtitle = item.sampleTrack == null ? "" : item.sampleTrack.displayArtist();
                item.trailing = item.count + (item.count == 1 ? " song" : " songs");
            } else {
                item.subtitle = item.key;
                item.trailing = item.count + (item.count == 1 ? " song" : " songs");
            }
        }
        ArrayList<GroupItem> groups = new ArrayList<>(map.values());
        sortGroups(groups);
        return groups;
    }

    public List<GroupItem> folderChildren(String parent) {
        List<Track> tracks = allTracks();
        String base = parent == null ? commonFolderParent(tracks) : parent;
        LinkedHashMap<String, GroupItem> map = new LinkedHashMap<>();
        for (Track track : tracks) {
            String child = immediateChild(base, track.folderKey());
            if (child == null) {
                continue;
            }
            GroupItem item = map.get(child);
            if (item == null) {
                item = new GroupItem(GROUP_FOLDER, child, folderLabel(child), child, "", 0, track);
                map.put(child, item);
            }
            item.count++;
        }
        ArrayList<GroupItem> folders = new ArrayList<>(map.values());
        for (GroupItem item : folders) {
            int subfolders = folderChildrenCount(item.key, tracks);
            item.trailing = subfolders > 0 ? subfolders + (subfolders == 1 ? " folder" : " folders")
                    : item.count + (item.count == 1 ? " song" : " songs");
        }
        sortGroups(folders);
        return folders;
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

    public List<Track> tracksInFolder(String folder) {
        ArrayList<Track> result = new ArrayList<>();
        for (Track track : allTracks()) {
            if (track.folderKey().equals(folder)) {
                result.add(track);
            }
        }
        sortTracks(result);
        return result;
    }

    public List<Track> tracksUnderFolder(String folder) {
        ArrayList<Track> result = new ArrayList<>();
        for (Track track : allTracks()) {
            String key = track.folderKey();
            if (key.equals(folder) || key.startsWith(folder + File.separator)) {
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
            return folderLabel(track.folderKey());
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

    private static void sortGroups(List<GroupItem> groups) {
        final Collator collator = Collator.getInstance(Locale.getDefault());
        Collections.sort(groups, new Comparator<GroupItem>() {
            @Override
            public int compare(GroupItem left, GroupItem right) {
                return collator.compare(left.label, right.label);
            }
        });
    }

    private static String folderLabel(String path) {
        if (path == null || path.length() == 0) {
            return "Unknown Folder";
        }
        File file = new File(path);
        return Track.clean(file.getName(), path);
    }

    private static String commonFolderParent(List<Track> tracks) {
        String common = null;
        for (Track track : tracks) {
            String folder = track.folderKey();
            if (folder.length() == 0) {
                continue;
            }
            common = common == null ? folder : commonPrefixFolder(common, folder);
        }
        if (common == null || common.length() == 0) {
            return "";
        }
        File parent = new File(common).getParentFile();
        return parent == null ? common : parent.getAbsolutePath();
    }

    private static String commonPrefixFolder(String left, String right) {
        String separator = File.separator;
        String[] a = left.split("[/\\\\]+");
        String[] b = right.split("[/\\\\]+");
        int max = Math.min(a.length, b.length);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < max; i++) {
            if (!a[i].equals(b[i])) {
                break;
            }
            if (a[i].length() == 0) {
                builder.append(separator);
            } else {
                if (builder.length() > 1) {
                    builder.append(separator);
                }
                builder.append(a[i]);
            }
        }
        return builder.toString();
    }

    private static String immediateChild(String parent, String folder) {
        if (folder == null || folder.length() == 0 || parent == null || folder.equals(parent)) {
            return null;
        }
        String prefix = parent.length() == 0 || parent.endsWith(File.separator) ? parent : parent + File.separator;
        if (!folder.startsWith(prefix)) {
            return null;
        }
        String rest = folder.substring(prefix.length());
        if (rest.length() == 0) {
            return null;
        }
        int slash = rest.indexOf(File.separator);
        String childName = slash < 0 ? rest : rest.substring(0, slash);
        return prefix + childName;
    }

    private static int folderChildrenCount(String folder, List<Track> tracks) {
        HashSet<String> children = new HashSet<>();
        for (Track track : tracks) {
            String child = immediateChild(folder, track.folderKey());
            if (child != null) {
                children.add(child);
            }
        }
        return children.size();
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
        public String subtitle;
        public String trailing;
        public int count;
        public final Track sampleTrack;

        GroupItem(String type, String key, String label, String subtitle, String trailing, int count, Track sampleTrack) {
            this.type = type;
            this.key = key;
            this.label = label;
            this.subtitle = subtitle;
            this.trailing = trailing;
            this.count = count;
            this.sampleTrack = sampleTrack;
        }

        @Override
        public String toString() {
            return label + "  " + trailing;
        }
    }
}
