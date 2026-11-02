from celery import Celery
from app.core.config import settings

celery_app = Celery(
    "academic_workflow_worker",
    broker=settings.CELERY_BROKER_URL,
    backend=settings.CELERY_RESULT_BACKEND
)

celery_app.conf.update(
    task_serializer="json",
    accept_content=["json"],
    result_serializer="json",
    timezone="UTC",
    enable_utc=True,
    beat_schedule={
        "evaluate-deadlines-every-15-mins": {
            "task": "app.workers.tasks.periodic_deadline_monitoring",
            "schedule": 900.0,
        },
        "sync-academic-cloud-hourly": {
            "task": "app.workers.tasks.periodic_cloud_sync",
            "schedule": 3600.0,
        }
    }
)
