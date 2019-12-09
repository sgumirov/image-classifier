package com.gumirov.shamil.whatsthat

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity
  : AppCompatActivity()
{
  private lateinit var container: FrameLayout

  override fun onCreate(savedInstanceState: Bundle?) {
    WhatsThatApplication.component.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    container = findViewById(R.id.fragment_container)
  }

  override fun onResume() {
    super.onResume()
    // Before setting full screen flags, we must wait a bit to let UI settle; otherwise, we may
    // be trying to set app to immersive mode before it's ready and the flags do not stick
    container.postDelayed({
      container.systemUiVisibility = FLAGS_FULLSCREEN
    }, IMMERSIVE_FLAG_TIMEOUT)
  }

  companion object {
    private const val IMMERSIVE_FLAG_TIMEOUT = 500L
    const val FLAGS_FULLSCREEN =
        View.SYSTEM_UI_FLAG_LOW_PROFILE or
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
  }
}
