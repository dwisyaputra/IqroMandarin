package com.example.iqromandarin

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker for daily review notifications.
 * Scheduled to run at ~8AM daily.
 */
class ReviewNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        sendReviewNotification()
        return Result.success()
    }

    private fun sendReviewNotification() {
        val notificationHelper = NotificationHelper(applicationContext)
        notificationHelper.sendDailyReviewNotification()
    }

    companion object {
        private const val WORK_NAME = "daily_review_notification"

        fun scheduleDaily(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val dailyWork = PeriodicWorkRequestBuilder<ReviewNotificationWorker>(
                24, TimeUnit.HOURS,
                15, TimeUnit.MINUTES // flex period
            )
                .setConstraints(constraints)
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                dailyWork
            )
        }

        private fun calculateInitialDelay(): Long {
            val cal = java.util.Calendar.getInstance()
            val now = cal.timeInMillis
            cal.set(java.util.Calendar.HOUR_OF_DAY, 8) // 8 AM
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            if (cal.timeInMillis <= now) {
                cal.add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
            return cal.timeInMillis - now
        }
    }
}

/**
 * Simple notification helper
 */
class NotificationHelper(private val context: Context) {

    fun sendDailyReviewNotification() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "review_channel",
                "Review Harian",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifikasi review mandarin harian"
            }
            val manager = context.getSystemService(android.app.NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val intent = android.content.Intent(context, MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context, 0, intent,
            android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = androidx.core.app.NotificationCompat.Builder(context, "review_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("📚 Waktu Belajar Mandarin!")
            .setContentText("Anda punya item untuk di-review hari ini. 加油!")
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = androidx.core.app.NotificationManagerCompat.from(context)
        if (androidx.core.app.ActivityCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(1001, notification)
        }
    }
}
