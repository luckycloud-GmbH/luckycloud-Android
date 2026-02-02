package com.seafile.seadroid2.share

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import org.hamcrest.CoreMatchers.anything
import org.hamcrest.Matchers.allOf
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeDiagnosingMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

import com.seafile.seadroid2.R
import com.seafile.seadroid2.data.SeafDirent
import com.seafile.seadroid2.ui.activity.ShareToSeafileActivity
import androidx.test.core.app.ActivityScenario

@RunWith(AndroidJUnit4::class)
@LargeTest
class ShareToSeafileEspressoE2eTest {

    @Rule @JvmField
    val storagePerms: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    @Test
    fun shareAndChooseFolder_withEspresso() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val targetFolder = "test-folder"

        // Prepare a test file and content Uri via FileProvider
        val base = ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: ctx.filesDir
        val testDir = File(base, "espresso-share-test").apply { mkdirs() }
        val testFile = File(testDir, "sample.txt").apply { writeText("hello luckycloud") }
        val uri = FileProvider.getUriForFile(ctx, ctx.packageName, testFile)

        val intent = Intent(ctx, ShareToSeafileActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "*/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        ActivityScenario.launch<ShareToSeafileActivity>(intent).use {
            // Wait for list to render
            onView(withId(android.R.id.list)).check(matches(isDisplayed()))
            waitMs(700)

            // Try to navigate (account -> repo -> dir) until folder becomes visible
            var clicked = tryClickFolder(targetFolder)
            var attempts = 0
            while (!clicked && attempts < 3) {
                // Click first item to advance selection
                try {
                    onData(anything())
                        .inAdapterView(withId(android.R.id.list))
                        .atPosition(0)
                        .perform(click())
                } catch (_: Throwable) { /* ignore */ }
                waitMs(800)
                clicked = tryClickFolder(targetFolder)
                attempts++
            }

            // Confirm selection
            onView(withId(R.id.ok_card)).perform(click())
        }
    }

    private fun seafDirentWithName(name: String): Matcher<Any> = object : TypeSafeDiagnosingMatcher<Any>() {
        override fun describeTo(description: Description) {
            description.appendText("SeafDirent with name \"").appendText(name).appendText("\"")
        }

        override fun matchesSafely(item: Any, mismatchDescription: Description): Boolean {
            if (item !is SeafDirent) {
                mismatchDescription.appendText("item is not SeafDirent")
                return false
            }
            val ok = item.name == name && item.isDir
            if (!ok) mismatchDescription.appendText("got \"").appendText(item.name).appendText("\"")
            return ok
        }
    }

    private fun tryClickFolder(name: String): Boolean {
        return try {
            onData(seafDirentWithName(name))
                .inAdapterView(withId(android.R.id.list))
                .perform(click())
            true
        } catch (t: Throwable) {
            try {
                onView(allOf(withId(R.id.list_item_title), withText(name)))
                    .perform(click())
                true
            } catch (_: Throwable) {
                false
            }
        }
    }

    private fun waitMs(ms: Long) {
        try { Thread.sleep(ms) } catch (_: InterruptedException) {}
    }
}
