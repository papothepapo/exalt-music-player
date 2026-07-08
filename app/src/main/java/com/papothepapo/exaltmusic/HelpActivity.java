package com.papothepapo.exaltmusic;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HelpActivity extends Activity {
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(8, 6, 8, 6);
        root.setBackgroundColor(Ui.bg(this));
        root.addView(Ui.label(this, "Help", 3, Ui.accent(this), Typeface.BOLD), new LinearLayout.LayoutParams(-1, 40));
        TextView body = Ui.text(this,
                "Up/Down moves through lists.\nLeft/Right switches tabs or folder panes.\nCenter opens, plays, or pauses.\nRight soft/Menu opens player speed or settings.\nLeft soft returns or opens Now Playing from the library.\nHold a group or track to edit tags.\nOn Now Playing, Left/Right seeks while playing and changes tracks while paused.",
                -1,
                Ui.textColor(this));
        root.addView(body, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_SOFT_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
