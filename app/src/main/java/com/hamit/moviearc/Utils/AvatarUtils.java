package com.hamit.moviearc.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

public class AvatarUtils {

    public static void setAvatar(ImageView imageView, String name, @Nullable String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Load actual image using Glide/Picasso
            Glide.with(imageView.getContext())
                    .load(imageUrl)
                    .placeholder(createTextDrawable(imageView.getContext(), name))
                    .into(imageView);
        } else {
            // Use text avatar
            imageView.setImageDrawable(createTextDrawable(imageView.getContext(), name));
        }
    }

    private static Drawable createTextDrawable(Context context, String name) {
        String initials = getInitials(name);
        int color = getColorFromName(name);

        int size = 120; // Size in pixels
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Draw circle background
        Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(color);
        circlePaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(size/2f, size/2f, size/2f, circlePaint);

        // Draw text
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(24); // Font size
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        // Center text vertically and horizontally
        Rect textBounds = new Rect();
        textPaint.getTextBounds(initials, 0, initials.length(), textBounds);

        float textX = size / 2f;
        float textY = size / 2f - (textBounds.top + textBounds.bottom) / 2f;

        canvas.drawText(initials, textX, textY, textPaint);

        return new BitmapDrawable(context.getResources(), bitmap);
    }

    // ... rest of the methods remain the same
    private static String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "??";
        }

        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        } else {
            return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
    }

    private static int getColorFromName(String name) {
        // Generate consistent color based on name
        int hash = name.hashCode();
        int index = Math.abs(hash) % AVATAR_COLORS.length;
        return AVATAR_COLORS[index];
    }

    private static final int[] AVATAR_COLORS = {
            Color.parseColor("#FF6B6B"), Color.parseColor("#4ECDC4"),
            Color.parseColor("#45B7D1"), Color.parseColor("#96CEB4"),
            Color.parseColor("#FFEAA7"), Color.parseColor("#DDA0DD"),
            Color.parseColor("#98D8C8"), Color.parseColor("#F7DC6F")
    };
}