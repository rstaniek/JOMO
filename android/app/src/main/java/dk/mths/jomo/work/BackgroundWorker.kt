package dk.mths.jomo.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import dk.mths.jomo.R
import dk.mths.jomo.utils.StatusNotificationHelper
import java.io.File


class BackgroundWorker(appContext: Context, params: WorkerParameters) :
    Worker(appContext, params) {

    // Notification Channel constants

    // Name of Notification Channel for verbose notifications of background work
    @JvmField val VERBOSE_NOTIFICATION_CHANNEL_NAME: CharSequence =
        "Verbose WorkManager Notifications"
    val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION =
        "Shows notifications whenever work starts"
    @JvmField val NOTIFICATION_TITLE: CharSequence = "Test WorkRequest Starting"
    val CHANNEL_ID = "VERBOSE_NOTIFICATION"
    val NOTIFICATION_ID = 1


        override fun doWork(): Result{
            val appContext = applicationContext
            val notificationHelper = StatusNotificationHelper()

            val testData = inputData.getString("Test")

            notificationHelper.makeStatusNotification(testData.toString(), appContext)

            return Result.success()
        }


}