package com.gumirov.shamil.whatsthat.classifier

import android.content.res.AssetManager
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.SupportPreconditions
import org.tensorflow.lite.support.common.TensorOperator
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*

abstract class ImageClassifier(assetManager: AssetManager)
{
  /** The loaded TensorFlow Lite model */
  private val tfliteModel: MappedByteBuffer

  /** Input image dimension */
  private var imageSizeX: Int = 0
  /** Input image dimension */
  private var imageSizeY: Int = 0

  /** An instance of the driver class to run model inference with Tensorflow Lite */
  protected val tflite: Interpreter

  /** Options for configuring the Interpreter. */
  private val tfliteOptions = Interpreter.Options()

  /** Labels corresponding to the output of the vision model.  */
  private val labels: List<String>

  /** Input image TensorBuffer.  */
  private var inputImageBuffer: TensorImage

  /** Output probability TensorBuffer.  */
  private val outputProbabilityBuffer: TensorBuffer

  /** Processer to apply post processing of the output probability.  */
  private val probabilityProcessor: TensorProcessor

  init {
    tfliteModel = loadMappedFile(assetManager, getModelPath())
    tfliteOptions.setNumThreads(NUM_THREADS)
    tflite = Interpreter(tfliteModel, tfliteOptions)

    // Loads labels out from the label file.
    labels = loadLabels(assetManager, getLabelPath())

    // Reads type and shape of input and output tensors, respectively.
    val imageTensorIndex = 0
    val imageShape = tflite.getInputTensor(imageTensorIndex).shape() // {1, height, width, 3}
    imageSizeY = imageShape[1]
    imageSizeX = imageShape[2]
    val imageDataType = tflite.getInputTensor(imageTensorIndex).dataType()
    val probabilityTensorIndex = 0
    val probabilityShape =
      tflite.getOutputTensor(probabilityTensorIndex).shape() // {1, NUM_CLASSES}
    val probabilityDataType = tflite.getOutputTensor(probabilityTensorIndex).dataType()

    // Creates the input tensor.
    inputImageBuffer = TensorImage(imageDataType)

    // Creates the output tensor and its processor.
    outputProbabilityBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType)

    probabilityProcessor = TensorProcessor.Builder().add(getPostprocessNormalizeOp()).build()
  }

  /** Runs inference and returns the classification results.  */
  open fun recognizeImage(bitmap: Bitmap): List<Result> {
    inputImageBuffer = loadImage(bitmap, 0)
    // Runs the inference call.
    tflite.run(inputImageBuffer.getBuffer(), outputProbabilityBuffer.buffer.rewind())
    // Gets the map of label and probability.
    val labeledProbability =
      TensorLabel(labels, probabilityProcessor.process(outputProbabilityBuffer))
        .getMapWithFloatValue()
    // Gets top-k results.
    return getTopKProbability(labeledProbability)
  }

  private fun loadImage(bitmap: Bitmap, sensorOrientation: Int): TensorImage {
    // Loads bitmap into a TensorImage.
    inputImageBuffer.load(bitmap)
    // Creates processor for the TensorImage.
    val cropSize = Math.min(bitmap.width, bitmap.height)
    val numRoration = sensorOrientation / 90
    val imageProcessor = ImageProcessor.Builder()
      .add(ResizeWithCropOrPadOp(cropSize, cropSize))
      .add(ResizeOp(imageSizeX, imageSizeY, ResizeOp.ResizeMethod.BILINEAR))
      .add(Rot90Op(numRoration))
      .add(getPreprocessNormalizeOp())
      .build()
    return imageProcessor.process(inputImageBuffer)
  }

  /**
   * Gets the TensorOperator to dequantize the output probability in post processing.
   *
   * <p>For quantized model, we need de-quantize the prediction with NormalizeOp (as they are all
   * essentially linear transformation). For float model, de-quantize is not required. But to
   * uniform the API, de-quantize is added to float model too. Mean and std are set to 0.0f and
   * 1.0f, respectively.
   */
  abstract fun getPostprocessNormalizeOp(): TensorOperator?

  /** labels to laod from */
  abstract fun getLabelPath(): String

  /** Model path to laod from */
  abstract fun getModelPath(): String

  /** Gets the TensorOperator to nomalize the input image in preprocessing.  */
  protected abstract fun getPreprocessNormalizeOp(): TensorOperator

  /** Closes the interpreter and model to release resources.  */
  fun close() {
    tflite.close()
  }

  /** An immutable result returned by a Classifier describing what was recognized.  */
  class Result(
    /**
     * A unique identifier for what has been recognized. Specific to the class, not the instance of
     * the object.
     */
    val id: String?,
    /** Display name for the recognition.  */
    val title: String?,
    /**
     * A sortable score for how good the recognition is relative to others. Higher should be better.
     */
    val confidence: Float
  ) {
    override fun toString(): String {
      var resultString = ""
      if (id != null) {
        resultString += "[$id] "
      }
      if (title != null) {
        resultString += "$title "
      }
      resultString += String.format("(%.1f%%) ", confidence * 100.0f)
      return resultString.trim { it <= ' ' }
    }
  }

  companion object {
    private const val MAX_RESULTS = 1
    private const val NUM_THREADS = 2

    @Throws(IOException::class)
    fun loadLabels(assetManager: AssetManager, filePath: String): List<String> {
      SupportPreconditions.checkNotNull(filePath, "File path cannot be null.")
      val labels = ArrayList<String>()
      val reader = BufferedReader(InputStreamReader(assetManager.open(filePath)))

      var line = reader.readLine()
      while (line != null) {
        labels.add(line)
        line = reader.readLine()
      }

      reader.close()
      return labels
    }

    @Throws(IOException::class)
    fun loadMappedFile(assetManager: AssetManager, filePath: String): MappedByteBuffer {
      SupportPreconditions.checkNotNull(filePath, "File path cannot be null.")
      val fileDescriptor = assetManager.openFd(filePath)
      val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
      val fileChannel = inputStream.channel
      val startOffset = fileDescriptor.startOffset
      val declaredLength = fileDescriptor.declaredLength
      return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /** Gets the top-k results.  */
    private fun getTopKProbability(labelProb: Map<String, Float>): List<Result> {
      // Find the best classifications.
      val pq = PriorityQueue<Result>(
        MAX_RESULTS,
        Comparator<Result> { lhs, rhs ->
          // Intentionally reversed to put high confidence at the head of the queue.
          java.lang.Float.compare(rhs.confidence, lhs.confidence)
        })

      for ((key, value) in labelProb) {
        pq.add(Result("" + key, key, value))
      }

      val recognitions = ArrayList<Result>()
      val recognitionsSize = Math.min(pq.size, MAX_RESULTS)
      for (i in 0 until recognitionsSize) {
        recognitions.add(pq.poll())
      }
      return recognitions
    }
  }
}

class ClassifierFloatMobileNet @Throws(IOException::class) constructor(assetManager: AssetManager)
  : ImageClassifier(assetManager)
{
  override fun getLabelPath(): String = "labels.txt"
  override fun getModelPath(): String = "mobilenet_v1_1.0_224.tflite"
  override fun getPreprocessNormalizeOp(): TensorOperator = NormalizeOp(IMAGE_MEAN, IMAGE_STD)
  override fun getPostprocessNormalizeOp(): TensorOperator = NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD)

  companion object {

    /** Float MobileNet requires additional normalization of the used input.  */
    private val IMAGE_MEAN = 127.5f

    private val IMAGE_STD = 127.5f

    /**
     * Float model does not need dequantization in the post-processing. Setting mean and std as 0.0f
     * and 1.0f, repectively, to bypass the normalization.
     */
    private val PROBABILITY_MEAN = 0.0f

    private val PROBABILITY_STD = 1.0f
  }
}

class ClassifierQuantizedMobileNet @Throws(IOException::class) constructor(assetManager: AssetManager)
  : ImageClassifier(assetManager)
{
  override fun getModelPath(): String = "mobilenet_v1_1.0_224_quant.tflite"
  override fun getLabelPath(): String = "labels.txt"
  override fun getPreprocessNormalizeOp(): TensorOperator =
    NormalizeOp(IMAGE_MEAN, IMAGE_STD)

  override fun getPostprocessNormalizeOp(): TensorOperator =
    NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD)

  companion object {
    /**
     * The quantized model does not require normalization, thus set mean as 0.0f, and std as 1.0f to
     * bypass the normalization.
     */
    private val IMAGE_MEAN = 0.0f

    private val IMAGE_STD = 1.0f

    /** Quantized MobileNet requires additional dequantization to the output probability.  */
    private val PROBABILITY_MEAN = 0.0f

    private val PROBABILITY_STD = 255.0f
  }
}
