from abc import ABC, abstractmethod
from typing import List, Dict, Any, Optional

class CloudConnector(ABC):
    @abstractmethod
    async def get_assignments(self) -> List[Dict[str, Any]]:
        """Retrieve course assignments with instructions and deadlines."""
        pass

    @abstractmethod
    async def search_files(self, query: str) -> List[Dict[str, Any]]:
        """Search student cloud storage for relevant academic reference documents."""
        pass

    @abstractmethod
    async def upload_file(self, file_name: str, content_bytes: bytes, mime_type: str, folder_id: Optional[str] = None) -> Dict[str, Any]:
        """Upload academic document to authorized storage folder."""
        pass

    @abstractmethod
    async def get_calendar_events(self) -> List[Dict[str, Any]]:
        """Retrieve scheduled lectures and deadline calendar events."""
        pass

    @abstractmethod
    async def get_academic_emails(self, query: str = "assignment OR deadline OR exam") -> List[Dict[str, Any]]:
        """Retrieve academic notification messages."""
        pass

    @abstractmethod
    async def submit_assignment(self, course_id: str, assignment_id: str, attachment_file_ids: List[str]) -> Dict[str, Any]:
        """Execute submission on destination platform. Requires verified student authorization."""
        pass
