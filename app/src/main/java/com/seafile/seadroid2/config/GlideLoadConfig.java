package com.seafile.seadroid2.config;

import android.util.DisplayMetrics;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.account.SupportAccountManager;
import com.seafile.seadroid2.ui.WidgetUtils;

public class GlideLoadConfig {

    public static GlideUrl getGlideUrl(String url) {
        String token = SupportAccountManager.getInstance().getCurrentAccount().token;

        GlideUrl glideUrl = new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("Authorization", "Token " + token)
                .build());
        return glideUrl;
    }

    public static RequestOptions getOptions(String key, int columns) {
        if (key.isEmpty())
            return new RequestOptions()
                    .fallback(R.drawable.file_image)
                    .placeholder(R.drawable.file_image)
                    .override(WidgetUtils.getThumbnailWidth(columns), WidgetUtils.getThumbnailWidth(columns));
        return new RequestOptions()
                .fallback(R.drawable.file_image)
                .placeholder(R.drawable.file_image)
                .signature(new ObjectKey(key))
                .override(WidgetUtils.getThumbnailWidth(columns), WidgetUtils.getThumbnailWidth(columns));
    }

    public static RequestOptions getDefaultAvatarOptions(int columns) {
        return new RequestOptions()
                .placeholder(R.drawable.ic_personal)
                .override(WidgetUtils.getThumbnailWidth(columns), WidgetUtils.getThumbnailWidth(columns));
    }
}
