package com.papothepapo.exaltmusic;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class GroupAdapter extends BaseAdapter {
    private final Context context;
    private final List<MusicRepository.GroupItem> groups;

    public GroupAdapter(Context context, List<MusicRepository.GroupItem> groups) {
        this.context = context;
        this.groups = groups;
    }

    @Override
    public int getCount() {
        return groups.size();
    }

    @Override
    public MusicRepository.GroupItem getItem(int position) {
        return groups.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Row row;
        if (convertView == null) {
            row = new Row(context);
        } else {
            row = (Row) convertView;
        }
        row.bind(groups.get(position));
        return row;
    }

    private static class Row extends LinearLayout {
        private final ImageView art;
        private final TextView title;
        private final TextView subtitle;
        private final TextView count;

        Row(Context context) {
            super(context);
            setOrientation(HORIZONTAL);
            setGravity(Gravity.CENTER_VERTICAL);
            setPadding(8, 5, 8, 5);

            art = new ImageView(context);
            art.setScaleType(ImageView.ScaleType.CENTER_CROP);
            addView(art, new LinearLayout.LayoutParams(42, 42));

            LinearLayout texts = new LinearLayout(context);
            texts.setOrientation(VERTICAL);
            texts.setPadding(8, 0, 8, 0);
            title = Ui.label(context, "", 0, Ui.textColor(context), Typeface.BOLD);
            subtitle = Ui.label(context, "", -3, Ui.muted(context), Typeface.NORMAL);
            texts.addView(title, new LinearLayout.LayoutParams(-1, 22));
            texts.addView(subtitle, new LinearLayout.LayoutParams(-1, 18));
            addView(texts, new LinearLayout.LayoutParams(0, 46, 1));

            count = Ui.label(context, "", -1, Ui.accent(context), Typeface.BOLD);
            count.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            addView(count, new LinearLayout.LayoutParams(76, 42));
        }

        void bind(MusicRepository.GroupItem group) {
            setBackgroundColor(Ui.bg(getContext()));
            title.setText(group.label);
            subtitle.setText(group.subtitle);
            count.setText(group.trailing);
            if (group.sampleTrack != null) {
                art.setImageBitmap(AlbumArtCache.get(getContext(), group.sampleTrack, 42));
            } else {
                art.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }
    }
}
