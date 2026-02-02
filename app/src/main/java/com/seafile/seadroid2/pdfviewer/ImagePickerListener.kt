package com.seafile.seadroid2.pdfviewer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient.FileChooserParams
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bhuvaneshw.pdf.PdfListener

class ImagePickerListener(activity: ComponentActivity) : PdfListener {

    private var fileChooserCallback: ValueCallback<Array<out Uri?>?>? = null
    private val fileChooserLauncher: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val uris = FileChooserParams.parseResult(result.resultCode, result.data)
                fileChooserCallback?.onReceiveValue(uris)
            } else {
                fileChooserCallback?.onReceiveValue(null)
            }
            fileChooserCallback = null
        }

    override fun onShowFileChooser(
        filePathCallback: ValueCallback<Array<out Uri?>?>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        fileChooserCallback?.onReceiveValue(null)
        fileChooserCallback = filePathCallback

        val intent = fileChooserParams?.createIntent()
        if (intent != null) {
            fileChooserLauncher.launch(intent)
            return true
        }

        return false
    }

}
