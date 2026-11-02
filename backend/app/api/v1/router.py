from fastapi import APIRouter
from app.api.v1 import auth, assignments, ai, submissions, cloud, attendance, activity, integrations

api_router = APIRouter()
api_router.include_router(auth.router)
api_router.include_router(assignments.router)
api_router.include_router(ai.router)
api_router.include_router(submissions.router)
api_router.include_router(cloud.router)
api_router.include_router(attendance.router)
api_router.include_router(activity.router)
api_router.include_router(integrations.router)
