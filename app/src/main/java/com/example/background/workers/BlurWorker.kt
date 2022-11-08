package com.example.background.workers

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.decodeResource
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.background.KEY_IMAGE_URI
import com.example.background.R

class BlurWorker(
    context: Context,
    workerParam: WorkerParameters
): Worker(context, workerParam) {
    override fun doWork(): Result {
        val appContext  = applicationContext
        val resourceUri = inputData.getString(KEY_IMAGE_URI)
        makeStatusNotification("Blurring Image", appContext)

        return try{
           // val picture = decodeResource(appContext.resources,R.drawable.doggo)
            if(TextUtils.isEmpty(resourceUri)){
                Log.e(TAG,"Invalid URI")
                throw IllegalArgumentException("Invalid URI")
            }
            val resolver  = appContext.contentResolver

            val picture = BitmapFactory.decodeStream(
                resolver.openInputStream(Uri.parse(resourceUri))
            )

            val output = blurBitmap(picture,appContext)

            val outputURI = writeBitmapToFile(appContext,output)

            
            Result.success()
        }catch (throwable: Throwable){
            Log.e(TAG, "Error applying blur")
            Result.failure()
        }
    }
}