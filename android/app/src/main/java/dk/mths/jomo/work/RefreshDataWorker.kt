package dk.mths.jomo.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class RefreshDataWorker(appContext: Context, params: WorkerParameters) :
    Worker(appContext, params) {

        override fun doWork(): Result{
            return Result.success()
        }
}