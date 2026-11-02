package com.academic.workflow

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.academic.workflow.core.di.AppContainer
import com.academic.workflow.core.worker.DeadlineSyncWorker
import java.util.concurrent.TimeUnit

class AcademicFlowApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        createNotificationChannels()
        scheduleBackgroundSync()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val deadlineChannel = NotificationChannel(
                CHANNEL_DEADLINES,
                "Academic Deadlines & Submissions",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent assignment deadlines, reminders, and submission confirmations"
                enableVibration(true)
            }

            val attendanceChannel = NotificationChannel(
                CHANNEL_ATTENDANCE,
                "Attendance Warnings",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Attendance shortage alerts and target milestones"
            }

            val aiChannel = NotificationChannel(
                CHANNEL_AI,
                "AI Assistant & Document Operations",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications when documents or AI tasks finish"
            }

            notificationManager.createNotificationChannels(
                listOf(deadlineChannel, attendanceChannel, aiChannel)
            )
        }
    }

    private fun scheduleBackgroundSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncWorkRequest = PeriodicWorkRequestBuilder<DeadlineSyncWorker>(
            15, TimeUnit.MINUTES
        ).setConstraints(constraints).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "AcademicDeadlinePeriodicSync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )
    }

    companion object {
        const val CHANNEL_DEADLINES = "academic_deadlines"
        const val CHANNEL_ATTENDANCE = "academic_attendance"
        const val CHANNEL_AI = "academic_ai_tasks"
    }
}
