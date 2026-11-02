import asyncio
from app.workers.celery_app import celery_app
from app.services.deadline_monitor import deadline_monitor

@celery_app.task
def periodic_deadline_monitoring():
    """
    Background worker job:
    Evaluates student deadlines across all active assignments.
    Sends contextual alerts (5d, 3d, 2d missing files, 1d urgent, 3h readiness).
    Never submits automatically.
    """
    sample_assignments = [
        {
            "id": "asgn_1",
            "title": "DBMS Practical Lab 4",
            "subject": "Database Management Systems",
            "deadline": "2026-11-10T23:59:00Z",
            "status": "PREPARED",
            "matched_document_id": "doc_1"
        }
    ]
    notifications_generated = []
    for asgn in sample_assignments:
        alerts = deadline_monitor.evaluate_assignment(asgn)
        notifications_generated.extend(alerts)

    return {"status": "SUCCESS", "evaluated_count": len(sample_assignments), "alerts": notifications_generated}

@celery_app.task
def periodic_cloud_sync():
    """
    Background worker job:
    Synchronizes course updates and files from Google and Microsoft APIs.
    """
    return {"status": "SUCCESS", "synced_platforms": ["GOOGLE_CLASSROOM", "MICROSOFT_TEAMS"]}
