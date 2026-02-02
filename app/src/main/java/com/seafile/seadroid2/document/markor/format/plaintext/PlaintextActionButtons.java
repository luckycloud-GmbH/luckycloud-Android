/*#######################################################
 *
 *   Maintained 2018-2025 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package com.seafile.seadroid2.document.markor.format.plaintext;

import android.content.Context;

import androidx.annotation.NonNull;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.document.markor.format.ActionButtonBase;
import com.seafile.seadroid2.document.markor.format.markdown.MarkdownReplacePatternGenerator;
import com.seafile.seadroid2.document.markor.frontend.textview.AutoTextFormatter;
import com.seafile.seadroid2.document.markor.model.Document;

import java.util.Arrays;
import java.util.List;

public class PlaintextActionButtons extends ActionButtonBase {

    public PlaintextActionButtons(@NonNull Context context, Document document) {
        super(context, document);
    }

    @Override
    public List<ActionItem> getFormatActionList() {
        return Arrays.asList(
                new ActionItem(abid_common_checkbox_list, R.drawable.ic_check_box_black_24dp, R.string.check_list),
                new ActionItem(abid_common_unordered_list_char, R.drawable.ic_list_black_24dp, R.string.unordered_list),
                new ActionItem(abid_common_ordered_list_number, R.drawable.ic_format_list_numbered_black_24dp, R.string.ordered_list),
                new ActionItem(abid_common_indent, R.drawable.ic_format_indent_increase_black_24dp, R.string.indent),
                new ActionItem(abid_common_deindent, R.drawable.ic_format_indent_decrease_black_24dp, R.string.deindent)
        );
    }

    @Override
    protected
    String getFormatActionsKey() {
        return "pref_key__plaintext__action_keys";
    }

    @Override
    protected void renumberOrderedList() {
        // Use markdown format for plain text too
        AutoTextFormatter.renumberOrderedList(_hlEditor.getText(), MarkdownReplacePatternGenerator.formatPatterns);
    }
}
