package com.papothepapo.exaltmusic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaMetadataRetriever;
import android.util.LruCache;

public final class AlbumArtCache {
    private static final LruCache<String, Bitmap> CACHE = new LruCache<>(12);

    private AlbumArtCache() {
    }

    public static Bitmap get(Context context, Track track, int sizePx) {
        String key = track.album + "|" + track.artist + "|" + track.path + "|" + sizePx;
        Bitmap cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        Bitmap bitmap = embedded(track.path, sizePx);
        if (bitmap == null) {
            bitmap = placeholder(context, track, sizePx);
        }
        CACHE.put(key, bitmap);
        return bitmap;
    }

    public static int color(Context context, Track track) {
        Bitmap bitmap = get(context, track, 48);
        long red = 0;
        long green = 0;
        long blue = 0;
        int samples = 0;
        int step = Math.max(1, bitmap.getWidth() / 8);
        for (int y = 0; y < bitmap.getHeight(); y += step) {
            for (int x = 0; x < bitmap.getWidth(); x += step) {
                int pixel = bitmap.getPixel(x, y);
                red += Color.red(pixel);
                green += Color.green(pixel);
                blue += Color.blue(pixel);
                samples++;
            }
        }
        if (samples == 0) {
            return Ui.accent(context);
        }
        return Color.rgb((int) (red / samples), (int) (green / samples), (int) (blue / samples));
    }

    private static Bitmap embedded(String path, int sizePx) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(path);
            byte[] bytes = retriever.getEmbeddedPicture();
            if (bytes == null) {
                return null;
            }
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bounds);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize(bounds, sizePx);
            Bitmap decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            if (decoded == null) {
                return null;
            }
            return Bitmap.createScaledBitmap(decoded, sizePx, sizePx, true);
        } catch (RuntimeException ex) {
            return null;
        } finally {
            try {
                retriever.release();
            } catch (Exception ignored) {
            }
        }
    }

    private static int sampleSize(BitmapFactory.Options options, int target) {
        int sample = 1;
        int height = options.outHeight;
        int width = options.outWidth;
        while (height / sample > target * 2 || width / sample > target * 2) {
            sample *= 2;
        }
        return sample;
    }

    private static Bitmap placeholder(Context context, Track track, int sizePx) {
        Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int accent = Ui.accent(context);
        paint.setColor(Ui.blend(accent, Ui.bg(context), 0.35f));
        canvas.drawRect(0, 0, sizePx, sizePx, paint);
        paint.setColor(Ui.blend(accent, Color.WHITE, 0.35f));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(sizePx * 0.38f);
        String letter = track.album.length() > 0 ? track.album.substring(0, 1).toUpperCase() : "?";
        canvas.drawText(letter, sizePx / 2f, sizePx * 0.62f, paint);
        return bitmap;
    }
}
