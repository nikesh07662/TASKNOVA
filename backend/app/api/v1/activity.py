from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from typing import List
from app.core.database import get_db
from app.models.activity_log import ActivityLog
from app.models.user import User
from app.schemas.cloud import ActivityItemOut
from app.api.deps import get_current_user

router = APIRouter(prefix="/activity", tags=["Activity & Notifications"])

@router.get("/feed", response_model=List[ActivityItemOut])
async def get_activity_feed(user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    stmt = select(ActivityLog).where(ActivityLog.user_id == user.id).order_by(ActivityLog.timestamp.desc()).limit(30)
    res = await db.execute(stmt)
    activities = res.scalars().all()

    if not activities:
        # Seed initial activities
        demo_activities = [
            ActivityLog(
                user_id=user.id,
                title="Classroom Synchronized",
                description="Synchronized 3 active courses and 1 upcoming deadline from Google Classroom.",
                category="SYNC_OPERATION"
            ),
            ActivityLog(
                user_id=user.id,
                title="AI Document Prepared",
                description="Generated and verified solution for DBMS Practical Lab 4.",
                category="DOCUMENT_GENERATION"
            ),
            ActivityLog(
                user_id=user.id,
                title="Universal Task Memory Linked",
                description="Matched BCNF requirements with cloud document DBMS_Practical_Guide.pdf.",
                category="AI_ACTION"
            )
        ]
        db.add_all(demo_activities)
        await db.commit()
        for a in demo_activities:
            await db.refresh(a)
        activities = demo_activities

    return [
        ActivityItemOut(
            id=a.id,
            title=a.title,
            description=a.description,
            category=a.category,
            timestamp=a.timestamp.isoformat(),
            is_success=a.is_success,
            related_entity_id=a.related_entity_id
        )
        for a in activities
    ]
