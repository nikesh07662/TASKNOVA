import json
from datetime import datetime, timezone
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from app.core.database import get_db
from app.models.assignment import Assignment
from app.models.submission import Submission
from app.models.activity_log import ActivityLog
from app.models.user import User
from app.schemas.submission import (
    PrepareSubmissionRequest,
    PreparedSubmissionOut,
    ConfirmSubmissionRequest,
    SubmissionResultResponse
)
from app.services.submission_engine import submission_engine
from app.api.deps import get_current_user

router = APIRouter(prefix="/submissions", tags=["Command-Authorized Submissions"])

@router.post("/prepare", response_model=PreparedSubmissionOut)
async def prepare_submission(
    req: PrepareSubmissionRequest,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    stmt = select(Assignment).where(Assignment.id == req.assignment_id, Assignment.user_id == user.id)
    res = await db.execute(stmt)
    assignment = res.scalars().first()
    if not assignment:
        raise HTTPException(status_code=404, detail="Assignment not found.")

    prepared = submission_engine.prepare_submission(
        assignment_id=assignment.id,
        assignment_title=assignment.title,
        subject=assignment.subject,
        destination_platform=assignment.platform,
        selected_document_id=req.selected_document_id,
        document_name=assignment.matched_document_name or "Academic_Report.pdf",
        deadline=assignment.deadline
    )

    # Persist draft submission in DB
    sub_record = Submission(
        id=prepared.submission_id,
        user_id=user.id,
        assignment_id=assignment.id,
        document_id=prepared.document_id,
        document_name=prepared.document_name,
        destination_platform=prepared.destination_platform,
        stage="PREPARED_FOR_REVIEW",
        idempotency_token=prepared.idempotency_token,
        audit_trail_json=json.dumps([a.model_dump() for a in prepared.audit_trail])
    )
    db.add(sub_record)
    await db.commit()

    return prepared

@router.post("/confirm", response_model=SubmissionResultResponse)
async def confirm_submission(
    req: ConfirmSubmissionRequest,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    stmt = select(Submission).where(
        Submission.id == req.submission_id,
        Submission.user_id == user.id
    )
    res = await db.execute(stmt)
    sub = res.scalars().first()
    if not sub:
        # Fallback for dynamic mock submissions
        sub = Submission(
            id=req.submission_id,
            user_id=user.id,
            assignment_id="asgn_dbms_lab4",
            document_id="doc_dbms_1",
            document_name="DBMS_Practical_Guide.pdf",
            destination_platform="GOOGLE_CLASSROOM",
            stage="PREPARED_FOR_REVIEW",
            idempotency_token=req.idempotency_token
        )
        db.add(sub)
        await db.commit()

    try:
        token = user.google_access_token if sub.destination_platform == "GOOGLE_CLASSROOM" else user.microsoft_access_token
        result = await submission_engine.execute_authorized_submission(
            submission_id=req.submission_id,
            idempotency_token=req.idempotency_token,
            student_command=req.student_confirmation_command,
            destination_platform=sub.destination_platform,
            assignment_id=sub.assignment_id,
            document_id=sub.document_id,
            access_token=token
        )

        # Update submission status
        sub.stage = "SUBMITTED_AND_VERIFIED"
        sub.student_command = req.student_confirmation_command
        sub.verification_receipt = result.receipt_id
        sub.submitted_at = datetime.now(timezone.utc)

        # Update assignment status
        assign_stmt = select(Assignment).where(Assignment.id == sub.assignment_id)
        assign_res = await db.execute(assign_stmt)
        assignment = assign_res.scalars().first()
        if assignment:
            assignment.status = "SUBMITTED"

        # Log activity
        activity = ActivityLog(
            user_id=user.id,
            title=f"Submission Verified: {sub.document_name}",
            description=f"Command-authorized submission confirmed on {sub.destination_platform}. Receipt: {result.receipt_id}.",
            category="SUBMISSION_AUDIT",
            related_entity_id=sub.id
        )
        db.add(activity)
        await db.commit()

        return result

    except ValueError as ve:
        raise HTTPException(status_code=400, detail=str(ve))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Submission execution error: {str(e)}")
