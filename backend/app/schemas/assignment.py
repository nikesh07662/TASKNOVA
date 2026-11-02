from typing import Optional, List
from app.schemas.base import CamelModel

class AttachedFileSchema(CamelModel):
    id: str
    name: str
    download_url: str
    mime_type: str = "application/pdf"

class AssignmentOut(CamelModel):
    id: str
    title: str
    subject: str
    instructions: str
    deadline: str
    platform: str
    status: str
    source_platform_id: Optional[str] = None
    attached_files: List[AttachedFileSchema] = []
    matched_document_id: Optional[str] = None
    matched_document_name: Optional[str] = None
    is_duplicate: bool = False
    is_readiness_checked: bool = False
    missing_files_warning: Optional[str] = None

class AssignmentCreate(CamelModel):
    title: str
    subject: str
    instructions: str
    deadline: str
    platform: str = "MANUAL"
