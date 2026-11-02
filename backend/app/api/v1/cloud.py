import os
from fastapi import APIRouter, Depends, UploadFile, File, Form, HTTPException
from fastapi.responses import FileResponse
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from typing import List, Optional
from app.core.database import get_db
from app.models.cloud_file import CloudFile
from app.models.user import User
from app.schemas.cloud import CloudFileOut
from app.api.deps import get_current_user
from app.connectors.google_drive import GoogleDriveConnector
from app.connectors.microsoft_onedrive import MicrosoftOneDriveConnector

router = APIRouter(prefix="/cloud", tags=["Cloud Storage"])

@router.get("/files", response_model=List[CloudFileOut])
async def list_cloud_files(
    provider: Optional[str] = None,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    stmt = select(CloudFile).where(CloudFile.user_id == user.id)
    if provider:
        stmt = stmt.where(CloudFile.provider == provider.upper())
    stmt = stmt.order_by(CloudFile.created_at.desc())
    res = await db.execute(stmt)
    files = res.scalars().all()

    if not files:
        # Seed initial cloud documents
        demo_files = [
            CloudFile(
                user_id=user.id,
                name="DBMS_Practical_Guide.pdf",
                provider="GOOGLE_DRIVE",
                mime_type="application/pdf",
                size_bytes=248000,
                modified_time="2026-11-01T14:20:00Z",
                web_view_link="https://drive.google.com/file/d/demo_dbms/view",
                associated_subject="DBMS",
                is_generated_doc=True
            ),
            CloudFile(
                user_id=user.id,
                name="Algorithms_Dynamic_Programming.docx",
                provider="MICROSOFT_ONEDRIVE",
                mime_type="application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                size_bytes=184320,
                modified_time="2026-10-30T16:00:00Z",
                web_view_link="https://onedrive.live.com/view.aspx?id=demo_algo",
                associated_subject="Algorithms",
                is_generated_doc=False
            ),
            CloudFile(
                user_id=user.id,
                name="OS_Process_Scheduling_Notes.pdf",
                provider="GOOGLE_DRIVE",
                mime_type="application/pdf",
                size_bytes=412980,
                modified_time="2026-10-28T09:15:00Z",
                web_view_link="https://drive.google.com/file/d/demo_os/view",
                associated_subject="Operating Systems",
                is_generated_doc=False
            )
        ]
        db.add_all(demo_files)
        await db.commit()
        for f in demo_files:
            await db.refresh(f)
        files = demo_files

    return [CloudFileOut.model_validate(f) for f in files]

@router.post("/upload", response_model=CloudFileOut)
async def upload_file(
    provider: str = Form(...),
    file: UploadFile = File(...),
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    content = await file.read()
    prov_upper = provider.upper()

    if prov_upper == "GOOGLE_DRIVE":
        connector = GoogleDriveConnector(user.google_access_token)
        uploaded = await connector.upload_document(file.filename, content, file.content_type or "application/pdf")
    else:
        connector = MicrosoftOneDriveConnector(user.microsoft_access_token)
        uploaded = await connector.upload_document(file.filename, content)

    cloud_record = CloudFile(
        user_id=user.id,
        name=file.filename,
        provider=prov_upper,
        mime_type=file.content_type or "application/pdf",
        size_bytes=len(content),
        modified_time="2026-11-02T19:00:00Z",
        web_view_link=uploaded.get("webViewLink") or uploaded.get("webUrl")
    )
    db.add(cloud_record)
    await db.commit()
    await db.refresh(cloud_record)
    return CloudFileOut.model_validate(cloud_record)

@router.get("/docs/{filename}")
async def serve_pdf(filename: str):
    file_path = os.path.join(os.getcwd(), "generated_docs", filename)
    if os.path.exists(file_path):
        return FileResponse(file_path, media_type="application/pdf", filename=filename)
    raise HTTPException(status_code=404, detail="Document file not found.")
