from sqlalchemy import Column, String, DateTime, ForeignKey, Text
from datetime import datetime, timezone
import uuid
from app.core.database import Base

class TaskMemoryItem(Base):
    __tablename__ = "task_memory_items"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = Column(String, ForeignKey("users.id"), index=True, nullable=False)
    title = Column(String, nullable=False)
    summary = Column(Text, nullable=False)
    content = Column(Text, nullable=False)
    category = Column(String, nullable=False)  # ASSIGNMENT, DOCUMENT, CONVERSATION, DEADLINE, SUBMISSION
    related_entity_id = Column(String, nullable=True)
    metadata_json = Column(Text, default="{}")
    embedding_json = Column(Text, default="[]")  # Serialized vector representation compatible with pgvector
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))
