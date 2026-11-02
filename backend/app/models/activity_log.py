from sqlalchemy import Column, String, Boolean, DateTime, ForeignKey, Text
from datetime import datetime, timezone
import uuid
from app.core.database import Base

class ActivityLog(Base):
    __tablename__ = "activity_logs"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = Column(String, ForeignKey("users.id"), index=True, nullable=False)
    title = Column(String, nullable=False)
    description = Column(Text, nullable=False)
    category = Column(String, nullable=False)  # AI_ACTION, DOCUMENT_GENERATION, SUBMISSION_AUDIT, SYNC_OPERATION, ATTENDANCE_UPDATE, NOTIFICATION
    is_success = Column(Boolean, default=True)
    related_entity_id = Column(String, nullable=True)
    timestamp = Column(DateTime, default=lambda: datetime.now(timezone.utc))
