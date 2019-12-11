package com.gumirov.shamil.whatsthat

import android.graphics.Bitmap
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.gumirov.shamil.whatsthat.classifier.ImageClassifier
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(manifest=Config.NONE)
@RunWith(RobolectricTestRunner::class)
class AnalysisResultViewModelTest
{
  @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

  @Mock private lateinit var classifier: ImageClassifier

  private lateinit var viewModel: AnalysisResultViewModel
  private lateinit var bitmap: Bitmap
  private val result = listOf(ImageClassifier.Result("id", "title", 1f))

  @Before
  fun setup() {
    bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    MockitoAnnotations.initMocks(this)
    viewModel = AnalysisResultViewModel(classifier)
    //mock method call
    `when`(classifier.recognizeImage(any())).thenAnswer {
      return@thenAnswer result
    }
  }

  @Test
  fun testViewModel() {
    //mock observer
    val observer = mock<Observer<String>>()
    viewModel.result.observeForever(observer)
    //execute and assert
    viewModel.image.postValue(bitmap)
    Assert.assertEquals(String.format("%s (%.1f%%)", result[0].title, result[0].confidence * 100f), viewModel.result.value?.toString())
  }
}
