/*#######################################################
 *
 *   Maintained 2017-2025 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package com.seafile.seadroid2.document.markor.util;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.seafile.seadroid2.document.markor.model.Document;
import com.seafile.seadroid2.document.opoc.util.GsContextUtils;

import java.io.File;

public class MarkorContextUtils extends GsContextUtils {

    public MarkorContextUtils(@Nullable final Context context) {
    }

    public static File getIntentFile(final Intent intent, final @Nullable Context context) {
        if (intent == null) {
            return null;
        }

        // By extra path
        File file = (File) intent.getSerializableExtra(Document.EXTRA_FILE);

        // By url in data
        if (file == null) {
            try {
                file = new File(intent.getData().getPath());
            } catch (NullPointerException ignored) {
            }
        }

        // By stream etc
        if (file == null && context != null) {
            file = GsContextUtils.extractFileFromIntent(intent, context);
        }

        return file;
    }

    @Override
    public void startActivity(final Context context, final Intent intent) {
        super.startActivity(context, intent);
    }
}
