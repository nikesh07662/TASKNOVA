from sqlalchemy import Column, String, Integer, Float, DateTime, ForeignKey
from datetime import datetime, timezone
import uuid
from app.core.database import Base

class AttendanceCourse(Base):
    __tablename__ = "attendance_courses"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = Column(String, ForeignKey("users.id"), index=True, nullable=False)
    subject_id = Column(String, nullable=False)
    subject_name = Column(String, nullable=False)
    total_lectures = Column(Integer, default=0)
    attended_lectures = Column(Integer, default=0)
    missed_lectures = Column(Integer, default=0)
    target_percentage = Column(Float, default=75.0)
    last_updated = Column(DateTime, default=lambda: datetime.now(timezone.utc))
