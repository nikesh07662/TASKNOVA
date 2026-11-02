from sqlalchemy import Column, String, Boolean, Float, DateTime
from datetime import datetime, timezone
import uuid
from app.core.database import Base

class User(Base):
    __tablename__ = "users"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    email = Column(String, unique=True, index=True, nullable=False)
    full_name = Column(String, nullable=False)
    hashed_password = Column(String, nullable=False)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))

    # OAuth tokens (encrypted in production)
    google_access_token = Column(String, nullable=True)
    google_refresh_token = Column(String, nullable=True)
    is_google_connected = Column(Boolean, default=False)

    microsoft_access_token = Column(String, nullable=True)
    microsoft_refresh_token = Column(String, nullable=True)
    is_microsoft_connected = Column(Boolean, default=False)

    preferred_ai_model = Column(String, default="gpt-4o")
    attendance_target = Column(Float, default=75.0)
