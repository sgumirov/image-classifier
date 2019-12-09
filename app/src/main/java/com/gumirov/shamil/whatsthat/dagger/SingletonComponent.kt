package com.gumirov.shamil.whatsthat.dagger

import com.gumirov.shamil.whatsthat.CameraFragment
import com.gumirov.shamil.whatsthat.MainActivity
import com.gumirov.shamil.whatsthat.dagger.modules.AssetManagerModule
import com.gumirov.shamil.whatsthat.dagger.modules.TensorFlowModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
  AssetManagerModule::class,
  TensorFlowModule::class
])
interface SingletonComponent {
  //Activities:
  fun inject(m: MainActivity)

  //Fragments:
  fun inject(fragment: CameraFragment)
}
