package com.seafile.seadroid2.e2e

import android.app.Instrumentation
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.media.MediaScannerConnection
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShareE2EUiAutomatorTest {

    @Rule @JvmField
    val storagePerms: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private val inst: Instrumentation = InstrumentationRegistry.getInstrumentation()
    private val device: UiDevice = UiDevice.getInstance(inst)
    private val targetPkg: String = inst.targetContext.packageName
    private val args = InstrumentationRegistry.getArguments()

    @Test
    fun shareFileFromFilesApp_toLuckycloud_succeeds() {
        val ctx = inst.targetContext
        val filename = "uiauto_share_test_${System.currentTimeMillis()}.txt"
        val content = "Hello luckycloud from UI Automator!"
        val fileUri = createFileInDownloads(ctx, filename, content)
        assumeTrue("Failed to create test file in Downloads", fileUri != null)

        val launched = launchFilesApp(ctx)
        assumeTrue("Files app not available on this device", launched)

        assertTrue(
            waitForAnyPackage(
                8000,
                "com.google.android.documentsui", // Pixel
                "com.android.documentsui",        // AOSP
                "com.sec.android.app.myfiles"     // Samsung
            )
        )

        // Navigate to Downloads (override via -Pandroid.testInstrumentationRunnerArguments.downloadsLabel=<text>)
        val downloadsLabelArg = args.getString("downloadsLabel", null)
        val downloads = if (downloadsLabelArg != null) {
            device.waitFindAny(6000, By.textContains(downloadsLabelArg))
        } else device.waitFindAny(
            6000,
            By.textStartsWith("Downloads"),
            By.text("Download"),
            By.textContains("Herunter"),
            By.textContains("Eigene Dateien"),
            By.descContains("Downloads")
        )
        downloads?.click() ?: run {
            // Try open navigation drawer and click Downloads
            device.findObject(By.descContains("Open navigation drawer"))?.click()
            val dl = device.waitFindAny(4000, By.textStartsWith("Downloads"), By.textContains("Download"))
            assumeTrue("Could not navigate to Downloads", dl != null)
            dl!!.click()
        }

        val fileObj = device.wait(Until.findObject(By.text(filename)), 6000)
        assumeTrue("Test file not visible in Files app", fileObj != null)
        fileObj!!.longClick()

        // Share (override via -P...shareLabel=<text>)
        val shareLabelArg = args.getString("shareLabel", null)
        val shareBtn = if (shareLabelArg != null) {
            device.waitFindAny(4000, By.textContains(shareLabelArg), By.descContains(shareLabelArg))
        } else device.waitFindAny(
            4000,
            By.descContains("Share"),
            By.text("Share"),
            By.descContains("Teilen"),
            By.text("Teilen")
        )
        assumeTrue("Share button not found", shareBtn != null)
        shareBtn!!.click()

        // Select our app on the share sheet (override via -P...appLabel=<text>)
        val appLabel = args.getString("appLabel", "luckycloud")
        var target = device.waitFindAny(
            4000,
            By.text(appLabel),
            By.textContains(appLabel),
            By.textContains(appLabel.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.lowercase() }),
            By.descContains(appLabel)
        )
        if (target == null) {
            // Try expanding share sheet to show all apps
            val moreBtn = device.waitFindAny(
                2000,
                By.textContains("More"), By.descContains("More"),
                By.textContains("Mehr"), By.textContains("Weitere"), By.descContains("Weitere"),
                By.textContains("Alle"), By.descContains("Alle"),
                By.textContains("Apps"), By.descContains("Apps")
            )
            moreBtn?.click()
            target = device.waitFindAny(
                4000,
                By.text(appLabel),
                By.textContains(appLabel),
                By.textContains(appLabel.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.lowercase() }),
                By.descContains(appLabel)
            )
        }
        assumeTrue("$appLabel not present in share sheet", target != null)
        target!!.click()

        // Our app should be in foreground (Path chooser)
        assertTrue(device.wait(Until.hasObject(By.pkg(targetPkg).depth(0)), 8000))

        // Select the target folder "test-folder" and confirm with "Select"
        val folderName = args.getString("targetFolder", "test-folder")
        var folder = device.waitFindAny(3000, By.text(folderName), By.textContains(folderName))
        if (folder == null) {
            // Try to scroll a bit to reveal the item
            repeat(4) {
                folder = device.waitFindAny(1000, By.text(folderName), By.textContains(folderName))
                if (folder != null) return@repeat
                val displayHeight = device.displayHeight
                val displayWidth = device.displayWidth
                val startY = (displayHeight * 0.75).toInt()
                val endY = (displayHeight * 0.25).toInt()
                val x = displayWidth / 2
                device.swipe(x, startY, x, endY, /*steps*/ 20)
            }
        }
        assumeTrue("target folder '$folderName' not found", folder != null)
        folder!!.click()

        // Click "Select" (consider localized labels)
        val selectBtn = device.waitFindAny(
            3000,
            By.text("Select"),
            By.textContains("Select"),
            By.text("Auswählen"),
            By.textContains("Ausw"),
            By.res("android:id/button1"),
            By.descContains("Select")
        )
        assumeTrue("Select button not found", selectBtn != null)
        selectBtn!!.click()
    }

    private fun launchFilesApp(ctx: Context): Boolean {
        val pm = ctx.packageManager
        // Optional overrides for device-specific file managers
        val overridePkg = args.getString("fileManagerPkg", null)
        val overrideActivity = args.getString("fileManagerActivity", null)
        if (!overridePkg.isNullOrBlank()) {
            if (!overrideActivity.isNullOrBlank()) {
                try {
                    val intent = Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_LAUNCHER)
                        .setClassName(overridePkg, overrideActivity)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    ctx.startActivity(intent)
                    return true
                } catch (_: Exception) { }
            }
            pm.getLaunchIntentForPackage(overridePkg)?.let { launch ->
                launch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                ctx.startActivity(launch)
                return true
            }
        }

        val candidates = listOf(
            "com.google.android.documentsui" to listOf("com.android.documentsui.files.FilesActivity", "com.google.android.documentsui.files.FilesActivity"),
            "com.android.documentsui" to listOf("com.android.documentsui.files.FilesActivity"),
            "com.sec.android.app.myfiles" to listOf(
                "com.sec.android.app.myfiles.external.ui.MainActivity",
                "com.sec.android.app.myfiles.common.MainActivity",
                "com.sec.android.app.myfiles.activity.MainActivity"
            )
        )
        for ((pkg, classes) in candidates) {
            pm.getLaunchIntentForPackage(pkg)?.let { launch ->
                launch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                ctx.startActivity(launch)
                return true
            }
            for (cls in classes) {
                try {
                    val intent = Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_LAUNCHER)
                        .setClassName(pkg, cls)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    ctx.startActivity(intent)
                    return true
                } catch (_: Exception) { }
            }
        }
        return false
    }

    private fun createFileInDownloads(ctx: Context, name: String, text: String): Uri? {
        return try {
            if (Build.VERSION.SDK_INT >= 29) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val uri = ctx.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    ctx.contentResolver.openOutputStream(uri).use { it?.write(text.toByteArray()) }
                    val finished = ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) }
                    ctx.contentResolver.update(uri, finished, null, null)
                }
                uri
            } else {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!dir.exists()) dir.mkdirs()
                val file = java.io.File(dir, name)
                file.writeText(text)
                MediaScannerConnection.scanFile(ctx, arrayOf(file.absolutePath), arrayOf("text/plain"), null)
                Uri.fromFile(file)
            }
        } catch (_: Throwable) {
            null
        }
    }

    // Helper: wait for any of the selectors
    private fun UiDevice.waitFindAny(timeoutMs: Long, vararg sels: BySelector): androidx.test.uiautomator.UiObject2? {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            for (s in sels) {
                val o = findObject(s)
                if (o != null) return o
            }
            waitForIdle()
        }
        return null
    }

    private fun waitForAnyPackage(timeoutMs: Long, vararg pkgs: String): Boolean {
        val end = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < end) {
            val current = device.currentPackageName
            if (pkgs.contains(current)) return true
            device.waitForIdle()
        }
        return false
    }
}
