package com.gumirov.shamil.whatsthat.dagger.modules

import android.content.res.AssetManager
import dagger.Module
import dagger.Provides

@Module
class AssetManagerModule(private val assetManager: AssetManager) {
  @Provides
  fun appAssetManager(): AssetManager = assetManager
}
