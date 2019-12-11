package com.gumirov.shamil.whatsthat

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.gumirov.shamil.whatsthat.classifier.ImageClassifier
import javax.inject.Inject

class AnalysisResultViewModel @Inject constructor(private val classifier: ImageClassifier)
  : ViewModel()
{
  val image = MutableLiveData<Bitmap>()
  private val resultList = Transformations.map(image) { classifier.recognizeImage(it) }
  val result = Transformations.map(resultList) {
    if (it?.size ?: 0 > 0) String.format("%s (%.1f%%)", it[0].title, it[0].confidence * 100f) else "Definitely not a hotdog" }
}
