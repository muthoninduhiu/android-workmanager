package com.example.background.workers

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.decodeResource
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.background.R

class BlurWorker(
    context: Context,
    workerParam: WorkerParameters
): Worker(context, workerParam) {
    override fun doWork(): Result {
        val appContext  = applicationContext
        makeStatusNotification("Blurring Image", appContext)

        return try{
            val picture = decodeResource(appContext.resources,R.drawable.doggo)
            val output = blurBitmap(picture,appContext)
            val outputURI = writeBitmapToFile(appContext,output)
            makeStatusNotification("Output is: $outputURI", appContext)
            
            Result.success()
        }catch (throwable: Throwable){
            Log.e(TAG, "Error applying blur")
            Result.failure()
        }
    }
}