from sqlalchemy import Column, String, Boolean, Integer, DateTime, ForeignKey
from datetime import datetime, timezone
import uuid
from app.core.database import Base

class CloudFile(Base):
    __tablename__ = "cloud_files"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = Column(String, ForeignKey("users.id"), index=True, nullable=False)
    name = Column(String, nullable=False)
    provider = Column(String, nullable=False)  # GOOGLE_DRIVE, MICROSOFT_ONEDRIVE, LOCAL_STORAGE
    mime_type = Column(String, default="application/pdf")
    size_bytes = Column(Integer, default=0)
    modified_time = Column(String, default="")
    web_view_link = Column(String, nullable=True)
    download_url = Column(String, nullable=True)
    path = Column(String, default="/")
    is_generated_doc = Column(Boolean, default=False)
    associated_subject = Column(String, nullable=True)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))
