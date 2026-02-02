package com.seafile.seadroid2.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import android.os.Environment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.ComponentNameMatchers
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.BundleMatchers
import android.app.Instrumentation.ActivityResult
import com.seafile.seadroid2.ui.activity.ShareToSeafileActivity
import com.seafile.seadroid2.ui.activity.SeafilePathChooserActivity
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import org.junit.Rule
import org.hamcrest.core.AllOf.allOf
import android.app.Activity
import androidx.test.espresso.intent.Intents.release
import androidx.test.espresso.intent.Intents.init
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction

@RunWith(AndroidJUnit4::class)
class ShareToSeafileActivityShareTest {

    @Rule @JvmField
    val storagePerms: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    @Rule @JvmField
    val intentsRule = IntentsRule()

    private fun createTempContentUri(context: Context, name: String, content: String = "demo"): Uri {
        val base = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
        val dir = File(base, "share-test").apply { mkdirs() }
        val file = File(dir, name)
        file.writeText(content)
        val authority = context.packageName // matches provider authority ${applicationId}
        return FileProvider.getUriForFile(context, authority, file)
    }

    @Test
    fun
            launchWithActionSend_singleFile_doesNotCrash() {
        val target = ApplicationProvider.getApplicationContext<Context>()
        val uri = createTempContentUri(target, "sample.txt", "hello luckycloud")

        // Stub next activity to avoid UI dependencies
        intending(hasComponent(SeafilePathChooserActivity::class.java.name))
            .respondWith(ActivityResult(Activity.RESULT_CANCELED, null))

        val intent = Intent(ApplicationProvider.getApplicationContext(), ShareToSeafileActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "*/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        ActivityScenario.launch<ShareToSeafileActivity>(intent).use { _ -> }
    }

    @Test
    fun launchWithActionSendMultiple_multipleFiles_doesNotCrash() {
        val target = ApplicationProvider.getApplicationContext<Context>()
        val uri1 = createTempContentUri(target, "one.txt", "one")
        val uri2 = createTempContentUri(target, "two.txt", "two")

        intending(hasComponent(SeafilePathChooserActivity::class.java.name))
            .respondWith(ActivityResult(Activity.RESULT_CANCELED, null))

        val intent = Intent(ApplicationProvider.getApplicationContext(), ShareToSeafileActivity::class.java).apply {
            action = Intent.ACTION_SEND_MULTIPLE
            type = "*/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayListOf(uri1, uri2))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        ActivityScenario.launch<ShareToSeafileActivity>(intent).use { _ -> }
    }
}
