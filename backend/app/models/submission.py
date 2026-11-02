from sqlalchemy import Column, String, Boolean, DateTime, ForeignKey, Text
from datetime import datetime, timezone
import uuid
from app.core.database import Base

class Submission(Base):
    __tablename__ = "submissions"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = Column(String, ForeignKey("users.id"), index=True, nullable=False)
    assignment_id = Column(String, ForeignKey("assignments.id"), nullable=False)
    document_id = Column(String, nullable=False)
    document_name = Column(String, nullable=False)
    destination_platform = Column(String, nullable=False)
    stage = Column(String, default="PREPARED_FOR_REVIEW")  # PREPARED_FOR_REVIEW, CONFIRMED_BY_USER, SUBMITTED_AND_VERIFIED, FAILED
    idempotency_token = Column(String, unique=True, index=True, nullable=False)
    student_command = Column(String, nullable=True)
    verification_receipt = Column(String, nullable=True)
    error_reason = Column(String, nullable=True)
    audit_trail_json = Column(Text, default="[]")
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))
    submitted_at = Column(DateTime, nullable=True)
