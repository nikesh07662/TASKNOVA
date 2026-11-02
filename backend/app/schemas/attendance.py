from typing import List
from app.schemas.base import CamelModel

class AttendanceRecordOut(CamelModel):
    subject_id: str
    subject_name: str
    total_lectures: int
    attended_lectures: int
    missed_lectures: int
    target_percentage: float = 75.0
    last_updated: str = ""

class AttendanceSummaryOut(CamelModel):
    overall_percentage: float
    subjects_shortage_count: int
    total_lectures_overall: int
    total_attended_overall: int
    records: List[AttendanceRecordOut] = []

class AttendanceUpdateRequest(CamelModel):
    subject_id: str
    attended_increment: int = 0
    missed_increment: int = 0
