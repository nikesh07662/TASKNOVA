package com.academic.workflow.core.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.academic.workflow.AcademicFlowApplication
import com.academic.workflow.R
import com.academic.workflow.core.network.NetworkResult
import com.academic.workflow.domain.model.AssignmentStatus
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.Instant

class DeadlineSyncWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = context.applicationContext as? AcademicFlowApplication ?: return Result.failure()
        val assignmentRepo = app.container.assignmentRepository

        return try {
            val syncResult = assignmentRepo.syncAssignments().first { it !is NetworkResult.Loading }
            if (syncResult is NetworkResult.Success) {
                checkDeadlinesAndNotify(syncResult.data)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun checkDeadlinesAndNotify(assignments: List<com.academic.workflow.domain.model.Assignment>) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val now = Instant.now()

        for (assignment in assignments) {
            if (assignment.status == AssignmentStatus.SUBMITTED) continue

            try {
                val deadlineInstant = Instant.parse(assignment.deadline)
                val hoursUntil = Duration.between(now, deadlineInstant).toHours()

                if (hoursUntil in 1..24) {
                    val notification = NotificationCompat.Builder(context, AcademicFlowApplication.CHANNEL_DEADLINES)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Urgent: ${assignment.subject} Due Soon")
                        .setContentText("\"${assignment.title}\" is due in $hoursUntil hours! Prepare document now.")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .build()

                    notificationManager.notify(assignment.id.hashCode(), notification)
                }
            } catch (_: Exception) {
                // Ignore parsing errors
            }
        }
    }
}
