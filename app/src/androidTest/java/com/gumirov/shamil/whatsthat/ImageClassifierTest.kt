/*
 * (c) 2019 by Shamil Gumirov
 * Licensed under GNU GPL 3.0
 */
package com.gumirov.shamil.whatsthat

import android.Manifest
import android.graphics.BitmapFactory
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class ImageClassifierTest {
  @get:Rule
  val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

  @Test
  fun useAppContext() {
    val banana = BitmapFactory.decodeStream(
      InstrumentationRegistry.getInstrumentation().context.assets.open("banana.jpg")
    )
    val scenario = launchFragmentInContainer<CameraFragment>()

    scenario.onFragment { fragment ->
      fragment.viewModel.image.value = banana
    }
    onView(withId(R.id.result)).check(matches(withSubstring("banana ")))
  }
}
