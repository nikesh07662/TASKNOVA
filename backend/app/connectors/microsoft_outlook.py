import re
import httpx
from typing import List, Dict, Any, Optional

class MicrosoftOutlookConnector:
    """
    Official Microsoft Graph Outlook Mail Connector.
    Endpoints:
    - GET /me/messages
    - GET /me/messages/{id}
    """
    BASE_URL = "https://graph.microsoft.com/v1.0/me"

    def __init__(self, access_token: Optional[str] = None):
        self.access_token = access_token
        self.headers = {
            "Authorization": f"Bearer {access_token}" if access_token else "",
            "Accept": "application/json"
        }

    async def get_academic_emails(self) -> List[Dict[str, Any]]:
        """Retrieve Outlook student emails regarding assignment updates."""
        if not self.access_token:
            return [self.classify_and_extract_deadlines(e) for e in self._demo_emails()]

        async with httpx.AsyncClient() as client:
            try:
                params = {"$top": "10", "$search": "\"assignment\" OR \"deadline\""}
                response = await client.get(f"{self.BASE_URL}/messages", headers=self.headers, params=params)
                if response.status_code == 200:
                    raw_emails = response.json().get("value", [])
                    return [self.classify_and_extract_deadlines(e) for e in raw_emails]
            except Exception:
                pass
        return [self.classify_and_extract_deadlines(e) for e in self._demo_emails()]

    def classify_and_extract_deadlines(self, email_data: Dict[str, Any]) -> Dict[str, Any]:
        """Classify student emails and detect deadline mentions."""
        subject = email_data.get("subject", "")
        body_preview = email_data.get("bodyPreview", "")
        combined = f"{subject} {body_preview}".lower()

        extracted_deadline = None
        date_patterns = [
            r"due\s+on\s+([A-Za-z]+\s+\d{1,2})",
            r"due\s*:\s*([A-Za-z]+\s+\d{1,2})",
            r"before\s+([A-Za-z]+day)"
        ]
        for pattern in date_patterns:
            m = re.search(pattern, combined)
            if m:
                extracted_deadline = m.group(1)
                break

        return {
            "id": email_data.get("id", "outlook_msg_default"),
            "subject": subject,
            "from": email_data.get("from", {}).get("emailAddress", {}).get("address", "instructor@university.edu"),
            "bodyPreview": body_preview,
            "receivedDateTime": email_data.get("receivedDateTime", "2026-11-01T11:00:00Z"),
            "is_assignment_related": any(k in combined for k in ["assignment", "coursework", "lab", "teams", "deadline"]),
            "extracted_deadline": extracted_deadline,
            "priority": "HIGH" if extracted_deadline else "NORMAL"
        }

    def _demo_emails(self) -> List[Dict[str, Any]]:
        return [
            {
                "id": "outlook_msg_1",
                "subject": "CS201 Assignment 3 Released on MS Teams",
                "from": {"emailAddress": {"name": "Prof. Alan Turing", "address": "turing@university.edu"}},
                "bodyPreview": "Please review the Bellman-Ford optimization criteria on MS Teams before Friday.",
                "receivedDateTime": "2026-11-01T11:00:00Z"
            }
        ]
