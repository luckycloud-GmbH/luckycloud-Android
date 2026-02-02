/*#######################################################
 *
 *   Maintained 2017-2025 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package com.seafile.seadroid2.document.markor.format;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.TooltipCompat;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.document.markor.frontend.textview.HighlightingEditor;
import com.seafile.seadroid2.document.markor.frontend.textview.TextViewUtils;
import com.seafile.seadroid2.document.markor.model.AppSettings;
import com.seafile.seadroid2.document.markor.model.Document;
import com.seafile.seadroid2.document.markor.util.MarkorContextUtils;
import com.seafile.seadroid2.document.opoc.format.GsTextUtils;
import com.seafile.seadroid2.document.opoc.util.GsCollectionUtils;
import com.seafile.seadroid2.document.opoc.util.GsContextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public abstract class ActionButtonBase {

    public static final int abid_common_delete_lines = 0;
    public static final int abid_common_duplicate_lines = 1;
    public static final int abid_common_new_line_below = 2;
    public static final int abid_common_move_text_one_line_up = 3;
    public static final int abid_common_move_text_one_line_down = 4;
    public static final int abid_common_web_jump_to_very_top_or_bottom = 5;

    public static final int abid_common_checkbox_list = 8;
    public static final int abid_common_unordered_list_char = 9;
    public static final int abid_common_ordered_list_number = 10;
    public static final int abid_markdown_bold = 11;
    public static final int abid_markdown_italic = 12;
    public static final int abid_markdown_strikeout = 13;
    public static final int abid_markdown_code_inline = 14;
    public static final int abid_markdown_quote = 15;
    public static final int abid_markdown_h1 = 16;
    public static final int abid_markdown_h2 = 17;
    public static final int abid_markdown_h3 = 18;
    public static final int abid_markdown_horizontal_line = 19;
    public static final int abid_common_indent = 20;
    public static final int abid_common_deindent = 21;

    private Activity _activity;
    private MarkorContextUtils _cu;
    private final int _buttonHorizontalMargin;

    protected HighlightingEditor _hlEditor;
    protected WebView _webView;
    protected Document _document;
    protected AppSettings _appSettings;
    protected int _indent;

    public static final String ACTION_ORDER_PREF_NAME = "action_order";
    private static final String ORDER_SUFFIX = "_order";
    private static final String DISABLED_SUFFIX = "_disabled";

    public ActionButtonBase(@NonNull final Context context, final Document document) {
        _document = document;
        _appSettings = AppSettings.get(context);
        _buttonHorizontalMargin = GsContextUtils.instance.convertDpToPx(context, 6);
        _indent = 4;
    }

    // Override to implement custom onClick
    public boolean onActionClick(final @StringRes int action) {
        return runCommonAction(action);
    }

    // Override to implement custom onLongClick
    public boolean onActionLongClick(final @StringRes int action) {
        return runCommonLongPressAction(action);
    }

    // Override to implement custom title action
    public boolean runTitleClick() {
        return false;
    }

    /**
     * Derived classes must return a unique StringRes id.
     * This is used to extract the appropriate action order preference.
     *
     * @return StringRes preference key
     */
    protected abstract String getFormatActionsKey();

    /**
     * Derived classes must return a List of ActionItem. One for each action they want to implement.
     *
     * @return List of ActionItems
     */
    protected abstract List<ActionItem> getFormatActionList();

    /**
     * These will not be added to the actions list.
     *
     * @return List of keyId strings.
     */
    public List<String> getDisabledActions() {
        return loadActionPreference(DISABLED_SUFFIX);
    }

    /**
     * Map every string Action identifier -> ActionItem
     *
     * @return Map of String key -> Action
     */
    public Map<String, ActionItem> getActiveActionMap() {
        final List<ActionItem> actionList = getActionList();
        final List<String> keyList = getActiveActionKeys();

        final Map<String, ActionItem> map = new HashMap<>();
        for (int i = 0; i < actionList.size(); i++) {
            map.put(keyList.get(i), actionList.get(i));
        }
        return map;
    }

    /**
     * Get a combined action list - from derived format and the base actions
     */
    private List<ActionItem> getActionList() {
        final List<ActionItem> commonActions = Arrays.asList(
                new ActionItem(abid_common_delete_lines, R.drawable.ic_delete_black_24dp, R.string.delete_lines),
                new ActionItem(abid_common_duplicate_lines, R.drawable.ic_duplicate_lines_black_24dp, R.string.duplicate_lines),
                new ActionItem(abid_common_new_line_below, R.drawable.ic_baseline_keyboard_return_24, R.string.start_new_line_below),
                new ActionItem(abid_common_move_text_one_line_up, R.drawable.ic_baseline_arrow_upward_24, R.string.move_text_one_line_up).setRepeatable(true),
                new ActionItem(abid_common_move_text_one_line_down, R.drawable.ic_baseline_arrow_downward_24, R.string.move_text_one_line_down).setRepeatable(true),
                new ActionItem(abid_common_web_jump_to_very_top_or_bottom, R.drawable.ic_vertical_align_center_black_24dp, R.string.jump_to_bottom).setDisplayMode(ActionItem.DisplayMode.VIEW)
        );

        // Order is enforced separately
        final Map<Integer, ActionItem> unique = new HashMap<>();

        for (final ActionItem item : commonActions) {
            unique.put(item.keyId, item);
        }

        // Actions in the derived class override common actions if they share the same keyId
        for (final ActionItem item : getFormatActionList()) {
            unique.put(item.keyId, item);
        }

        return new ArrayList<>(unique.values());
    }

    /**
     * Get string for every ActionItem.keyId defined by getActiveActionList
     *
     * @return List or resource strings
     */
    public List<String> getActiveActionKeys() {
        return GsCollectionUtils.map(getActionList(), item -> String.valueOf(item.keyId));
    }

    /**
     * Save an action order to preferences.
     * The Preference is derived from the key returned by getFormatActionsKey
     * <p>
     * Keys are joined into a comma separated list before saving.
     *
     * @param keys of keys (in order) to save
     */
    public void saveDisabledActions(final Collection<String> keys) {
        saveActionPreference(DISABLED_SUFFIX, keys);
    }

    /**
     * Save an action order to preferences.
     * The Preference is derived from the key returned by getFormatActionsKey
     * <p>
     * Keys are joined into a comma separated list before saving.
     *
     * @param keys of keys (in order) to save
     */
    public void saveActionOrder(final Collection<String> keys) {
        saveActionPreference(ORDER_SUFFIX, keys);
    }

    private void saveActionPreference(final String suffix, final Collection<String> values) {
        final SharedPreferences settings = getContext().getSharedPreferences(ACTION_ORDER_PREF_NAME, Context.MODE_PRIVATE);
        final String formatKey = getFormatActionsKey() + suffix;
        settings.edit().putString(formatKey, TextUtils.join(",", values)).apply();
    }

    private List<String> loadActionPreference(final String suffix) {
        String formatKey = getFormatActionsKey() + suffix;
        SharedPreferences settings = getContext().getSharedPreferences(ACTION_ORDER_PREF_NAME, Context.MODE_PRIVATE);
        String combinedKeys = settings.getString(formatKey, null);
        return combinedKeys != null ? Arrays.asList(combinedKeys.split(",")) : Collections.emptyList();
    }

    /**
     * Get the ordered list of preference keys.
     * <p>
     * This routine does the following:
     * 1. Extract list of currently defined actions
     * 2. Extract saved action-order-list (Comma separated) from preferences
     * 3. Split action order list into list of action keys
     * 4. Remove action keys which are no longer present in currently defined actions from the preference list
     * 5. Add new actions which are not in the preference list to the preference list
     * 6. If changes were made (i.e. actions have been added or removed), re-save the preference list
     *
     * @return List of Action Item keys in order specified by preferences
     */
    public List<String> getActionOrder() {
        final Set<String> order = new LinkedHashSet<>(loadActionPreference(ORDER_SUFFIX));

        final Set<String> defined = new LinkedHashSet<>(getActiveActionKeys());
        final Set<String> disabled = new LinkedHashSet<>(getDisabledActions());

        // Any definedKeys which are not in prefs or disabled keys are added to disabled
        final Set<String> existing = GsCollectionUtils.union(order, disabled);
        final Set<String> added = GsCollectionUtils.setDiff(defined, existing);
        final Set<String> removed = GsCollectionUtils.setDiff(existing, defined);

        // NOTE: suppressing this for increased discoverability
        // Disable any new actions unless none existing (i.e. first run)
        // if (!existing.isEmpty()) {
        //     disabled.addAll(added);
        // }

        // Add new ones to order
        order.addAll(added);

        // Removed removed from order and disabled
        disabled.removeAll(removed);
        order.removeAll(removed);

        if (!added.isEmpty() || !removed.isEmpty()) {
            saveActionOrder(order);
        }

        return new ArrayList<>(order);
    }

    @SuppressWarnings("ConstantConditions")
    public void recreateActionButtons(final ViewGroup barLayout, final ActionItem.DisplayMode displayMode) {
        barLayout.removeAllViews();
        final Map<String, ActionItem> map = getActiveActionMap();
        final List<String> orderedKeys = getActionOrder();
        final Set<String> disabledKeys = new HashSet<>(getDisabledActions());

        for (final String key : orderedKeys) {
            final ActionItem action = map.get(key);
            if (!disabledKeys.contains(key) && (action.displayMode == displayMode || action.displayMode == ActionItem.DisplayMode.ANY)) {
                appendActionButtonToBar(barLayout, action);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupRepeat(final View btn) {
        // Velocity and acceleration parameters
        final int INITIAL_DELAY = 300, DELTA_DELAY = 100, MIN_DELAY = 100;
        final Handler handler = new Handler();

        final Runnable repeater = new Runnable() {
            int delay = INITIAL_DELAY;

            @Override
            public void run() {
                btn.callOnClick();
                delay = Math.max(MIN_DELAY, delay - DELTA_DELAY);
                handler.postDelayed(this, delay);
            }
        };

        btn.setOnLongClickListener(v -> {
            btn.callOnClick(); // Trigger immediately
            handler.postDelayed(repeater, INITIAL_DELAY);
            return true;
        });

        btn.setOnTouchListener((view, event) -> {
            final int eac = event.getAction();
            if (eac == MotionEvent.ACTION_UP || eac == MotionEvent.ACTION_CANCEL) {
                handler.removeCallbacksAndMessages(null);
            }
            return false;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void appendActionButtonToBar(final ViewGroup barLayout, final @NonNull ActionItem action) {
        final ImageView btn = (ImageView) _activity.getLayoutInflater().inflate(R.layout.quick_keyboard_button, null);
        btn.setImageResource(action.iconId);
        final String desc = rstr(action.stringId);
        btn.setContentDescription(desc);
        TooltipCompat.setTooltipText(btn, desc);

        btn.setOnClickListener(v -> {
            try {
                // run action
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                onActionClick(action.keyId);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        if (action.isRepeatable) {
            setupRepeat(btn);
        } else {
            btn.setOnLongClickListener(v -> {
                try {
                    v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    return onActionLongClick(action.keyId);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return false;
            });
        }
        final int sidePadding = _buttonHorizontalMargin + btn.getPaddingLeft(); // Left and right are symmetrical
        btn.setPadding(sidePadding, btn.getPaddingTop(), sidePadding, btn.getPaddingBottom());
        barLayout.addView(btn);
    }

    protected void runRegularPrefixAction(String action) {
        runRegularPrefixAction(action, null, false);
    }

    protected void runRegularPrefixAction(String action, Boolean ignoreIndent) {
        runRegularPrefixAction(action, null, ignoreIndent);
    }

    protected void runRegularPrefixAction(String action, String replaceString) {
        runRegularPrefixAction(action, replaceString, false);
    }

    protected void runRegularPrefixAction(final String action, final String replaceString, final Boolean ignoreIndent) {

        String replacement = (replaceString == null) ? "" : replaceString;

        String patternIndent = ignoreIndent ? "(^\\s*)" : "(^)";
        String replaceIndent = "$1";

        String escapedAction = String.format("\\Q%s\\E", action);
        String escapedReplace = String.format("(\\Q%s\\E)?", replacement);

        ReplacePattern[] patterns = {
                // Replace action with replacement
                new ReplacePattern(patternIndent + escapedAction, replaceIndent + replacement),
                // Replace replacement or nothing with action
                new ReplacePattern(patternIndent + escapedReplace, replaceIndent + action),
        };

        runRegexReplaceAction(Arrays.asList(patterns));
    }

    public static class ReplacePattern {
        public final Matcher matcher;
        public final String replacePattern;
        public final boolean replaceAll;

        public boolean isSameReplace() {
            return replacePattern.equals("$0");
        }

        /**
         * Construct a ReplacePattern
         *
         * @param searchPattern  regex search pattern
         * @param replacePattern replace string
         * @param replaceAll     whether to replace all or just the first
         */
        public ReplacePattern(Pattern searchPattern, String replacePattern, boolean replaceAll) {
            this.matcher = searchPattern.matcher("");
            this.replacePattern = replacePattern;
            this.replaceAll = replaceAll;
        }

        public CharSequence replace() {
            return replaceAll ? matcher.replaceAll(replacePattern) : matcher.replaceFirst(replacePattern);
        }

        public ReplacePattern(String searchPattern, String replacePattern, boolean replaceAll) {
            this(Pattern.compile(searchPattern), replacePattern, replaceAll);
        }

        public ReplacePattern(Pattern searchPattern, String replacePattern) {
            this(searchPattern, replacePattern, false);
        }

        public ReplacePattern(String searchPattern, String replacePattern) {
            this(Pattern.compile(searchPattern), replacePattern, false);
        }
    }

    public void runRegexReplaceAction(final ReplacePattern... patterns) {
        runRegexReplaceAction(Arrays.asList(patterns));
    }

    public void runRegexReplaceAction(final List<ReplacePattern> patterns) {
        runRegexReplaceAction(_hlEditor, patterns);
    }

    public void runRegexReplaceAction(final String pattern, final String replace) {
        runRegexReplaceAction(Collections.singletonList(new ReplacePattern(pattern, replace)));
    }

    public static void runRegexReplaceAction(final EditText editor, final ReplacePattern... patterns) {
        runRegexReplaceAction(editor, Arrays.asList(patterns));
    }

    /**
     * Runs through a sequence of regex-search-and-replace actions on each selected line.
     * This function wraps _runRegexReplaceAction with a call to disable text trackers
     *
     * @param patterns An array of ReplacePattern
     */
    public static void runRegexReplaceAction(final EditText editor, final List<ReplacePattern> patterns) {
        if (editor instanceof HighlightingEditor) {
            ((HighlightingEditor) editor).withAutoFormatDisabled(() -> runRegexReplaceAction(editor.getText(), patterns));
        } else {
            runRegexReplaceAction(editor.getText(), patterns);
        }
    }

    public static void runRegexReplaceAction(final Editable editable, final ReplacePattern... patterns) {
        runRegexReplaceAction(editable, Arrays.asList(patterns));
    }

    private static void runRegexReplaceAction(final Editable editable, final List<ReplacePattern> patterns) {

        final int[] sel = TextViewUtils.getSelection(editable);
        if (sel[0] < 0) {
            return;
        }
        final int[][] offsets = TextViewUtils.getLineOffsetFromIndex(editable, sel);

        final TextViewUtils.ChunkedEditable text = TextViewUtils.ChunkedEditable.wrap(editable);
        // Start of line on which sel begins
        final int selStartStart = TextViewUtils.getLineStart(text, sel[0]);

        // Number of lines we will be modifying
        final int lineCount = GsTextUtils.countChars(text, sel[0], sel[1], '\n')[0] + 1;
        int lineStart = selStartStart;


        for (int i = 0; i < lineCount; i++) {

            int lineEnd = TextViewUtils.getLineEnd(text, lineStart);
            final String line = TextViewUtils.toString(text, lineStart, lineEnd);

            for (final ReplacePattern pattern : patterns) {
                if (pattern.matcher.reset(line).find()) {
                    if (!pattern.isSameReplace()) {
                        text.replace(lineStart, lineEnd, pattern.replace());
                    }
                    break;
                }
            }

            lineStart = TextViewUtils.getLineEnd(text, lineStart) + 1;
        }

        text.applyChanges();
        TextViewUtils.setSelectionFromOffsets(editable, offsets);
    }

    public static void surroundBlock(final Editable text, final String delim) {
        final int[] sel = TextViewUtils.getLineSelection(text);
        if (text != null && sel[0] >= 0) {
            final CharSequence line = text.subSequence(sel[0], sel[1]);
            text.replace(sel[0], sel[1], delim + "\n" + line + "\n" + delim);
        }
    }

    protected void runSurroundAction(final String delim) {
        runSurroundAction(delim, delim, true);
    }

    /**
     * Surrounds the current selection with the given startDelimiter and end strings.
     * If the region is already surrounded by the given strings, they are removed instead.
     *
     * @param open  The string to insert at the start of the selection
     * @param close The string to insert at the end of the selection
     * @param trim  Whether to trim spaces from the start and end of the selection
     */
    protected void runSurroundAction(final String open, final String close, final boolean trim) {
        final Editable text = _hlEditor.getText();
        final int[] sel = TextViewUtils.getSelection(text);
        if (sel[0] < 0) {
            return;
        }

        // Detect if delims within or around selection
        // If so, remove it
        // -------------------------------------------------------------------------
        final int ss = sel[0], se = sel[1];
        final int ol = open.length(), cl = close.length(), sl = se - ss;
        // Left as a CharSequence to help maintain spans
        final CharSequence selection = text.subSequence(ss, se);

        // Case delims around selection
        if ((ss >= ol) && ((se + cl) <= text.length())) {
            final String before = text.subSequence(ss - ol, ss).toString();
            final String after = text.subSequence(se, se + cl).toString();
            if (before.equals(open) && after.equals(close)) {
                text.replace(ss - ol, se + cl, selection);
                _hlEditor.setSelection(ss - ol, se - ol);
                return;
            }
        }

        // Case delims within selection
        if ((se - ss) >= (ol + cl)) {
            final String within = text.subSequence(ss, se).toString();
            if (within.startsWith(open) && within.endsWith(close)) {
                text.replace(ss, se, within.substring(ol, within.length() - cl));
                _hlEditor.setSelection(ss, se - ol - cl);
                return;
            }
        }

        final String replace;
        if (trim && selection.length() > 0) {
            final int f = TextViewUtils.getFirstNonWhitespace(selection);
            final int l = TextViewUtils.getLastNonWhitespace(selection) + 1;
            replace = selection.subSequence(0, f) + open +
                    selection.subSequence(f, l) + close +
                    selection.subSequence(l, sl);
        } else {
            replace = open + selection + close;
        }

        text.replace(ss, se, replace);
        _hlEditor.setSelection(ss + ol, se + ol);
    }

    public ActionButtonBase setUiReferences(@Nullable final Activity activity, @Nullable final HighlightingEditor hlEditor, @Nullable final WebView webview) {
        _activity = activity;
        _hlEditor = hlEditor;
        _webView = webview;
        _cu = new MarkorContextUtils(_activity);
        return this;
    }

    public Document getDocument() {
        return _document;
    }

    public ActionButtonBase setDocument(Document document) {
        _document = document;
        return this;
    }

    public Activity getActivity() {
        return _activity;
    }

    public Context getContext() {
        return _activity != null ? _activity : _appSettings.getContext();
    }

    public MarkorContextUtils getCu() {
        return _cu;
    }

    /**
     * Callable from background thread!
     */
    public void setEditorTextAsync(final String text) {
        _activity.runOnUiThread(() -> _hlEditor.setText(text));
    }

    protected void runIndentLines(final boolean deIndent) {
        if (deIndent) {
            final String leadingIndentPattern = String.format("^\\s{1,%d}", _indent);
            ActionButtonBase.runRegexReplaceAction(_hlEditor, new ReplacePattern(leadingIndentPattern, ""));
        } else {
            final String tabString = GsTextUtils.repeatChars(' ', _indent);
            ActionButtonBase.runRegexReplaceAction(_hlEditor, new ReplacePattern("^", tabString));
        }
    }

    // Some actions common to multiple file types
    // Can be called _explicitly_ by a derived class
    protected final boolean runCommonAction(final @StringRes int action) {
        final Editable text = _hlEditor.getText();
        switch (action) {
            case abid_common_unordered_list_char: {
                runRegularPrefixAction(_appSettings.getUnorderedListCharacter() + " ", true);
                return true;
            }
            case abid_common_checkbox_list: {
                runRegularPrefixAction("- [ ] ", "- [x] ", true);
                return true;
            }
            case abid_common_ordered_list_number: {
                runRegularPrefixAction("1. ", true);
                return true;
            }
            case abid_common_move_text_one_line_up:
            case abid_common_move_text_one_line_down: {
                moveLineSelectionBy1(_hlEditor, action == abid_common_move_text_one_line_up);
                runRenumberOrderedListIfRequired();
                return true;
            }
            case abid_common_indent:
            case abid_common_deindent: {
                runIndentLines(action == abid_common_deindent);
                runRenumberOrderedListIfRequired();
                return true;
            }
            case abid_common_new_line_below: {
                // Go to end of line, works with wrapped lines too
                final int sel = TextViewUtils.getSelection(_hlEditor)[1];
                if (sel > 0) {
                    _hlEditor.setSelection(TextViewUtils.getLineEnd(text, sel));
                    _hlEditor.simulateKeyPress(KeyEvent.KEYCODE_ENTER);
                }
                return true;
            }
            case abid_common_delete_lines: {
                final int[] sel = TextViewUtils.getLineSelection(text);
                if (GsTextUtils.isValidSelection(text, sel)) {
                    final boolean lastLine = sel[1] == text.length();
                    final boolean firstLine = sel[0] == 0;
                    text.delete(sel[0] - (lastLine && !firstLine ? 1 : 0), sel[1] + (lastLine ? 0 : 1));
                }
                return true;
            }
            case abid_common_duplicate_lines: {
                duplicateLineSelection(_hlEditor);
                runRenumberOrderedListIfRequired();
                return true;
            }
            case abid_common_web_jump_to_very_top_or_bottom: {
                runJumpBottomTopAction(ActionItem.DisplayMode.VIEW);
                return true;
            }
        }
        return false;
    }

    // Some long-press actions common to multiple file types
    // Can be called _explicitly_ by a derived class
    @SuppressLint("NonConstantResourceId")
    protected final boolean runCommonLongPressAction(@StringRes int action) {
        switch (action) {
            case abid_common_ordered_list_number: {
                runRenumberOrderedListIfRequired(true);
                return true;
            }
            case abid_common_move_text_one_line_up:
            case abid_common_move_text_one_line_down: {
                TextViewUtils.showSelection(_hlEditor);
                return true;
            }
            case abid_common_new_line_below: {
                // Long press = line above
                final Editable text = _hlEditor.getText();
                if (text != null) {
                    final int sel = TextViewUtils.getSelection(text)[0];
                    if (sel >= 0) {
                        final int lineStart = TextViewUtils.getLineStart(text, sel);
                        text.insert(lineStart, "\n");
                        _hlEditor.setSelection(lineStart);
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static class ActionItem {
        @StringRes
        public int keyId;
        @DrawableRes
        public int iconId;
        @StringRes
        public int stringId;
        public DisplayMode displayMode = DisplayMode.EDIT;

        public boolean isRepeatable = false;

        public enum DisplayMode {EDIT, VIEW, ANY}

        public ActionItem(@StringRes int key, @DrawableRes int icon, @StringRes int string) {
            keyId = key;
            iconId = icon;
            stringId = string;
        }

        public ActionItem setDisplayMode(DisplayMode mode) {
            displayMode = mode;
            return this;
        }

        public ActionItem setRepeatable(boolean repeatable) {
            isRepeatable = repeatable;
            return this;
        }
    }

    public static void moveLineSelectionBy1(final HighlightingEditor hlEditor, final boolean isUp) {
        final Editable text = hlEditor.getText();
        final int[] sel = TextViewUtils.getSelection(text);
        if (text == null || sel[0] < 0) {
            return;
        }

        final int[] lineSel = TextViewUtils.getLineSelection(text, sel);

        if ((isUp && lineSel[0] > 0) || (!isUp && lineSel[1] < text.length())) {
            final CharSequence lines = text.subSequence(lineSel[0], lineSel[1]);

            final int[] altSel = TextViewUtils.getLineSelection(text, isUp ? lineSel[0] - 1 : lineSel[1] + 1);
            final CharSequence altLine = text.subSequence(altSel[0], altSel[1]);

            final int[][] offsets = TextViewUtils.getLineOffsetFromIndex(text, sel);

            hlEditor.withAutoFormatDisabled(() -> {
                final SpannableStringBuilder newPair = new SpannableStringBuilder()
                        .append(isUp ? lines : altLine)
                        .append("\n")
                        .append(isUp ? altLine : lines);
                text.replace(Math.min(lineSel[0], altSel[0]), Math.max(lineSel[1], altSel[1]), newPair);
            });

            offsets[0][0] += isUp ? -1 : 1;
            offsets[1][0] += isUp ? -1 : 1;

            TextViewUtils.setSelectionFromOffsets(text, offsets);
        }
    }

    public static void duplicateLineSelection(final HighlightingEditor hlEditor) {
        // Duplication is performed downwards, selection is moving alongside it and
        // cursor is preserved regarding column position (helpful for editing the
        // newly created line at the selected position right away).
        final Editable text = hlEditor.getText();
        final int[] sel = TextViewUtils.getSelection(text);
        if (sel[0] >= 0) {
            final int linesStart = TextViewUtils.getLineStart(text, sel[0]);
            final int linesEnd = TextViewUtils.getLineEnd(text, sel[1]);

            final CharSequence lines = text.subSequence(linesStart, linesEnd);

            final int[][] offsets = TextViewUtils.getLineOffsetFromIndex(text, sel);

            hlEditor.withAutoFormatDisabled(() -> {
                // Prepending the newline instead of appending it is required for making
                // this logic work even if it's about the last line in the given file.
                final String lines_final = String.format("\n%s", lines);
                text.insert(linesEnd, lines_final);
            });

            final int lineCount = offsets[1][0] - offsets[0][0] + 1;
            offsets[0][0] += lineCount;
            offsets[1][0] += lineCount;

            TextViewUtils.setSelectionFromOffsets(text, offsets);
        }
    }

    // Derived classes should override this to implement format-specific renumber logic
    protected void renumberOrderedList() {
        // No-op in base class
    }

    public final void runRenumberOrderedListIfRequired() {
        runRenumberOrderedListIfRequired(false);
    }

    public final void runRenumberOrderedListIfRequired(final boolean force) {
        if (force || _hlEditor.getAutoFormatEnabled()) {
            _hlEditor.withAutoFormatDisabled(this::renumberOrderedList);
        }
    }

    private String rstr(@StringRes int resKey) {
        return getContext().getString(resKey);
    }

    public void runJumpBottomTopAction(ActionItem.DisplayMode displayMode) {
        if (displayMode == ActionItem.DisplayMode.EDIT) {
            int pos = _hlEditor.getSelectionStart();
            _hlEditor.setSelection(pos == 0 ? _hlEditor.getText().length() : 0);
        } else if (displayMode == ActionItem.DisplayMode.VIEW) {
            boolean top = _webView.getScrollY() > 100;
            _webView.scrollTo(0, top ? 0 : _webView.getContentHeight());
            if (!top) {
                _webView.scrollBy(0, 1000);
                _webView.scrollBy(0, 1000);
            }
        }
    }

    public boolean onReceiveKeyPress(final int keyCode, final KeyEvent event) {
        return false;
    }
}
