package dk.mths.jomo.work

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File

private const val TAG = "CleaupWorker"
class CleanUpWorker(context: Context, params: WorkerParameters) : Worker(context, params){

    override fun doWork(): Result {

        return try {
            val outputDirectory = File(applicationContext.filesDir, "Jomo_data")
            if (outputDirectory.exists()) {
                val entries = outputDirectory.listFiles()
                if (entries != null) {
                    for (entry in entries) {
                        val name = entry.name
                        if (name.isNotEmpty() && name.endsWith(".png")) {
                            val deleted = entry.delete()
                            Log.i(TAG, "Deleted $name - $deleted")
                        }
                    }
                }
            }
            Result.success()
        } catch (exception: Exception) {
            exception.printStackTrace()
            Result.failure()
        }
    }
}