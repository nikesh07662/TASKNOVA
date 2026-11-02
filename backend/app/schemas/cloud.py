from typing import Optional
from app.schemas.base import CamelModel

class CloudFileOut(CamelModel):
    id: str
    name: str
    provider: str
    mime_type: Optional[str] = "application/pdf"
    size_bytes: int = 0
    modified_time: Optional[str] = ""
    web_view_link: Optional[str] = None
    download_url: Optional[str] = None
    path: str = "/"
    is_generated_doc: bool = False
    associated_subject: Optional[str] = None

class ActivityItemOut(CamelModel):
    id: str
    title: str
    description: str
    category: str
    timestamp: str
    is_success: bool = True
    related_entity_id: Optional[str] = None
