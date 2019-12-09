package com.gumirov.shamil.whatsthat

import android.app.Application
import com.gumirov.shamil.whatsthat.dagger.DaggerSingletonComponent
import com.gumirov.shamil.whatsthat.dagger.SingletonComponent
import com.gumirov.shamil.whatsthat.dagger.modules.AssetManagerModule

class WhatsThatApplication
  : Application()
{
  override fun onCreate() {
    super.onCreate()
    component = DaggerSingletonComponent.builder().
        assetManagerModule(AssetManagerModule(assets)).
        build()
  }

  companion object {
    lateinit var component: SingletonComponent
      private set
  }
}
