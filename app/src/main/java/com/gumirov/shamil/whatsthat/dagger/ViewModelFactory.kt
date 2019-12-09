package com.gumirov.shamil.whatsthat.dagger

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders

@Suppress("UNCHECKED_CAST")
inline fun <reified T : ViewModel> FragmentActivity.getViewModel(crossinline createViewModelFunc: () -> T): T {
  return T::class.java.let {
    ViewModelProviders.of(this, object : ViewModelProvider.Factory {
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return createViewModelFunc() as T
      }
    }).get(it)
  }
}

inline fun <reified T : ViewModel> viewModel(activity: FragmentActivity, crossinline createViewModelFunc: () -> T): Lazy<T> {
  return lazy(LazyThreadSafetyMode.NONE) {
    activity.getViewModel { createViewModelFunc() }
  }
}
