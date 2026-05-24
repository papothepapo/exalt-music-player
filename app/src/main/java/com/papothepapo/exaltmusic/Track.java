package com.papothepapo.exaltmusic;

import java.io.File;
import java.io.Serializable;

public class Track implements Serializable {
    public final long id;
    public final String path;
    public final long durationMs;
    public String title;
    public String artist;
    public String album;
    public String albumArtist;

    public Track(long id, String path, long durationMs, String title, String artist, String album, String albumArtist) {
        this.id = id;
        this.path = path == null ? "" : path;
        this.durationMs = durationMs;
        this.title = clean(title, new File(this.path).getName());
        this.artist = clean(artist, "Unknown Artist");
        this.album = clean(album, "Unknown Album");
        this.albumArtist = clean(albumArtist, this.artist);
    }

    public String folderKey() {
        File parent = new File(path).getParentFile();
        return parent == null ? "" : parent.getAbsolutePath();
    }

    public String displayArtist() {
        return clean(albumArtist, artist);
    }

    public static String clean(String value, String fallback) {
        if (value == null) {
            return fallback == null ? "" : fallback;
        }
        String trimmed = value.trim();
        if (trimmed.length() == 0 || "<unknown>".equalsIgnoreCase(trimmed)) {
            return fallback == null ? "" : fallback;
        }
        return trimmed;
    }
}
