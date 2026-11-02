import json
from datetime import datetime, timezone
from fastapi import APIRouter, Depends, UploadFile, File, Form, HTTPException
from typing import Dict, Any
from app.models.user import User
from app.schemas.ai import ChatApiRequest, ChatMessageOut, DocumentGenerateRequest, DocumentGenerateResponse
from app.services.ai_agent import ai_agent
from app.services.document_generator import document_generator
from app.services.task_memory_service import task_memory_service
from app.api.deps import get_current_user

router = APIRouter(prefix="/ai", tags=["AI Assistant"])

@router.post("/chat", response_model=ChatMessageOut)
async def chat(req: ChatApiRequest, user: User = Depends(get_current_user)):
    content, tool_executions, metadata = await ai_agent.execute_agent_loop(
        prompt=req.prompt,
        model=req.model,
        user_id=user.id,
        context_files=req.attached_file_ids
    )

    # Persist into Universal Task Memory
    task_memory_service.store_memory(
        user_id=user.id,
        title=f"Chat: {req.prompt[:30]}",
        content=content,
        category="CONVERSATION",
        metadata={"model": req.model}
    )

    return ChatMessageOut(
        id=f"msg_{datetime.now(timezone.utc).timestamp()}",
        role="ASSISTANT",
        content=content,
        timestamp=datetime.now(timezone.utc).isoformat(),
        model_used=req.model,
        tool_executions=tool_executions,
        suggested_action=metadata.get("suggested_action"),
        related_assignment_id=metadata.get("assignment_id")
    )

@router.post("/generate-document", response_model=DocumentGenerateResponse)
async def generate_document(req: DocumentGenerateRequest, user: User = Depends(get_current_user)):
    result = await document_generator.generate_document(
        title=req.title,
        subject=req.subject,
        document_type=req.document_type,
        instructions=req.instructions
    )

    task_memory_service.store_memory(
        user_id=user.id,
        title=req.title,
        content=result["markdown_content"][:400],
        category="DOCUMENT",
        related_entity_id=result["document_id"],
        metadata={"subject": req.subject}
    )

    return DocumentGenerateResponse(
        document_id=result["document_id"],
        title=result["title"],
        markdown_content=result["markdown_content"],
        pdf_download_url=result["pdf_download_url"],
        drive_uploaded=result["drive_uploaded"],
        onedrive_uploaded=result["onedrive_uploaded"]
    )

@router.post("/import-export")
async def import_export(
    provider: str = Form(...),
    file: UploadFile = File(...),
    user: User = Depends(get_current_user)
):
    try:
        content_bytes = await file.read()
        raw_json = json.loads(content_bytes.decode("utf-8"))
        count = task_memory_service.import_chat_export(user.id, provider, raw_json)
        return {"importedConversations": str(count), "provider": provider, "status": "SUCCESS"}
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Failed to parse conversation export: {str(e)}")
