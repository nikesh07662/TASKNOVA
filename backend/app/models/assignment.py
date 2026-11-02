from sqlalchemy import Column, String, Boolean, DateTime, ForeignKey, Text
from datetime import datetime, timezone
import uuid
from app.core.database import Base

class Assignment(Base):
    __tablename__ = "assignments"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = Column(String, ForeignKey("users.id"), index=True, nullable=False)
    title = Column(String, nullable=False)
    subject = Column(String, nullable=False)
    instructions = Column(Text, default="")
    deadline = Column(String, nullable=False)  # ISO-8601
    platform = Column(String, nullable=False)  # GOOGLE_CLASSROOM, MICROSOFT_TEAMS, MANUAL
    status = Column(String, default="PENDING")  # PENDING, IN_PROGRESS, PREPARED, SUBMITTED, OVERDUE
    source_platform_id = Column(String, nullable=True)
    matched_document_id = Column(String, nullable=True)
    matched_document_name = Column(String, nullable=True)
    is_duplicate = Column(Boolean, default=False)
    is_readiness_checked = Column(Boolean, default=False)
    missing_files_warning = Column(String, nullable=True)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))
