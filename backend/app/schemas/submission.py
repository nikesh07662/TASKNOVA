from typing import Optional, List
from app.schemas.base import CamelModel

class SubmissionAuditLogSchema(CamelModel):
    timestamp: str
    actor: str
    action: str
    details: str

class PrepareSubmissionRequest(CamelModel):
    assignment_id: str
    selected_document_id: str

class PreparedSubmissionOut(CamelModel):
    submission_id: str
    assignment_id: str
    assignment_title: str
    subject: str
    destination_platform: str
    document_id: str
    document_name: str
    document_size_formatted: str
    document_preview_snippet: str
    deadline: str
    stage: str
    idempotency_token: str
    audit_trail: List[SubmissionAuditLogSchema] = []

class ConfirmSubmissionRequest(CamelModel):
    submission_id: str
    idempotency_token: str
    student_confirmation_command: str

class SubmissionResultResponse(CamelModel):
    submission_id: str
    success: bool
    receipt_id: str
    verification_timestamp: str
    message: str
