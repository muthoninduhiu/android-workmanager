/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.background

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.graphics.BlurMaskFilter.Blur
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.example.background.workers.BlurWorker
import com.example.background.workers.CleanUpWorker
import com.example.background.workers.SaveImageToFileWorker


class BlurViewModel(application: Application) : ViewModel() {

    internal var imageUri: Uri? = null
    internal var outputUri: Uri? = null
    private val workerManager = WorkManager.getInstance(application)
    internal val outputWorkInfo: LiveData<List<WorkInfo>>

    init{
        imageUri = getImageUri(application.applicationContext)
        outputWorkInfo = workerManager.getWorkInfosByTagLiveData(TAG_OUTPUT)
    }
    internal fun cancelWork() {
        workerManager.cancelUniqueWork(IMAGE_MANIPULATION_WORK_NAME)
    }
    private fun createInputDataForUri(): Data {
        val builder = Data.Builder()

        imageUri?.let {
            builder.putString(KEY_IMAGE_URI, imageUri.toString())
        }
        return builder.build()
    }
    /**
     * Create the WorkRequest to apply the blur and save the resulting image
     * @param blurLevel The amount to blur the image
     */

    internal fun applyBlur(blurLevel: Int) {
        /*var cleanRequest = workerManager
            .beginWith(OneTimeWorkRequest
            .from(CleanUpWorker::class.java))*/

        var cleanRequest = workerManager.beginUniqueWork(
            IMAGE_MANIPULATION_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequest.from(BlurWorker::class.java)
        )
        // Create charging constraint
        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .build()

        val saveRequest = OneTimeWorkRequestBuilder<SaveImageToFileWorker>()
            .setConstraints(constraints)
            .addTag(TAG_OUTPUT)
            .build()
        cleanRequest = cleanRequest.then(saveRequest)

       for(i in 0 until blurLevel){
           val blurBuilder = OneTimeWorkRequestBuilder<BlurWorker>()
           if (i == 0){
               blurBuilder.setInputData(createInputDataForUri())
           }
           cleanRequest = cleanRequest.then(blurBuilder.build())
       }
        cleanRequest.enqueue()
    }

    private fun uriOrNull(uriString: String?): Uri? {
        return if (!uriString.isNullOrEmpty()) {
            Uri.parse(uriString)
        } else {
            null
        }
    }

    private fun getImageUri(context: Context): Uri {
        val resources = context.resources

        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(R.drawable.doggo))
            .appendPath(resources.getResourceTypeName(R.drawable.doggo))
            .appendPath(resources.getResourceEntryName(R.drawable.doggo))
            .build()
    }

    internal fun setOutputUri(outputImageUri: String?) {
        outputUri = uriOrNull(outputImageUri)
    }

    class BlurViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(BlurViewModel::class.java)) {
                BlurViewModel(application) as T
            } else {
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
