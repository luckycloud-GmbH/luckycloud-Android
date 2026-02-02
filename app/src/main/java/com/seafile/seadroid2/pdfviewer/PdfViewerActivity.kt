package com.seafile.seadroid2.pdfviewer

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bhuvaneshw.pdf.PdfListener
import com.bhuvaneshw.pdf.PdfUnstableApi
import com.bhuvaneshw.pdf.PdfUnstablePrintApi
import com.bhuvaneshw.pdf.PdfViewer
import com.bhuvaneshw.pdf.SimplePdfPrintAdapter
import com.bhuvaneshw.pdf.addListener
import com.bhuvaneshw.pdf.callIfScrollSpeedLimitIsEnabled
import com.bhuvaneshw.pdf.callSafely
import com.bhuvaneshw.pdf.setting.PdfSettingsManager
import com.bhuvaneshw.pdf.sharedPdfSettingsManager
import com.bhuvaneshw.pdf.ui.PdfViewerContainer
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import com.seafile.seadroid2.R
import com.seafile.seadroid2.SettingsManager
import com.seafile.seadroid2.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class PdfViewerActivity : AppCompatActivity() {

    companion object {
        const val DOWNLOAD_PDF_REQUEST_CODE = 1001

        const val PDF_SETTINGS = "PdfSettings"
        const val PRINT_ACTION = "print"
        const val DOWNLOAD_ACTION = "download"
        const val SAVE_ACTION = "save"
    }

    private var pdfBytesToSave: ByteArray? = null
    private val pageRotationDeltas: MutableMap<Int, Int> = mutableMapOf()

    private var fullscreen = false
    private lateinit var pdfSettingsManager: PdfSettingsManager

    lateinit var pdfToolBar: ExtendedToolBar

    lateinit var filePath: String
    lateinit var fileName: String

    @OptIn(PdfUnstablePrintApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pdf_viewer)

        val strManager = SettingsManager.instance()

        val container = findViewById<PdfViewerContainer>(R.id.container)
        val pdfViewer = findViewById<PdfViewer>(R.id.pdf_viewer)
        pdfToolBar = findViewById<ExtendedToolBar>(R.id.pdf_tool_bar)
        val loader = findViewById<View>(R.id.loader)
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)

        ViewCompat.setOnApplyWindowInsetsListener(container) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        pdfSettingsManager = sharedPdfSettingsManager(PDF_SETTINGS, MODE_PRIVATE).also { it.includeAll() }

        val (path, name) = getDataFromIntent() ?: run {
            toast(resources.getString(R.string.not_available))
            finish()
            return
        }
        filePath = path
        fileName = name

        pdfViewer.onReady {
            pdfSettingsManager.restore(this)
            load(filePath)
            if (filePath.isNotBlank()) {
                pdfToolBar.setFileName(fileName)
                pdfToolBar.pdfFileName = fileName
            }
            pdfToolBar.findViewById<EditText>(R.id.find_edit_text).setHint(R.string.find)
            pdfToolBar.findViewById<Switch>(R.id.show_all_highlights).setText(R.string.show_all)
            pdfToolBar.findViewById<Switch>(R.id.show_all_highlights).setTextColor(resources.getColor(R.color.search_foreground))

            val editTitle: TextView = pdfToolBar.findViewById(R.id.edit_title)
            editTitle.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val translated = when (s.toString()) {
                        "Edit" -> resources.getString(R.string.edit)
                        "Highlight" -> resources.getString(R.string.highlight)
                        "Text" -> resources.getString(R.string.text)
                        "Draw" -> resources.getString(R.string.draw)
                        "Add/Edit Images" -> resources.getString(R.string.add_edit_images)
                        else -> s.toString()
                    }
                    if (s.toString() != translated) {
                        editTitle.text = translated
                    }
                }

                override fun afterTextChanged(s: Editable) {}
            })
        }

        // Track page rotations initiated via toolbar actions so we can persist them on save
        pdfToolBar.onRotate = { pageIndex, deltaDegrees ->
            val current = pageRotationDeltas[pageIndex] ?: 0
            var updated = (current + deltaDegrees) % 360
            if (updated < 0) updated += 360
            pageRotationDeltas[pageIndex] = updated
        }

        pdfToolBar.onBack = {
            onBackPressedDispatcher.onBackPressed()
        }
        pdfToolBar.alertDialogBuilder = { AlertDialog.Builder(this) }
        pdfToolBar.showDialog = { dialog -> run {
            dialog.show()
            dialog.window?.setBackgroundDrawable(
                ContextCompat.getDrawable(this, R.drawable.msg_background)
            )
        }}
        var selectedColor = Color.WHITE
        pdfToolBar.pickColor = { onPickColor ->
            ColorPickerDialog.newBuilder()
                .setColor(selectedColor)
                .setDialogTitle(R.string.select_color)
                .setCustomButtonText(R.string.custom)
                .setSelectedButtonText(R.string.select)
                .setPresetsButtonText(R.string.presets)
                .create().apply {
                    setColorPickerDialogListener(object : ColorPickerDialogListener {
                        override fun onColorSelected(dialogId: Int, color: Int) {
                            selectedColor = color
                            onPickColor(color)
                        }

                        override fun onDialogDismissed(dialogId: Int) {}
                    })

                    show(supportFragmentManager, "color-picker-dialog")

                    Handler(Looper.getMainLooper()).postDelayed({
                        dialog?.window?.setBackgroundDrawableResource(R.drawable.msg_background)
                    }, 50)
                }
        }
        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_home_up_btn, theme)
        pdfToolBar.back.setImageDrawable(drawable)
        val params = pdfToolBar.back.layoutParams
        params.width = Utils.dip2px(this, 32f)
        pdfToolBar.back.layoutParams = params

        pdfToolBar.back.setOnClickListener {
            when {
                pdfToolBar.isHighlightBarVisible() -> {
                    pdfToolBar.setHighlightBarVisible(false)
                    pdfToolBar.setEditorBarVisible(true)
                }

                pdfToolBar.isFreeTextBarVisible() -> {
                    pdfToolBar.setFreeTextBarVisible(false)
                    pdfToolBar.setEditorBarVisible(true)
                }

                pdfToolBar.isInkBarVisible() -> {
                    pdfToolBar.setInkBarVisible(false)
                    pdfToolBar.setEditorBarVisible(true)
                }

                pdfToolBar.isStampBarVisible() -> {
                    pdfToolBar.setStampBarVisible(false)
                    pdfToolBar.setEditorBarVisible(true)
                }

                pdfToolBar.isEditorBarVisible() -> pdfToolBar.setEditorBarVisible(false)
                pdfToolBar.isFindBarVisible() -> pdfToolBar.setFindBarVisible(false)
                else -> pdfToolBar.onBack?.invoke()
            }
        }

        try {
            val edit: ImageView = pdfToolBar.findViewById<ImageView>(R.id.edit)
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
                edit.visibility = View.GONE
            } else {
                edit.setOnClickListener {
                    pdfViewer.editor.highlightColor = strManager.pdfHighlightColor
                    pdfToolBar.highlightColor.color = strManager.pdfHighlightColor
                    pdfViewer.editor.highlightThickness = strManager.pdfHighlightThickness
                    pdfViewer.editor.freeFontSize = strManager.pdfFontSize
                    pdfViewer.editor.freeFontColor = strManager.pdfFontColor
                    pdfToolBar.freeFontColor.color = strManager.pdfFontColor
                    pdfViewer.editor.inkThickness = strManager.pdfInkThickness
                    pdfViewer.editor.inkOpacity = strManager.pdfInkOpacity
                    pdfViewer.editor.inkColor = strManager.pdfInkColor
                    pdfToolBar.inkColor.color = strManager.pdfInkColor
                    pdfToolBar.setEditorBarVisible(true)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        pdfToolBar.highlightThickness.setOnClickListener {
            popup(it, pdfToolBar.popupBackgroundColor) {
                val paddingValue = context.dpToPx(12)
                addView(TextView(context).apply {
                    text = resources.getString(R.string.thickness)
                    setTextColor(pdfToolBar.contentColor)
                    setPadding(paddingValue, paddingValue, paddingValue, paddingValue)
                })
                addView(
                    SeekBar(context).apply {
                        max = 16
                        progress = pdfViewer.editor.highlightThickness - 8
                        setOnSeekBarChangeListener(onSeekBarChangeListener { newProgress ->
                            strManager.savePdfHighlightThickness(newProgress + 8)
                            pdfViewer.editor.highlightThickness = newProgress + 8
                        })
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            outlineSpotShadowColor = pdfToolBar.contentColor
                            outlineAmbientShadowColor = pdfToolBar.contentColor
                        }
                        setPadding(paddingValue, 0, paddingValue, paddingValue)
                    },
                    LinearLayout.LayoutParams(
                        context.dpToPx(220),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            }
        }

        pdfToolBar.highlightColor.setOnClickListener {
            popup(it, pdfToolBar.popupBackgroundColor) { dismiss ->
                val paddingValue = context.dpToPx(12)
                addView(TextView(context).apply {
                    text = resources.getString(R.string.highlight_color)
                    setTextColor(pdfToolBar.contentColor)
                    setPadding(paddingValue, paddingValue, paddingValue, paddingValue)
                })
                addView(
                    ColorItemGrid(context, pdfViewer.highlightEditorColors, pdfToolBar.contentColor) { color ->
                        @OptIn(PdfUnstableApi::class)
                        strManager.savePdfHighlightColor(color)
                        pdfViewer.editor.highlightColor = color
                        pdfToolBar.highlightColor.color = color
                        dismiss()
                    }
                )
            }
        }

        pdfToolBar.freeFontSize.setOnClickListener {
            popup(it, pdfToolBar.popupBackgroundColor) {
                val paddingValue = context.dpToPx(12)
                addView(TextView(context).apply {
                    text = resources.getString(R.string.font_size)
                    setTextColor(pdfToolBar.contentColor)
                    setPadding(paddingValue, paddingValue, paddingValue, paddingValue)
                })
                addView(
                    SeekBar(context).apply {
                        max = 95
                        progress = pdfViewer.editor.freeFontSize - 5
                        setOnSeekBarChangeListener(onSeekBarChangeListener { newProgress ->
                            strManager.savePdfFontSize(newProgress + 5)
                            pdfViewer.editor.freeFontSize = newProgress + 5
                        })
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            outlineSpotShadowColor = pdfToolBar.contentColor
                            outlineAmbientShadowColor = pdfToolBar.contentColor
                        }
                        setPadding(paddingValue, 0, paddingValue, paddingValue)
                    },
                    LinearLayout.LayoutParams(
                        context.dpToPx(220),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            }
        }

        pdfToolBar.freeFontColor.setOnClickListener {
            pdfToolBar.pickColor?.invoke { color ->
                strManager.savePdfFontColor(color)
                pdfViewer.editor.freeFontColor = color
                pdfToolBar.freeFontColor.color = color
            }
        }

        pdfToolBar.inkThickness.setOnClickListener {
            popup(it, pdfToolBar.popupBackgroundColor) {
                val paddingValue = context.dpToPx(12)
                addView(TextView(context).apply {
                    text = resources.getString(R.string.thickness)
                    setTextColor(pdfToolBar.contentColor)
                    setPadding(paddingValue, paddingValue, paddingValue, paddingValue)
                })
                addView(
                    SeekBar(context).apply {
                        max = 19
                        progress = pdfViewer.editor.inkThickness - 1
                        setOnSeekBarChangeListener(onSeekBarChangeListener { newProgress ->
                            strManager.savePdfInkThickness(newProgress + 1)
                            pdfViewer.editor.inkThickness = newProgress + 1
                        })
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            outlineSpotShadowColor = pdfToolBar.contentColor
                            outlineAmbientShadowColor = pdfToolBar.contentColor
                        }
                        setPadding(paddingValue, 0, paddingValue, paddingValue)
                    },
                    LinearLayout.LayoutParams(
                        context.dpToPx(220),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            }
        }

        pdfToolBar.inkOpacity.setOnClickListener {
            popup(it, pdfToolBar.popupBackgroundColor) {
                val paddingValue = context.dpToPx(12)
                addView(TextView(context).apply {
                    text = resources.getString(R.string.opacity)
                    setTextColor(pdfToolBar.contentColor)
                    setPadding(paddingValue, paddingValue, paddingValue, paddingValue)
                })
                addView(
                    SeekBar(context).apply {
                        max = 99
                        progress = pdfViewer.editor.inkOpacity - 1
                        setOnSeekBarChangeListener(onSeekBarChangeListener { newProgress ->
                            strManager.savePdfInkOpacity(newProgress + 1)
                            pdfViewer.editor.inkOpacity = newProgress + 1
                        })
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            outlineSpotShadowColor = pdfToolBar.contentColor
                            outlineAmbientShadowColor = pdfToolBar.contentColor
                        }
                        setPadding(paddingValue, 0, paddingValue, paddingValue)
                    },
                    LinearLayout.LayoutParams(
                        context.dpToPx(220),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            }
        }

        pdfToolBar.inkColor.setOnClickListener {
            pdfToolBar.pickColor?.invoke { color ->
                strManager.savePdfInkColor(color)
                pdfViewer.editor.inkColor = color
                pdfToolBar.inkColor.color = color
            }
        }

        container.alertDialogBuilder = pdfToolBar.alertDialogBuilder
        pdfViewer.pdfPrintAdapter = SimplePdfPrintAdapter();
        pdfViewer.addListener(DownloadPdfListener(fileName))
        pdfViewer.addListener(ImagePickerListener(this))
        container.setAsLoadingIndicator(loader)

        onBackPressedDispatcher.addCallback(this) {
            if (pdfToolBar.isFindBarVisible()) {
                pdfToolBar.setFindBarVisible(false)
            } else {
                pdfToolBar.action = SAVE_ACTION
                pdfViewer.downloadFile()
            }
        }

        pdfViewer.run {
            highlightEditorColors = listOf(
                "black" to resources.getColor(R.color.black),
                "red" to resources.getColor(R.color.red),
                "green" to resources.getColor(R.color.luckycloud_green),
                "yellow" to resources.getColor(R.color.lucky_yellow),
                "blue" to resources.getColor(R.color.blue),
            )
            addListener(
                onPageLoadFailed = {
                    toast(it)
                    finish()
                },
                onLinkClick = { link ->
                    startActivity(Intent(Intent.ACTION_VIEW, link.toUri()))
                },
                onProgressChange = { progress ->
                    progressBar.isIndeterminate = false
                    progressBar.progress = (progress * 100).toInt()
                }
            )
        }

        pdfViewer.addListener(object : PdfListener {
            @OptIn(PdfUnstableApi::class)
            override fun onSingleClick() {
                pdfViewer.callSafely {
                    fullscreen = !fullscreen
                    setFullscreen(fullscreen)
                    container.animateToolBar(!fullscreen)
                }
            }

            @OptIn(PdfUnstableApi::class)
            override fun onDoubleClick() {
                pdfViewer.run {
                    callSafely {
                        val originalCurrentPage = currentPage

                        if (!isZoomInMinScale()) zoomToMinimum()
                        else zoomToMaximum()

                        callIfScrollSpeedLimitIsEnabled {
                            goToPage(originalCurrentPage)
                        }
                    }
                }
            }
        })
    }

    override fun onPause() {
        findViewById<PdfViewer>(R.id.pdf_viewer).also {
            pdfSettingsManager.save(it)
            // Remove any rotation-related flags so rotation does not persist across PDFs
            clearRotationFromPrefs()
        }
        super.onPause()
    }

    override fun onDestroy() {
        findViewById<PdfViewer>(R.id.pdf_viewer).also {
            pdfSettingsManager.save(it)
            clearRotationFromPrefs()
        }
        super.onDestroy()
    }

    inner class DownloadPdfListener(private val pdfTitle: String) : PdfListener {
        override fun onSavePdf(pdfAsBytes: ByteArray) {
            // Apply any pending page rotations to the exported PDF bytes
            val bytesWithRotation = if (pageRotationDeltas.isNotEmpty()) {
                try {
                    applyPageRotations(pdfAsBytes, pageRotationDeltas)
                } catch (e: Exception) {
                    e.printStackTrace()
                    pdfAsBytes
                }
            } else pdfAsBytes
            pdfBytesToSave = bytesWithRotation

            if (pdfToolBar.action == PRINT_ACTION) {
                try {
                    val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
                    val jobName = resources.getString(R.string.print_document)
                    val adapter = PdfByteArrayPrintAdapter(bytesWithRotation)
                    printManager.print(jobName, adapter, null)
                } catch (e: IOException) {
                    e.printStackTrace()
                    return
                }
            } else if (pdfToolBar.action == SAVE_ACTION) {
                val file = File(filePath)
                try {
                    var needUpdate = false
                    if (file.exists()) {
                        if (!file.readBytes().contentEquals(bytesWithRotation)) {
                            needUpdate = true
                        }
                    } else {
                        file.createNewFile();
                        needUpdate = true
                    }
                    if (needUpdate) {
                        file.writeBytes(bytesWithRotation)
                        toast(resources.getString(R.string.editor_file_save_success))
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                // Clear recorded rotations after save
                pageRotationDeltas.clear()
                finish()
            } else if (pdfToolBar.action == DOWNLOAD_ACTION) {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_TITLE, pdfTitle)
                }

                this@PdfViewerActivity.startActivityForResult(intent, DOWNLOAD_PDF_REQUEST_CODE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == DOWNLOAD_PDF_REQUEST_CODE && resultCode == RESULT_OK) {
            val uri = data?.data
            uri?.let {
                pdfBytesToSave?.let { pdfAsBytes ->
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            contentResolver.openOutputStream(uri)?.use { it.write(pdfAsBytes) }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    // Remove any rotation-related flags from the library's stored preferences to avoid cross-PDF rotation persistence
    private fun clearRotationFromPrefs() {
        try {
            val prefs = getSharedPreferences(PDF_SETTINGS, Context.MODE_PRIVATE)
            val keys = prefs.all.keys
            val editor = prefs.edit()
            keys.filter { it.contains("rotat", ignoreCase = true) || it.contains("angle", ignoreCase = true) }
                .forEach { editor.remove(it) }
            editor.apply()
        } catch (_: Throwable) { }
    }
}

class PdfByteArrayPrintAdapter(
    private val data: ByteArray
) : PrintDocumentAdapter() {

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        layoutResultCallback: LayoutResultCallback?,
        extras: Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            layoutResultCallback?.onLayoutCancelled()
            return
        }

        val builder = PrintDocumentInfo.Builder("document.pdf")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
            .build()

        layoutResultCallback?.onLayoutFinished(builder, true)
    }

    override fun onWrite(
        pages: Array<PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        writeResultCallback: WriteResultCallback
    ) {
        try {
            ByteArrayInputStream(data).use { input ->
                FileOutputStream(destination.fileDescriptor).use { output ->
                    input.copyTo(output)
                }
            }
            writeResultCallback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: Exception) {
            writeResultCallback.onWriteFailed(e.message)
        }
    }
}

// Utilities to persist page rotations into the PDF bytes using iText (v5)
private fun applyPageRotations(pdfBytes: ByteArray, rotations: Map<Int, Int>): ByteArray {
    try {
        val reader = com.itextpdf.text.pdf.PdfReader(pdfBytes)
        val baos = java.io.ByteArrayOutputStream()
        val stamper = com.itextpdf.text.pdf.PdfStamper(reader, baos)
        val total = reader.numberOfPages
        for ((pageIndex, delta) in rotations) {
            // PdfViewer pages are typically zero-based; PdfReader pages are 1-based
            val pageNum = (pageIndex + 1).coerceIn(1, total)
            val pageDict = reader.getPageN(pageNum)
            val existing = pageDict.getAsNumber(com.itextpdf.text.pdf.PdfName.ROTATE)
            val currentAngle = existing?.intValue() ?: 0
            var newAngle = (currentAngle + delta) % 360
            if (newAngle < 0) newAngle += 360
            pageDict.put(com.itextpdf.text.pdf.PdfName.ROTATE, com.itextpdf.text.pdf.PdfNumber(newAngle))
        }
        stamper.close()
        reader.close()
        return baos.toByteArray()
    } catch (e: Exception) {
        e.printStackTrace()
        return pdfBytes
    }
}