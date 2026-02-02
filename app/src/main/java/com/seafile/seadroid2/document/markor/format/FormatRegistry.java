/*#######################################################
 *
 *   Maintained 2018-2025 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package com.seafile.seadroid2.document.markor.format;

import android.content.Context;
import android.text.InputFilter;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.seafile.seadroid2.document.markor.format.csv.CsvSyntaxHighlighter;
import com.seafile.seadroid2.document.markor.format.csv.CsvTextConverter;
import com.seafile.seadroid2.document.markor.format.keyvalue.KeyValueSyntaxHighlighter;
import com.seafile.seadroid2.document.markor.format.keyvalue.KeyValueTextConverter;
import com.seafile.seadroid2.document.markor.format.markdown.MarkdownActionButtons;
import com.seafile.seadroid2.document.markor.format.markdown.MarkdownReplacePatternGenerator;
import com.seafile.seadroid2.document.markor.format.markdown.MarkdownSyntaxHighlighter;
import com.seafile.seadroid2.document.markor.format.markdown.MarkdownTextConverter;
import com.seafile.seadroid2.document.markor.format.plaintext.PlaintextActionButtons;
import com.seafile.seadroid2.document.markor.format.plaintext.PlaintextSyntaxHighlighter;
import com.seafile.seadroid2.document.markor.format.plaintext.PlaintextTextConverter;
import com.seafile.seadroid2.document.markor.frontend.textview.AutoTextFormatter;
import com.seafile.seadroid2.document.markor.frontend.textview.ListHandler;
import com.seafile.seadroid2.document.markor.frontend.textview.SyntaxHighlighterBase;
import com.seafile.seadroid2.document.markor.model.AppSettings;
import com.seafile.seadroid2.document.markor.model.Document;
import com.seafile.seadroid2.document.opoc.util.GsFileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FormatRegistry {
    public static final int FORMAT_UNKNOWN = 0;
    public static final int FORMAT_MARKDOWN = 1;
    public static final int FORMAT_CSV = 2;
    public static final int FORMAT_PLAIN =3;
    public static final int FORMAT_KEYVALUE = 4;

    public final static MarkdownTextConverter CONVERTER_MARKDOWN = new MarkdownTextConverter();
    public final static KeyValueTextConverter CONVERTER_KEYVALUE = new KeyValueTextConverter();
    public final static CsvTextConverter CONVERTER_CSV = new CsvTextConverter();
    public final static PlaintextTextConverter CONVERTER_PLAINTEXT = new PlaintextTextConverter();

    // File extensions that are known not to be supported by Markor
    private static final List<String> EXTERNAL_FILE_EXTENSIONS = Collections.singletonList(".pdf");

    public static class Format {
        public final @StringRes int format, name;
        public final String defaultExtensionWithDot;
        public final TextConverterBase converter;

        public Format(@StringRes final int a_format, @StringRes final int a_name, final String a_defaultFileExtension, final TextConverterBase a_converter) {
            format = a_format;
            name = a_name;
            defaultExtensionWithDot = a_defaultFileExtension;
            converter = a_converter;
        }
    }

    // Order here is used to **determine** format by it's file extension and/or content heading
    public static final List<Format> FORMATS = Arrays.asList(
            new Format(FormatRegistry.FORMAT_MARKDOWN, 0, ".md", CONVERTER_MARKDOWN),
            new Format(FormatRegistry.FORMAT_CSV, 0, ".csv", CONVERTER_CSV),
            new Format(FormatRegistry.FORMAT_KEYVALUE, 0, ".json", CONVERTER_KEYVALUE),
            new Format(FormatRegistry.FORMAT_PLAIN, 0, ".txt", CONVERTER_PLAINTEXT),
            new Format(FormatRegistry.FORMAT_UNKNOWN, 0, "", null)
    );

    public static int getFormatByExtension(String extension) {
        for (Format format : FORMATS) {
            if (format.defaultExtensionWithDot.contains(extension)) {
                return format.format;
            }
        }
        return FormatRegistry.FORMAT_UNKNOWN;
    }

    public static boolean isFileSupported(final File file, final boolean... textOnly) {
        if (file != null) {
            for (final Format format : FORMATS) {
                if (format.converter != null && format.converter.isFileOutOfThisFormat(file)) {
                    return true;
                }
            }
        }
        return false;
    }

    public interface TextFormatApplier {
        void applyTextFormat(int textFormatId);
    }

    public static FormatRegistry getFormat(int formatId, @NonNull final Context context, final Document document) {
        final FormatRegistry format = new FormatRegistry();
        final AppSettings appSettings = AppSettings.get(context);

        switch (formatId) {
            case FORMAT_CSV: {
                format._converter = CONVERTER_CSV;
                format._highlighter = new CsvSyntaxHighlighter(appSettings);

                // TODO k3b ????
                format._textActions = new PlaintextActionButtons(context, document);
                format._autoFormatInputFilter = new AutoTextFormatter(MarkdownReplacePatternGenerator.formatPatterns);
                format._autoFormatTextWatcher = new ListHandler(MarkdownReplacePatternGenerator.formatPatterns);
                break;
            }
            case FORMAT_PLAIN: {
                format._converter = CONVERTER_PLAINTEXT;
                format._highlighter = new PlaintextSyntaxHighlighter(appSettings, document.extension);
                // Should implement code action buttons for PlaintextActionButtons
                format._textActions = new PlaintextActionButtons(context, document);
                format._autoFormatInputFilter = new AutoTextFormatter(MarkdownReplacePatternGenerator.formatPatterns);
                format._autoFormatTextWatcher = new ListHandler(MarkdownReplacePatternGenerator.formatPatterns);
                break;
            }
            case FORMAT_KEYVALUE: {
                format._converter = CONVERTER_KEYVALUE;
                format._highlighter = new KeyValueSyntaxHighlighter(appSettings);
                format._textActions = new PlaintextActionButtons(context, document);
                break;
            }
            default:
            case FORMAT_MARKDOWN: {
                formatId = FORMAT_MARKDOWN;
                format._converter = CONVERTER_MARKDOWN;
                format._highlighter = new MarkdownSyntaxHighlighter(appSettings);
                format._textActions = new MarkdownActionButtons(context, document);
                format._autoFormatInputFilter = new AutoTextFormatter(MarkdownReplacePatternGenerator.formatPatterns);
                format._autoFormatTextWatcher = new ListHandler(MarkdownReplacePatternGenerator.formatPatterns);
                break;
            }
        }
        format._formatId = formatId;
        return format;
    }

    private ActionButtonBase _textActions;
    private SyntaxHighlighterBase _highlighter;
    private TextConverterBase _converter;
    private InputFilter _autoFormatInputFilter;
    private TextWatcher _autoFormatTextWatcher;
    private int _formatId;

    public ActionButtonBase getActions() {
        return _textActions;
    }

    public TextWatcher getAutoFormatTextWatcher() {
        return _autoFormatTextWatcher;
    }

    public InputFilter getAutoFormatInputFilter() {
        return _autoFormatInputFilter;
    }

    public SyntaxHighlighterBase getHighlighter() {
        return _highlighter;
    }

    public TextConverterBase getConverter() {
        return _converter;
    }

    public int getFormatId() {
        return _formatId;
    }

    public static boolean isExternalFile(final File file) {
        final String ext = GsFileUtils.getFilenameExtension(file).toLowerCase();
        return EXTERNAL_FILE_EXTENSIONS.contains(ext);
    }
}
