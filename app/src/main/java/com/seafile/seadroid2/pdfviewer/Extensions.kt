package com.seafile.seadroid2.pdfviewer

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.ColorUtils
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

fun Activity.getDataFromIntent(): Pair<String, String>? {
    val filePath: String
    val fileName: String

    // View from other apps (from intent filter)
    if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
        filePath = intent.data.toString()
        fileName = intent.data!!.getFileName(this)
    } else {
        // Path from asset, url or android uri
        filePath = intent.extras?.getString("filePath")
            ?: intent.extras?.getString("fileUrl")
                    ?: intent.extras?.getString("fileUri")
                    ?: return null

        fileName = intent.extras?.getString("fileUri")?.toUri()?.getFileName(this)
            ?: intent.extras?.getString("fileName") ?: ""
    }

    return filePath to fileName
}

fun Uri.getFileName(context: Context): String {
    var name = "UNKNOWN"
    val cursor: Cursor? = context.contentResolver.query(
        this,
        arrayOf(OpenableColumns.DISPLAY_NAME),
        null,
        null,
        null
    )

    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                name = it.getString(nameIndex)
            }
        }
    }

    return name
}

fun Activity.setFullscreen(fullscreen: Boolean) {
    val window = window ?: return
    val insetsController = WindowCompat.getInsetsController(window, window.decorView)

    insetsController.apply {
        systemBarsBehavior = if (fullscreen) {
            hide(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.navigationBars())
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            show(WindowInsetsCompat.Type.statusBars())
            show(WindowInsetsCompat.Type.navigationBars())
            WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        }
    }

}

fun <T> Activity.setComponentEnabled(componentClass: Class<T>, enable: Boolean) {
    packageManager.setComponentEnabledSetting(
        ComponentName(this, componentClass),
        if (enable) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.DONT_KILL_APP
    )
}

fun Context.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun EditText.requestKeyboard() {
    requestFocus()
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

@Suppress("NOTHING_TO_INLINE")
fun View.find(id: Int): TextView {
    return findViewById(id)
}

fun Long.formatToSize(): String {
    if (this <= 0) return "0 B"
    val unit = when {
        this < 1024 -> "B"
        this < 1024 * 1024 -> "KB"
        this < 1024 * 1024 * 1024 -> "MB"
        this < 1024L * 1024 * 1024 * 1024 -> "GB"
        this < 1024L * 1024 * 1024 * 1024 * 1024 -> "TB"
        else -> "PB"
    }
    val value = when (unit) {
        "B" -> this.toDouble()
        "KB" -> this / 1024.0
        "MB" -> this / (1024.0 * 1024)
        "GB" -> this / (1024.0 * 1024 * 1024)
        "TB" -> this / (1024.0 * 1024 * 1024 * 1024)
        "PB" -> this / (1024.0 * 1024 * 1024 * 1024 * 1024)
        else -> 0.0
    }
    return "%.1f %s".format(value, unit)
}

fun String.formatToDate(): String {
    val cleanDate = if (this.startsWith("D:")) this.substring(2) else this
    if (cleanDate.length < 14) return this
    val rawDate = cleanDate.substring(0, 14)

    val parser = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
    val formatter = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault())

    try {
        val date = parser.parse(rawDate)
        if (date != null)
            return formatter.format(date)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return "Invalid Date"
}

internal fun View.setBgTintModes(color: Int) {
    backgroundTintList = ColorStateList(
        arrayOf(
            intArrayOf(android.R.attr.state_enabled),
            intArrayOf(-android.R.attr.state_enabled)
        ),
        intArrayOf(color, ColorUtils.setAlphaComponent(color, 150))
    )
}

fun Context.dpToPx(dp: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics
    ).roundToInt()
}

internal fun findSelectedOption(options: Array<String>, currentValue: String): Int {
    options.forEachIndexed { index, option ->
        if (option == currentValue)
            return index
    }
    return -1
}