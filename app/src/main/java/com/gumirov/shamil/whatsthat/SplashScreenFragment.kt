/*
 * (c) 2019 by Shamil Gumirov
 * Licensed under GNU GPL 3.0
 */
package com.gumirov.shamil.whatsthat

import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.transition.Fade
import androidx.transition.Scene
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import androidx.transition.TransitionManager
import kotlinx.android.synthetic.main.fragment_splash_screen.*

class SplashScreenFragment
  : Fragment()
{
  lateinit var root: View

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    root = inflater.inflate(R.layout.fragment_splash_screen, container, false)
    return root
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    setupAnimation1()
  }

  private fun setupAnimation1() {
    val scene0 = Scene.getSceneForLayout(scene_root, R.layout.fragment_splash_screen_start, requireContext())
    val scene1 = Scene.getSceneForLayout(scene_root, R.layout.fragment_splash_screen_middle, requireContext())
    val scene2 = Scene.getSceneForLayout(scene_root, R.layout.fragment_splash_screen_end, requireContext())
    val scene3 = Scene.getSceneForLayout(scene_root, R.layout.fragment_splash_screen_end_permissions, requireContext())
    scene0.enter()
    val to3 = Fade(Fade.MODE_IN).also { it -> it.setDuration(1000) }
    to3.interpolator = AccelerateDecelerateInterpolator()
    to3.targetIds.clear()
    to3.addTarget(R.id.splash_ask_permission)
    to3.addListener(object: TransitionListenerAdapter(){
      override fun onTransitionEnd(transition: Transition) {
        Handler().postDelayed({
          findNavController().navigate(SplashScreenFragmentDirections.splashToPermissions())
        }, 1000)
      }
    })
    val to2 = Fade(Fade.MODE_IN).also { it -> it.setDuration(500) }
    to2.interpolator = AccelerateDecelerateInterpolator()
    to2.targetIds.clear()
    to2.addTarget(R.id.splash_logo)
    to2.addTarget(R.id.splash_description)
    to2.addListener(object: TransitionListenerAdapter(){
      override fun onTransitionEnd(transition: Transition) {
        if (PermissionRequestFragment.hasPermissions(requireContext()))
          findNavController().navigate(SplashScreenFragmentDirections.splashToPermissions())
        else
          TransitionManager.go(scene3, to3)
      }
    })
    val to1 = Slide(Gravity.TOP).also { it -> it.setDuration(800) }
    to2.interpolator = AccelerateDecelerateInterpolator()
    to1.addListener(object: TransitionListenerAdapter(){
      override fun onTransitionEnd(transition: Transition) {
        TransitionManager.go(scene2, to2)
      }
    })
    TransitionManager.go(scene1, to1)
  }
}
