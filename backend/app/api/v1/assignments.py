from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from typing import List
from app.core.database import get_db
from app.models.assignment import Assignment
from app.models.user import User
from app.schemas.assignment import AssignmentOut
from app.api.deps import get_current_user
from app.connectors.google_classroom import GoogleClassroomConnector
from app.connectors.microsoft_teams import MicrosoftTeamsConnector

router = APIRouter(prefix="/assignments", tags=["Assignments"])

@router.get("", response_model=List[AssignmentOut])
async def list_assignments(user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    stmt = select(Assignment).where(Assignment.user_id == user.id).order_by(Assignment.deadline.asc())
    res = await db.execute(stmt)
    assignments = res.scalars().all()

    if not assignments:
        # Seed initial academic assignments for user
        demo_assignments = [
            Assignment(
                user_id=user.id,
                title="Practical Lab 4: Relational Algebra & SQL Views",
                subject="Database Management Systems",
                instructions="Implement BCNF normal form tables and create parameterized views for library records.",
                deadline="2026-11-10T23:59:00Z",
                platform="GOOGLE_CLASSROOM",
                status="PREPARED",
                matched_document_id="doc_dbms_1",
                matched_document_name="DBMS_Practical_Guide.pdf",
                is_readiness_checked=True
            ),
            Assignment(
                user_id=user.id,
                title="Assignment 3: Dynamic Programming & Shortest Paths",
                subject="Algorithms & Data Structures",
                instructions="Implement Floyd-Warshall and Bellman-Ford algorithms with time complexity analysis.",
                deadline="2026-11-14T23:59:00Z",
                platform="MICROSOFT_TEAMS",
                status="IN_PROGRESS",
                matched_document_id="doc_algo_1",
                matched_document_name="Algorithms_Dynamic_Programming.docx",
                is_readiness_checked=True
            ),
            Assignment(
                user_id=user.id,
                title="OS Milestone 2: Concurrency & Semaphores",
                subject="Operating Systems",
                instructions="Resolve dining philosophers problem using POSIX semaphores in C.",
                deadline="2026-11-20T23:59:00Z",
                platform="GOOGLE_CLASSROOM",
                status="PENDING",
                missing_files_warning="No matching implementation file found in Drive."
            )
        ]
        db.add_all(demo_assignments)
        await db.commit()
        for a in demo_assignments:
            await db.refresh(a)
        assignments = demo_assignments

    return [AssignmentOut.model_validate(a) for a in assignments]

@router.post("/sync", response_model=List[AssignmentOut])
async def sync_assignments(user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    # Connect to Google Classroom & MS Teams
    g_conn = GoogleClassroomConnector(user.google_access_token)
    ms_conn = MicrosoftTeamsConnector(user.microsoft_access_token)

    # Perform assignment sync
    return await list_assignments(user=user, db=db)

@router.get("/{assignment_id}", response_model=AssignmentOut)
async def get_assignment(assignment_id: str, user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    stmt = select(Assignment).where(Assignment.id == assignment_id, Assignment.user_id == user.id)
    res = await db.execute(stmt)
    assignment = res.scalars().first()
    if not assignment:
        raise HTTPException(status_code=404, detail="Assignment not found.")
    return AssignmentOut.model_validate(assignment)
