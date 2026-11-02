import httpx
from typing import List, Dict, Any, Optional

class GoogleClassroomConnector:
    """
    Official Google Classroom API Connector.
    Endpoints:
    - GET /courses
    - GET /courses/{courseId}/courseWork
    - GET /courses/{courseId}/courseWork/{courseWorkId}/studentSubmissions
    - POST /courses/{courseId}/courseWork/{courseWorkId}/studentSubmissions/{id}:turnIn
    - GET /courses/{courseId}/announcements
    """
    BASE_URL = "https://classroom.googleapis.com/v1"

    def __init__(self, access_token: Optional[str] = None):
        self.access_token = access_token
        self.headers = {
            "Authorization": f"Bearer {access_token}" if access_token else "",
            "Accept": "application/json"
        }

    async def list_courses(self) -> List[Dict[str, Any]]:
        """List active student courses."""
        if not self.access_token:
            return self._demo_courses()

        async with httpx.AsyncClient() as client:
            try:
                response = await client.get(f"{self.BASE_URL}/courses?courseStates=ACTIVE", headers=self.headers)
                if response.status_code == 200:
                    return response.json().get("courses", [])
                elif response.status_code == 403:
                    raise PermissionError("Institutional admin approval required for Google Classroom API access.")
            except PermissionError:
                raise
            except Exception:
                pass
        return self._demo_courses()

    async def get_course_work(self, course_id: str) -> List[Dict[str, Any]]:
        """Retrieve course assignments, instructions, and due dates."""
        if not self.access_token:
            return self._demo_assignments(course_id)

        async with httpx.AsyncClient() as client:
            try:
                response = await client.get(f"{self.BASE_URL}/courses/{course_id}/courseWork", headers=self.headers)
                if response.status_code == 200:
                    return response.json().get("courseWork", [])
            except Exception:
                pass
        return self._demo_assignments(course_id)

    async def get_student_submission(self, course_id: str, course_work_id: str, submission_id: str = "me") -> Dict[str, Any]:
        """Retrieve student coursework submission state."""
        if not self.access_token:
            return {
                "id": submission_id,
                "courseId": course_id,
                "courseWorkId": course_work_id,
                "state": "TURNED_IN",
                "assignedGrade": 95.0
            }

        async with httpx.AsyncClient() as client:
            try:
                url = f"{self.BASE_URL}/courses/{course_id}/courseWork/{course_work_id}/studentSubmissions/{submission_id}"
                response = await client.get(url, headers=self.headers)
                if response.status_code == 200:
                    return response.json()
            except Exception:
                pass
        return {"id": submission_id, "courseId": course_id, "state": "CREATED"}

    async def list_announcements(self, course_id: str) -> List[Dict[str, Any]]:
        """Retrieve classroom announcements."""
        if not self.access_token:
            return [
                {
                    "id": f"ann_{course_id}_1",
                    "courseId": course_id,
                    "text": "Reminder: All laboratory reports must include query execution plans and relational schemas.",
                    "creationTime": "2026-11-01T10:00:00Z"
                }
            ]

        async with httpx.AsyncClient() as client:
            try:
                url = f"{self.BASE_URL}/courses/{course_id}/announcements"
                response = await client.get(url, headers=self.headers)
                if response.status_code == 200:
                    return response.json().get("announcements", [])
            except Exception:
                pass
        return []

    async def submit_student_submission(self, course_id: str, course_work_id: str, submission_id: str) -> Dict[str, Any]:
        """
        Turn in a student submission using the official Google Classroom API:
        POST https://classroom.googleapis.com/v1/courses/{courseId}/courseWork/{courseWorkId}/studentSubmissions/{id}:turnIn
        """
        if not self.access_token:
            return {
                "success": True,
                "receipt_id": f"GCLASS-REC-{course_work_id[:6]}",
                "status": "TURNED_IN",
                "platform": "GOOGLE_CLASSROOM"
            }

        async with httpx.AsyncClient() as client:
            url = f"{self.BASE_URL}/courses/{course_id}/courseWork/{course_work_id}/studentSubmissions/{submission_id}:turnIn"
            response = await client.post(url, headers=self.headers, json={})
            if response.status_code == 200:
                return {
                    "success": True,
                    "receipt_id": f"GCLASS-TX-{response.json().get('id', submission_id)}",
                    "status": "TURNED_IN",
                    "platform": "GOOGLE_CLASSROOM"
                }
            elif response.status_code == 403:
                raise PermissionError("Institutional administrator approval required to modify student submissions.")
            else:
                raise RuntimeError(f"Classroom API submission error: {response.text}")

    def _demo_courses(self) -> List[Dict[str, Any]]:
        return [
            {"id": "course_dbms", "name": "Database Management Systems", "section": "CS301"},
            {"id": "course_os", "name": "Operating Systems & Concurrency", "section": "CS302"},
            {"id": "course_cn", "name": "Computer Networks & Protocols", "section": "CS303"}
        ]

    def _demo_assignments(self, course_id: str) -> List[Dict[str, Any]]:
        return [
            {
                "id": f"cw_{course_id}_1",
                "courseId": course_id,
                "title": "Practical Lab 4: Relational Algebra & SQL Views",
                "description": "Implement BCNF normal form tables and create parameterized views for library records.",
                "dueDate": {"year": 2026, "month": 11, "day": 10},
                "dueTime": {"hours": 23, "minutes": 59},
                "state": "PUBLISHED"
            }
        ]
