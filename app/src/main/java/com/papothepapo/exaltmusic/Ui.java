package com.papothepapo.exaltmusic;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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

    public static <T> ArrayAdapter<T> adapter(final Context context, List<T> items) {
        return new ArrayAdapter<T>(context, android.R.layout.simple_list_item_1, items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(TEXT);
                view.setTextSize(AppPrefs.fontSize(context));
                view.setTypeface(AppPrefs.typeface(context));
                view.setTextDirection(View.TEXT_DIRECTION_ANY_RTL);
                view.setPadding(10, 6, 10, 6);
                int selected = parent instanceof ListView ? ((ListView) parent).getSelectedItemPosition() : -1;
                view.setBackgroundColor(position == selected ? PANEL : BG);
                return view;
            }
        };
    }

    public static void tuneList(Activity activity, ListView listView) {
        listView.setBackgroundColor(BG);
        listView.setCacheColorHint(BG);
        listView.setDividerHeight(1);
        listView.setFocusable(true);
        listView.setFocusableInTouchMode(true);
        listView.requestFocus();
        activity.getWindow().getDecorView().setBackgroundColor(BG);
    }

    public static String mmss(int ms) {
        int total = Math.max(0, ms / 1000);
        int minutes = total / 60;
        int seconds = total % 60;
        return minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }
}
