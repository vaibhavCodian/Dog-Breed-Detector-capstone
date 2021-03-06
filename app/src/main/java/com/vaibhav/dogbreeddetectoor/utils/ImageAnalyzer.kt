package com.vaibhav.dogbreeddetectoor.utils

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import com.vaibhav.dogbreeddetectoor.model.BreedResult
import javax.inject.Inject

class ImageAnalyzer @Inject constructor():ImageAnalysis.Analyzer {

    private val _dogBreedResult = MutableLiveData<BreedResult>()
    val dogBreedResult:LiveData<BreedResult> = _dogBreedResult

    private val localModel = LocalModel.Builder()
        .setAssetFilePath("DogBreedModel.tflite")
        .build()

    private val customObjectDetectorOptions =
        CustomObjectDetectorOptions.Builder(localModel)
            .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
            .enableClassification()
            .setClassificationConfidenceThreshold(0.3f)
            .setMaxPerObjectLabelCount(1)
            .build()

    private val objectDetector = ObjectDetection.getClient(customObjectDetectorOptions)

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null){
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            objectDetector.process(image)
                .addOnFailureListener{
                    Log.d(TAG, it.printStackTrace().toString())
                }
                .addOnSuccessListener { listDetectedObjects ->
                    for (detectedObject in listDetectedObjects){
                        if (detectedObject.labels.isNotEmpty()) {
                            _dogBreedResult.value = BreedResult(
                                detectedObject.labels[0].text,
                                detectedObject.boundingBox)
                        }
                    }
                }.addOnCompleteListener{
                    imageProxy.close()
                }
        }
    }
}