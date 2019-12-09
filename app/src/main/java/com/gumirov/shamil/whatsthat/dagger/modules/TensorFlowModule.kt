package com.gumirov.shamil.whatsthat.dagger.modules

import android.content.res.AssetManager
import com.gumirov.shamil.whatsthat.classifier.ClassifierFloatMobileNet
import com.gumirov.shamil.whatsthat.classifier.ClassifierQuantizedMobileNet
import com.gumirov.shamil.whatsthat.classifier.ImageClassifier
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [AssetManagerModule::class])
class TensorFlowModule {
  @Provides @Singleton
  fun appTensorFlowClassifier(assetManager: AssetManager): ImageClassifier =
    ClassifierFloatMobileNet(assetManager)
}
