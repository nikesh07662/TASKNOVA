from typing import Optional, List
from app.schemas.base import CamelModel

class ToolExecutionInfoSchema(CamelModel):
    tool_name: str
    arguments_json: str
    result_summary: str
    is_executing: bool = False

class ChatMessageOut(CamelModel):
    id: str
    role: str
    content: str
    timestamp: str
    model_used: str = "gpt-4o"
    tool_executions: List[ToolExecutionInfoSchema] = []
    suggested_action: Optional[str] = None
    related_assignment_id: Optional[str] = None

class ChatApiRequest(CamelModel):
    prompt: str
    model: str = "gpt-4o"
    conversation_id: Optional[str] = None
    attached_file_ids: List[str] = []

class DocumentGenerateRequest(CamelModel):
    assignment_id: Optional[str] = None
    title: str
    subject: str
    document_type: str = "ASSIGNMENT"
    instructions: str
    reference_file_ids: List[str] = []

class DocumentGenerateResponse(CamelModel):
    document_id: str
    title: str
    markdown_content: str
    pdf_download_url: Optional[str] = None
    drive_uploaded: bool = False
    onedrive_uploaded: bool = False
