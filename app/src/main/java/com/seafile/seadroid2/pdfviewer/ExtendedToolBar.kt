package com.seafile.seadroid2.pdfviewer

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.SwitchCompat
import com.bhuvaneshw.pdf.PdfDocumentProperties
import com.bhuvaneshw.pdf.PdfUnstableApi
import com.bhuvaneshw.pdf.PdfViewer
import com.bhuvaneshw.pdf.PdfViewer.PageSpreadMode
import com.bhuvaneshw.pdf.ui.PdfToolBar
import com.bhuvaneshw.pdf.ui.PdfToolBarMenuItem
import com.mohammedalaa.seekbar.DoubleValueSeekBarView
import com.seafile.seadroid2.BuildConfig
import com.seafile.seadroid2.R
import kotlin.math.roundToInt

class ExtendedToolBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : PdfToolBar(context, attrs, defStyleAttr) {

    enum class CustomMenuItem(internal val id: Int) {
        SHARE(11),
        OPEN_WITH(12),
        ZOOM_LIMIT(13),
        PRINT(14),
        DOWNLOAD(15),
        ZOOM(16),
        GO_TO_PAGE(17),
        ROTATE_CLOCKWISE(18),
        ROTATE_ANTI_CLOCKWISE(19),
        SCROLL_MODE(20),
        SINGE_PAGE_ARRANGEMENT(21),
        SPLIT_MODE(22),
        ALIGN_MODE(23),
        SNAP_PAGE(24),
        PROPERTIES(25),
    }

    private val authority = BuildConfig.APPLICATION_ID
    var pdfFileName: String? = null
    var action = ""
    var onRotate: ((pageIndex: Int, deltaDegrees: Int) -> Unit)? = null

    @OptIn(PdfUnstableApi::class)
    override fun getPopupMenu(anchorView: View): PopupMenu {
        val wrapperContext = ContextThemeWrapper(context, R.style.popupOverflowMenu)
        return PopupMenu(wrapperContext, anchorView).apply {
            if (pdfViewer.createSharableUri(authority) != null) {
                menu.add(Menu.NONE, CustomMenuItem.SHARE.id, Menu.NONE, R.string.file_action_export)
                menu.add(Menu.NONE, CustomMenuItem.OPEN_WITH.id, Menu.NONE, R.string.file_action_open)
            }
            menu.add(Menu.NONE, CustomMenuItem.ZOOM_LIMIT.id, Menu.NONE, R.string.zoom_limit)
            menu.add(Menu.NONE, CustomMenuItem.PRINT.id, Menu.NONE, R.string.print)
            menu.add(Menu.NONE, CustomMenuItem.DOWNLOAD.id, Menu.NONE, R.string.download)
            menu.add(Menu.NONE,
                CustomMenuItem.ZOOM.id,
                Menu.NONE,
                pdfViewer.currentPageScaleValue.formatZoom(pdfViewer.currentPageScale)
            )
            menu.add(Menu.NONE, CustomMenuItem.GO_TO_PAGE.id, Menu.NONE, R.string.go_to_page)
            menu.add(Menu.NONE, CustomMenuItem.ROTATE_CLOCKWISE.id, Menu.NONE, R.string.rotate_clockwise)
            menu.add(Menu.NONE, CustomMenuItem.ROTATE_ANTI_CLOCKWISE.id, Menu.NONE, R.string.rotate_anti_clockwise)
            menu.add(Menu.NONE, CustomMenuItem.SCROLL_MODE.id, Menu.NONE, R.string.scroll_mode)
            if (pdfViewer.pageScrollMode.let { it == PdfViewer.PageScrollMode.VERTICAL || it == PdfViewer.PageScrollMode.HORIZONTAL }
                && pdfViewer.pageSpreadMode == PageSpreadMode.NONE)
                menu.add(Menu.NONE, CustomMenuItem.SINGE_PAGE_ARRANGEMENT.id, Menu.NONE, R.string.single_page_arrangement)
            menu.add(Menu.NONE, CustomMenuItem.SPLIT_MODE.id, Menu.NONE, R.string.split_mode)
            menu.add(Menu.NONE, CustomMenuItem.ALIGN_MODE.id, Menu.NONE, R.string.align_mode)
            menu.add(Menu.NONE, CustomMenuItem.SNAP_PAGE.id, Menu.NONE, R.string.snap_page)
            menu.add(Menu.NONE, CustomMenuItem.PROPERTIES.id, Menu.NONE, R.string.properties)
//            addDefaultMenus(this)
        }
    }

    private fun String.formatZoom(zoom: Float): String {
        return when (this) {
            Zoom.AUTOMATIC.value -> resources.getString(R.string.zoom_auto)
            Zoom.PAGE_FIT.value -> resources.getString(R.string.zoom_page_fit)
            Zoom.PAGE_WIDTH.value -> resources.getString(R.string.zoom_page_width)
            Zoom.ACTUAL_SIZE.value -> resources.getString(R.string.zoom_actual_size)
            else -> String.format(resources.getString(R.string.zoom_percent), (zoom * 100).roundToInt())
        }
    }

    private enum class Zoom(val value: String) {
        AUTOMATIC("auto"),
        PAGE_FIT("page-fit"),
        PAGE_WIDTH("page-width"),
        ACTUAL_SIZE("page-actual")
    }

    @OptIn(PdfUnstableApi::class)
    override fun handlePopupMenuItemClick(item: MenuItem): Boolean {
        if (super.handlePopupMenuItemClick(item)) return true

        return when (item.itemId) {
            CustomMenuItem.SHARE.id -> {
                pdfViewer.createSharableUri(authority)?.let {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, it)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, resources.getString(R.string.share_pdf_using)))
                } ?: context.toast(resources.getString(R.string.unable_share_pdf))
                true
            }

            CustomMenuItem.OPEN_WITH.id -> {
                pdfViewer.createSharableUri(authority)?.let {
                    context.startActivity(Intent(Intent.ACTION_VIEW, it).apply {
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    })
                } ?: context.toast(resources.getString(R.string.unable_open_pdf_other))
                true
            }

            CustomMenuItem.ZOOM_LIMIT.id -> {
                showZoomLimitDialog()
                true
            }

            CustomMenuItem.PRINT.id -> {
                action = PdfViewerActivity.PRINT_ACTION
                pdfViewer.downloadFile()
                true
            }

            CustomMenuItem.DOWNLOAD.id -> {
                action = PdfViewerActivity.DOWNLOAD_ACTION
                pdfViewer.downloadFile()
                true
            }

            CustomMenuItem.ZOOM.id -> {
                showZoomDialog()
                true
            }

            CustomMenuItem.GO_TO_PAGE.id -> {
                showGoToPageDialog()
                true
            }

            CustomMenuItem.ROTATE_CLOCKWISE.id -> {
                pdfViewer.rotateClockWise()
                onRotate?.invoke(pdfViewer.currentPage, 90)
                true
            }

            CustomMenuItem.ROTATE_ANTI_CLOCKWISE.id -> {
                pdfViewer.rotateCounterClockWise()
                onRotate?.invoke(pdfViewer.currentPage, -90)
                true
            }

            CustomMenuItem.SCROLL_MODE.id -> {
                showScrollModeDialog()
                true
            }

            CustomMenuItem.SINGE_PAGE_ARRANGEMENT.id -> {
                showSinglePageArrangementDialog()
                true
            }

            CustomMenuItem.SPLIT_MODE.id -> {
                showSpreadModeDialog()
                true
            }

            CustomMenuItem.ALIGN_MODE.id -> {
                showAlignModeDialog()
                true
            }

            CustomMenuItem.SNAP_PAGE.id -> {
                showSnapPageDialog()
                true
            }

            CustomMenuItem.PROPERTIES.id -> {
                showPropertiesDialog()
                true
            }

            else -> false
        }
    }

    private fun showZoomLimitDialog() {
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_zoom_limit, null)

        val zoomSeekbar = dialogView.findViewById<DoubleValueSeekBarView>(R.id.zoom_seekbar)
        zoomSeekbar.currentMinValue = (pdfViewer.minPageScale * 100).roundToInt()
        zoomSeekbar.currentMaxValue = (pdfViewer.maxPageScale * 100).roundToInt()

        val dialog = alertDialogBuilder()
            .setTitle(R.string.zoom_limit)
            .setView(dialogView)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
                pdfViewer.minPageScale = zoomSeekbar.currentMinValue / 100f
                pdfViewer.maxPageScale = zoomSeekbar.currentMaxValue / 100f
                pdfViewer.scalePageTo(pdfViewer.currentPageScale)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        showDialog(dialog)
    }

    private fun showZoomDialog() {
        val displayOptions = arrayOf(
            resources.getString(R.string.automatic),
            resources.getString(R.string.page_fit),
            resources.getString(R.string.page_width),
            resources.getString(R.string.actual_size),
            "50%", "75%", "100%", "125%", "150%", "200%", "300%", "400%"
        )
        val options = arrayOf(
            Zoom.AUTOMATIC.value, Zoom.PAGE_FIT.value,
            Zoom.PAGE_WIDTH.value, Zoom.ACTUAL_SIZE.value,
            "0.5", "0.75", "1", "1.25", "1.5", "2", "3", "4"
        )

        val selectedPosition = findSelectedOption(options, pdfViewer.currentPageScaleValue)
        val adapter = RadioOptionAdapter(context, displayOptions, selectedPosition)

        val dialog = alertDialogBuilder()
            .setTitle(R.string.select_zoom_level)
            .setAdapter(adapter) { dialog, which ->
                when (which) {
                    0 -> pdfViewer.zoomTo(PdfViewer.Zoom.AUTOMATIC)
                    1 -> pdfViewer.zoomTo(PdfViewer.Zoom.PAGE_FIT)
                    2 -> pdfViewer.zoomTo(PdfViewer.Zoom.PAGE_WIDTH)
                    3 -> pdfViewer.zoomTo(PdfViewer.Zoom.ACTUAL_SIZE)
                    else -> pdfViewer.scalePageTo(scale = options[which].toFloatOrNull() ?: 1f)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        showDialog(dialog)
    }

    private fun showGoToPageDialog() {
        @SuppressLint("InflateParams")
        val inflater = LayoutInflater.from(context)
        val root = inflater.inflate(R.layout.dialog_pdf_go_to_page, null)
        val field: EditText = root.findViewById(R.id.go_to_page_field)

        val gotTo: (String, DialogInterface) -> Unit = { pageNumber, dialog ->
            pdfViewer.goToPage(pageNumber.toIntOrNull() ?: pdfViewer.currentPage)
            dialog.dismiss()
        }

        val dialog = alertDialogBuilder()
            .setTitle(R.string.go_to_page)
            .setView(root)
            .setPositiveButton(R.string.go) { dialog, _ ->
                gotTo(field.text.toString(), dialog)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        field.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                gotTo(field.text.toString(), dialog)
                true
            } else {
                false
            }
        }
        dialog.setOnShowListener {
            field.postDelayed({
                field.requestKeyboard()
            }, 500)
        }
        showDialog(dialog)
    }

    private fun showSinglePageArrangementDialog() {
        val inflater = LayoutInflater.from(context)
        val root = inflater.inflate(R.layout.dialog_pdf_snap_page, null)
        val switch = root.findViewById<SwitchCompat>(R.id.snap_page)
        switch.isChecked = pdfViewer.singlePageArrangement

        val dialog = alertDialogBuilder()
            .setTitle(R.string.single_page_arrangement)
            .setView(root)
            .setPositiveButton(R.string.done) { dialog, _ ->
                pdfViewer.singlePageArrangement = switch.isChecked
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        showDialog(dialog)
    }

    private fun showSnapPageDialog() {
        val inflater = LayoutInflater.from(context)
        val root = inflater.inflate(R.layout.dialog_pdf_snap_page, null)
        val switch = root.findViewById<SwitchCompat>(R.id.snap_page)
        switch.isChecked = pdfViewer.snapPage

        val dialog = alertDialogBuilder()
            .setTitle(R.string.snap_page)
            .setView(root)
            .setPositiveButton(R.string.done) { dialog, _ ->
                pdfViewer.snapPage = switch.isChecked
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        showDialog(dialog)
    }

    private fun showPropertiesDialog() {
        val dialog = alertDialogBuilder()
            .setTitle(R.string.document_properties)
            .let {
                pdfViewer.properties?.let { properties ->
                    it.setPropertiesView(properties)
                } ?: it.setMessage(R.string.properties_not_loaded)
            }
            .setPositiveButton(R.string.close) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        showDialog(dialog)
    }

    private fun AlertDialog.Builder.setPropertiesView(properties: PdfDocumentProperties): AlertDialog.Builder {
        @SuppressLint("InflateParams")
        val inflater = LayoutInflater.from(context)
        val root = inflater.inflate(R.layout.dialog_pdf_properties, null)

        root.find(R.id.file_name).text = pdfFileName?.ifBlank { "-" } ?: "-"
        root.find(R.id.file_size).text = properties.fileSize.formatToSize()
        root.find(R.id.title).text = properties.title
        root.find(R.id.subject).text = properties.subject
        root.find(R.id.author).text = properties.author
        root.find(R.id.creator).text = properties.creator
        root.find(R.id.producer).text = properties.producer
        root.find(R.id.creation_date).text = properties.creationDate.formatToDate()
        root.find(R.id.modified_date).text = properties.modifiedDate.formatToDate()
        root.find(R.id.keywords).text = properties.keywords
        root.find(R.id.language).text = properties.language
        root.find(R.id.pdf_format_version).text = properties.pdfFormatVersion
        root.find(R.id.is_linearized).text = properties.isLinearized.toString()

        return setView(root)
    }

    private fun showScrollModeDialog() {
        val displayOptions = arrayOf(
            resources.getString(R.string.vertical),
            resources.getString(R.string.horizontal),
            resources.getString(R.string.wrapped),
            resources.getString(R.string.single_page),
        )
        val options = arrayOf(
            PdfViewer.PageScrollMode.VERTICAL.name,
            PdfViewer.PageScrollMode.HORIZONTAL.name,
            PdfViewer.PageScrollMode.WRAPPED.name,
            PdfViewer.PageScrollMode.SINGLE_PAGE.name
        )

        val selectedPosition = findSelectedOption(options, pdfViewer.pageScrollMode.name)
        val adapter = RadioOptionAdapter(context, displayOptions, selectedPosition)

        val dialog = alertDialogBuilder()
            .setTitle(resources.getString(R.string.select_page_scroll_mode))
            .setAdapter(adapter) { dialog, which ->
                pdfViewer.pageScrollMode = PdfViewer.PageScrollMode.valueOf(options[which])
                dialog.dismiss()
            }
            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        showDialog(dialog)
    }

    private fun showSpreadModeDialog() {
        val displayOptions = arrayOf(
            resources.getString(R.string.none),
            resources.getString(R.string.odd),
            resources.getString(R.string.even),
        )
        val options = arrayOf(
            PageSpreadMode.NONE.name,
            PageSpreadMode.ODD.name,
            PageSpreadMode.EVEN.name
        )

        val selectedPosition = findSelectedOption(options, pdfViewer.pageSpreadMode.name)
        val adapter = RadioOptionAdapter(context, displayOptions, selectedPosition)

        val dialog = alertDialogBuilder()
            .setTitle(resources.getString(R.string.select_page_split_mode))
            .setAdapter(adapter) { dialog, which ->
                pdfViewer.pageSpreadMode = PageSpreadMode.valueOf(options[which])
                dialog.dismiss()
            }
            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        showDialog(dialog)
    }

    private fun showAlignModeDialog() {
        val displayOptions = buildList {
            add(resources.getString(R.string.default_text))
            if (pdfViewer.singlePageArrangement || (pdfViewer.pageScrollMode != PdfViewer.PageScrollMode.VERTICAL && pdfViewer.pageScrollMode != PdfViewer.PageScrollMode.WRAPPED))
                add(resources.getString(R.string.center_vertically))
            if (pdfViewer.singlePageArrangement || (pdfViewer.pageScrollMode != PdfViewer.PageScrollMode.HORIZONTAL))
                add(resources.getString(R.string.center_horizontally))
            if (pdfViewer.singlePageArrangement || (pdfViewer.pageScrollMode == PdfViewer.PageScrollMode.SINGLE_PAGE))
                add(resources.getString(R.string.center_both))
        }.toTypedArray()
        val options = buildList {
            add(PdfViewer.PageAlignMode.DEFAULT.name)
            if (pdfViewer.singlePageArrangement || (pdfViewer.pageScrollMode != PdfViewer.PageScrollMode.VERTICAL && pdfViewer.pageScrollMode != PdfViewer.PageScrollMode.WRAPPED))
                add(PdfViewer.PageAlignMode.CENTER_VERTICAL.name)
            if (pdfViewer.singlePageArrangement || (pdfViewer.pageScrollMode != PdfViewer.PageScrollMode.HORIZONTAL))
                add(PdfViewer.PageAlignMode.CENTER_HORIZONTAL.name)
            if (pdfViewer.singlePageArrangement || (pdfViewer.pageScrollMode == PdfViewer.PageScrollMode.SINGLE_PAGE))
                add(PdfViewer.PageAlignMode.CENTER_BOTH.name)
        }.toTypedArray()

        val selectedPosition = findSelectedOption(options, pdfViewer.pageAlignMode.name)
        val adapter = RadioOptionAdapter(context, displayOptions, selectedPosition)

        val dialog = alertDialogBuilder()
            .setTitle(resources.getString(R.string.select_page_align_mode))
            .setAdapter(adapter) { dialog, which ->
                pdfViewer.pageAlignMode = PdfViewer.PageAlignMode.valueOf(options[which])
                dialog.dismiss()
            }
            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        showDialog(dialog)
    }
}

fun popup(
    view: View,
    backgroundColor: Int,
    content: LinearLayout.(dismiss: () -> Unit) -> Unit
) {
    val popup = PopupWindow(view.context)
    popup.contentView = LinearLayout(view.context).apply {
        setBackgroundResource(R.drawable.pdf_popup_bg)
        setBgTintModes(backgroundColor)
        orientation = LinearLayout.VERTICAL
        val paddingValue = context.dpToPx(12)
        setPadding(paddingValue, paddingValue, paddingValue, paddingValue)
        content(popup::dismiss)
    }
    popup.isOutsideTouchable = true
    popup.setBackgroundDrawable(null)
    popup.showAsDropDown(view)
}

fun onSeekBarChangeListener(callback: (newProgress: Int) -> Unit) =
    object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(
            seekBar: SeekBar?,
            progress: Int,
            fromUser: Boolean
        ) {
            callback(progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }