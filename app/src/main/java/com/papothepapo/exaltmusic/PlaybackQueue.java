package com.papothepapo.exaltmusic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PlaybackQueue {
    private static final ArrayList<Track> TRACKS = new ArrayList<>();

    private PlaybackQueue() {
    }

    public static synchronized void set(List<Track> tracks) {
        TRACKS.clear();
        TRACKS.addAll(tracks);
    }

    public static synchronized List<Track> all() {
        return new ArrayList<>(TRACKS);
    }

    public static synchronized Track get(int index) {
        if (index < 0 || index >= TRACKS.size()) {
            return null;
        }
        return TRACKS.get(index);
    }

    public static synchronized int size() {
        return TRACKS.size();
    }

    public static synchronized List<Track> readonly() {
        return Collections.unmodifiableList(new ArrayList<>(TRACKS));
    }
}
