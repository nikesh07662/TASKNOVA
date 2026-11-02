import pytest
from app.services.submission_engine import CommandAuthorizedSubmissionEngine

@pytest.mark.asyncio
async def test_submission_requires_explicit_student_command():
    engine = CommandAuthorizedSubmissionEngine()
    prepared = engine.prepare_submission(
        assignment_id="asgn_1",
        assignment_title="DBMS Normalization",
        subject="DBMS",
        destination_platform="GOOGLE_CLASSROOM",
        selected_document_id="doc_1",
        document_name="DBMS_Report.pdf",
        deadline="2026-11-10T23:59:00Z"
    )

    # Empty command MUST raise ValueError
    with pytest.raises(ValueError, match="Explicit student authorization command is mandatory"):
        await engine.execute_authorized_submission(
            submission_id=prepared.submission_id,
            idempotency_token=prepared.idempotency_token,
            student_command="",
            destination_platform=prepared.destination_platform,
            assignment_id=prepared.assignment_id,
            document_id=prepared.document_id
        )

@pytest.mark.asyncio
async def test_idempotency_duplicate_submission_blocked():
    engine = CommandAuthorizedSubmissionEngine()
    prepared = engine.prepare_submission(
        assignment_id="asgn_2",
        assignment_title="OS Memory Lab",
        subject="OS",
        destination_platform="MICROSOFT_TEAMS",
        selected_document_id="doc_2",
        document_name="OS_Report.pdf",
        deadline="2026-11-14T23:59:00Z"
    )

    # First authorized submission succeeds
    result = await engine.execute_authorized_submission(
        submission_id=prepared.submission_id,
        idempotency_token=prepared.idempotency_token,
        student_command="Submit OS Memory Lab to MS Teams",
        destination_platform=prepared.destination_platform,
        assignment_id=prepared.assignment_id,
        document_id=prepared.document_id
    )
    assert result.success is True
    assert result.receipt_id.startswith("MSTEAMS-REC-")

    # Second duplicate attempt with SAME idempotency token MUST be blocked
    with pytest.raises(ValueError, match="Duplicate submission attempt blocked"):
        await engine.execute_authorized_submission(
            submission_id=prepared.submission_id,
            idempotency_token=prepared.idempotency_token,
            student_command="Submit OS Memory Lab to MS Teams",
            destination_platform=prepared.destination_platform,
            assignment_id=prepared.assignment_id,
            document_id=prepared.document_id
        )
