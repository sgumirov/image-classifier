package com.gumirov.shamil.whatsthat.dagger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.civoli.dagger.Injectable
import javax.inject.Inject

abstract class BaseFragment<V: ViewModel, B: ViewDataBinding>(
    private val layoutId: Int, private val viewModelId: Int)
  : Injectable, Fragment()
{
  @Inject lateinit var viewModel: V
  lateinit var binding: B

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    binding = DataBindingUtil.inflate(
        inflater,
        layoutId,
        container,
        false
    )
    return binding.root
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    val viewModelFactory = viewModel.createFactory()
    ViewModelProviders.of(this, viewModelFactory).get(viewModel.javaClass)

    binding.setVariable(viewModelId, this.viewModel)
    binding.lifecycleOwner = this
  }
}
