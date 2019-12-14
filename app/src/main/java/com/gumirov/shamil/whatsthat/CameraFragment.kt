/*
 * (c) 2019 by Shamil Gumirov
 * Licensed under GNU GPL 3.0
 */
package com.gumirov.shamil.whatsthat

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.TextureView
import android.view.View
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraX
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysisConfig
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.android.example.cameraxbasic.utils.AutoFitPreviewBuilder
import com.gumirov.shamil.whatsthat.dagger.BaseFragment
import com.gumirov.shamil.whatsthat.databinding.FragmentCameraBinding
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executor
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Camera fragment. Implements all camera operations including:
 * - Viewfinder
 * - Image analysis
 */
class CameraFragment
  : BaseFragment<AnalysisResultViewModel, FragmentCameraBinding>(R.layout.fragment_camera, BR.viewmodel)
{
  private lateinit var container: ConstraintLayout
  private lateinit var viewFinder: TextureView
  private lateinit var mainExecutor: Executor

  private var displayId = -1
  private var lensFacing = CameraX.LensFacing.BACK
  private var preview: Preview? = null
  private var imageAnalyzer: ImageAnalysis? = null

  /** Internal reference of the [DisplayManager] */
  private lateinit var displayManager: DisplayManager

  /**
   * We need a display listener for orientation changes that do not trigger a configuration
   * change, for example if we choose to override config change in manifest or for 180-degree
   * orientation changes.
   */
  private val displayListener = object : DisplayManager.DisplayListener {
    override fun onDisplayAdded(displayId: Int) = Unit
    override fun onDisplayRemoved(displayId: Int) = Unit
    override fun onDisplayChanged(displayId: Int) = view?.let { view ->
      if (displayId == this@CameraFragment.displayId) {
        Log.d(TAG, "Rotation changed: ${view.display.rotation}")
        preview?.setTargetRotation(view.display.rotation)
        imageAnalyzer?.setTargetRotation(view.display.rotation)
      }
    } ?: Unit
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    WhatsThatApplication.component.inject(this)
    super.onActivityCreated(savedInstanceState)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Mark this as a retain fragment, so the lifecycle does not get restarted on config change
    retainInstance = true
    mainExecutor = ContextCompat.getMainExecutor(requireContext())
  }

  override fun onResume() {
    super.onResume()
    // Make sure that all permissions are still present, since user could have removed them
    //  while the app was on paused state
    if (!PermissionRequestFragment.hasPermissions(requireContext())) {
      Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
          CameraFragmentDirections.actionCameraToPermissions())
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()

    // Unregister the broadcast receivers and listeners
    displayManager.unregisterDisplayListener(displayListener)
  }

  @SuppressLint("MissingPermission")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    container = view as ConstraintLayout
    viewFinder = container.findViewById(R.id.view_finder)

    // Every time the orientation of device changes, recompute layout
    displayManager = viewFinder.context
        .getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    displayManager.registerDisplayListener(displayListener, null)

    // Wait for the views to be properly laid out
    viewFinder.post {
      // Keep track of the display in which this view is attached
      displayId = viewFinder.display.displayId

      // bind all camera use cases
      bindCameraUseCases()
    }
  }

  /** Declare and bind preview, capture and analysis use cases */
  private fun bindCameraUseCases() {
    // Get screen metrics used to setup camera for full screen resolution
    val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
    Log.d(TAG, "Screen metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")
    val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
    Log.d(TAG, "Preview aspect ratio: $screenAspectRatio")
    // Set up the view finder use case to display camera preview
    val viewFinderConfig = PreviewConfig.Builder().apply {
      setLensFacing(lensFacing)
      // We request aspect ratio but no resolution to let CameraX optimize our use cases
      setTargetAspectRatio(screenAspectRatio)
      // Set initial target rotation, we will have to call this again if rotation changes
      // during the lifecycle of this use case
      setTargetRotation(viewFinder.display.rotation)
    }.build()

    // Use the auto-fit preview builder to automatically handle size and orientation changes
    preview = AutoFitPreviewBuilder.build(viewFinderConfig, viewFinder)

    val analyzerConfig = ImageAnalysisConfig.Builder().apply {
      setLensFacing(lensFacing)
      // In our analysis, we care more about the latest image than analyzing *every* image
      setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
      // Set initial target rotation, we will have to call this again if rotation changes
      // during the lifecycle of this use case
      setTargetRotation(viewFinder.display.rotation)
    }.build()

    imageAnalyzer = ImageAnalysis(analyzerConfig).apply {
      setAnalyzer(mainExecutor, ImageAnalysis.Analyzer { image: ImageProxy, _: Int ->
        val bitmap = image.toBitmap()
        viewModel.image.postValue(bitmap)
      })
    }

    // Apply declared configs to CameraX using the same lifecycle owner
    CameraX.bindToLifecycle(viewLifecycleOwner, preview, imageAnalyzer)
  }

  /**
   *  [androidx.camera.core.ImageAnalysisConfig] requires enum value of
   *  [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
   *
   *  Detecting the most suitable ratio for dimensions provided in @params by counting absolute
   *  of preview ratio to one of the provided values.
   *
   *  @param width - preview width
   *  @param height - preview height
   *  @return suitable aspect ratio
   */
  private fun aspectRatio(width: Int, height: Int): AspectRatio {
    val previewRatio = max(width, height).toDouble() / min(width, height)

    if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
      return AspectRatio.RATIO_4_3
    }
    return AspectRatio.RATIO_16_9
  }

  companion object {
    private const val TAG = "CameraXBasic"
    private const val RATIO_4_3_VALUE = 4.0 / 3.0
    private const val RATIO_16_9_VALUE = 16.0 / 9.0

    fun ImageProxy.toBitmap(): Bitmap {
      val yBuffer = planes[0].buffer // Y
      val uBuffer = planes[1].buffer // U
      val vBuffer = planes[2].buffer // V

      val ySize = yBuffer.remaining()
      val uSize = uBuffer.remaining()
      val vSize = vBuffer.remaining()

      val nv21 = ByteArray(ySize + uSize + vSize)

      yBuffer.get(nv21, 0, ySize)
      vBuffer.get(nv21, ySize, vSize)
      uBuffer.get(nv21, ySize + vSize, uSize)

      val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
      val out = ByteArrayOutputStream()
      yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
      val imageBytes = out.toByteArray()
      return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
  }
}
