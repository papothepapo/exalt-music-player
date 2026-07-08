package com.papothepapo.exaltmusic;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;

public final class AppPrefs {
    private static final String NAME = "exalt_music_prefs";
    private static final String SKIP_FORWARD = "skip_forward";
    private static final String SKIP_BACK = "skip_back";
    private static final String FONT_SIZE = "font_size";
    private static final String FONT_NAME = "font_name";
    private static final String THEME_NAME = "theme_name";
    private static final String SPEED = "speed";

    private AppPrefs() {
    }

    public static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public static int skipForward(Context context) {
        return prefs(context).getInt(SKIP_FORWARD, 10);
    }

    public static int skipBack(Context context) {
        return prefs(context).getInt(SKIP_BACK, 10);
    }

    public static float fontSize(Context context) {
        return prefs(context).getFloat(FONT_SIZE, 16f);
    }

    public static String fontName(Context context) {
        return prefs(context).getString(FONT_NAME, "sans-serif");
    }

    public static String themeName(Context context) {
        return prefs(context).getString(THEME_NAME, "charcoal");
    }

    public static float speed(Context context) {
        return prefs(context).getFloat(SPEED, 1f);
    }

    public static void setSkipForward(Context context, int value) {
        prefs(context).edit().putInt(SKIP_FORWARD, value).apply();
    }

    public static void setSkipBack(Context context, int value) {
        prefs(context).edit().putInt(SKIP_BACK, value).apply();
    }

    public static void setFontSize(Context context, float value) {
        prefs(context).edit().putFloat(FONT_SIZE, value).apply();
    }

    public static void setFontName(Context context, String value) {
        prefs(context).edit().putString(FONT_NAME, value).apply();
    }

    public static void setThemeName(Context context, String value) {
        prefs(context).edit().putString(THEME_NAME, value).apply();
    }

    public static void setSpeed(Context context, float value) {
        prefs(context).edit().putFloat(SPEED, value).apply();
    }

    public static Typeface typeface(Context context) {
        String name = fontName(context);
        if ("serif".equals(name)) {
            return Typeface.SERIF;
        }
        if ("monospace".equals(name)) {
            return Typeface.MONOSPACE;
        }
        return Typeface.create(name, Typeface.NORMAL);
    }
}
