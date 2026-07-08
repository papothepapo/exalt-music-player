package com.papothepapo.exaltmusic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.KeyEvent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public final class Ui {
    public static final int BG = Color.rgb(5, 7, 10);
    public static final int PANEL = Color.rgb(18, 24, 30);
    public static final int TEXT = Color.rgb(238, 244, 235);
    public static final int MUTED = Color.rgb(157, 170, 162);
    public static final int ACCENT = Color.rgb(168, 224, 99);

    private Ui() {
    }

    public static TextView text(Context context, String value, float extraSp, int styleColor) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextColor(styleColor);
        view.setTextSize(AppPrefs.fontSize(context) + extraSp);
        view.setTypeface(AppPrefs.typeface(context));
        view.setTextDirection(View.TEXT_DIRECTION_ANY_RTL);
        view.setSingleLine(false);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setPadding(8, 4, 8, 4);
        return view;
    }

    public static int bg(Context context) {
        String theme = AppPrefs.themeName(context);
        if ("midnight".equals(theme)) {
            return Color.rgb(7, 14, 25);
        }
        if ("olive".equals(theme)) {
            return Color.rgb(12, 19, 14);
        }
        if ("paper".equals(theme)) {
            return Color.rgb(238, 235, 226);
        }
        return BG;
    }

    public static int panel(Context context) {
        String theme = AppPrefs.themeName(context);
        if ("midnight".equals(theme)) {
            return Color.rgb(17, 31, 48);
        }
        if ("olive".equals(theme)) {
            return Color.rgb(24, 35, 24);
        }
        if ("paper".equals(theme)) {
            return Color.rgb(222, 218, 207);
        }
        return PANEL;
    }

    public static int textColor(Context context) {
        return "paper".equals(AppPrefs.themeName(context)) ? Color.rgb(25, 28, 25) : TEXT;
    }

    public static int muted(Context context) {
        return "paper".equals(AppPrefs.themeName(context)) ? Color.rgb(83, 91, 82) : MUTED;
    }

    public static int accent(Context context) {
        String theme = AppPrefs.themeName(context);
        if ("midnight".equals(theme)) {
            return Color.rgb(98, 197, 255);
        }
        if ("olive".equals(theme)) {
            return Color.rgb(176, 218, 115);
        }
        if ("paper".equals(theme)) {
            return Color.rgb(23, 116, 97);
        }
        return ACCENT;
    }

    public static <T> ArrayAdapter<T> adapter(final Context context, List<T> items) {
        return new ArrayAdapter<T>(context, android.R.layout.simple_list_item_1, items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(textColor(context));
                view.setTextSize(AppPrefs.fontSize(context));
                view.setTypeface(AppPrefs.typeface(context));
                view.setTextDirection(View.TEXT_DIRECTION_ANY_RTL);
                view.setPadding(10, 6, 10, 6);
                int selected = parent instanceof ListView ? ((ListView) parent).getSelectedItemPosition() : -1;
                view.setBackgroundColor(position == selected ? panel(context) : bg(context));
                return view;
            }
        };
    }

    public static void tuneList(Activity activity, ListView listView) {
        listView.setBackgroundColor(bg(activity));
        listView.setCacheColorHint(bg(activity));
        listView.setDividerHeight(1);
        listView.setFocusable(true);
        listView.setFocusableInTouchMode(true);
        listView.requestFocus();
        activity.getWindow().getDecorView().setBackgroundColor(bg(activity));
        listView.setFastScrollEnabled(false);
    }

    public static String mmss(int ms) {
        int total = Math.max(0, ms / 1000);
        int minutes = total / 60;
        int seconds = total % 60;
        return minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }

    public static boolean moveList(ListView listView, int keyCode, int repeatCount) {
        int direction;
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            direction = 1;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            direction = -1;
        } else {
            return false;
        }
        int count = listView.getCount();
        if (count == 0) {
            return true;
        }
        int current = listView.getSelectedItemPosition();
        if (current == AdapterView.INVALID_POSITION) {
            current = 0;
        }
        int step = Math.min(7, 1 + repeatCount / 2);
        int next = Math.max(0, Math.min(count - 1, current + direction * step));
        listView.setSelection(next);
        listView.smoothScrollToPosition(next);
        return true;
    }

    public static TextView label(Context context, String text, float extraSp, int color, int style) {
        TextView view = text(context, text, extraSp, color);
        view.setSingleLine(true);
        view.setTypeface(AppPrefs.typeface(context), style);
        return view;
    }

    public static GradientDrawable rounded(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    public static int blend(int color, int other, float ratio) {
        float inverse = 1f - ratio;
        return Color.rgb(
                Math.round(Color.red(color) * inverse + Color.red(other) * ratio),
                Math.round(Color.green(color) * inverse + Color.green(other) * ratio),
                Math.round(Color.blue(color) * inverse + Color.blue(other) * ratio)
        );
    }

    public static void openNowPlaying(Activity activity) {
        Intent intent = new Intent(activity, PlayerActivity.class);
        intent.putExtra("index", PlaybackService.currentIndex());
        intent.putExtra("autoplay", false);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public static boolean hasNowPlaying() {
        return PlaybackService.currentIndex() >= 0 && PlaybackQueue.size() > 0;
    }
}
