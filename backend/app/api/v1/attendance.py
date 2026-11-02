from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from app.core.database import get_db
from app.models.attendance import AttendanceCourse
from app.models.user import User
from app.schemas.attendance import AttendanceSummaryOut, AttendanceRecordOut, AttendanceUpdateRequest
from app.api.deps import get_current_user

router = APIRouter(prefix="/attendance", tags=["Attendance Management"])

@router.get("", response_model=AttendanceSummaryOut)
async def get_attendance(user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    stmt = select(AttendanceCourse).where(AttendanceCourse.user_id == user.id)
    res = await db.execute(stmt)
    courses = res.scalars().all()

    if not courses:
        # Seed initial realistic course attendance
        demo_courses = [
            AttendanceCourse(
                user_id=user.id,
                subject_id="cs301",
                subject_name="Database Management Systems",
                total_lectures=40,
                attended_lectures=32,
                missed_lectures=8,
                target_percentage=user.attendance_target
            ),
            AttendanceCourse(
                user_id=user.id,
                subject_id="cs302",
                subject_name="Operating Systems",
                total_lectures=30,
                attended_lectures=21,
                missed_lectures=9,
                target_percentage=user.attendance_target
            ),
            AttendanceCourse(
                user_id=user.id,
                subject_id="cs303",
                subject_name="Computer Networks",
                total_lectures=36,
                attended_lectures=34,
                missed_lectures=2,
                target_percentage=user.attendance_target
            )
        ]
        db.add_all(demo_courses)
        await db.commit()
        for c in demo_courses:
            await db.refresh(c)
        courses = demo_courses

    total_lec = sum(c.total_lectures for c in courses)
    total_att = sum(c.attended_lectures for c in courses)
    overall_pct = (total_att / total_lec * 100.0) if total_lec > 0 else 100.0

    shortage_count = sum(
        1 for c in courses
        if (c.attended_lectures / c.total_lectures * 100.0 if c.total_lectures > 0 else 100.0) < c.target_percentage
    )

    records = [
        AttendanceRecordOut(
            subject_id=c.subject_id,
            subject_name=c.subject_name,
            total_lectures=c.total_lectures,
            attended_lectures=c.attended_lectures,
            missed_lectures=c.missed_lectures,
            target_percentage=c.target_percentage,
            last_updated=c.last_updated.isoformat()
        )
        for c in courses
    ]

    return AttendanceSummaryOut(
        overall_percentage=round(overall_pct, 1),
        subjects_shortage_count=shortage_count,
        total_lectures_overall=total_lec,
        total_attended_overall=total_att,
        records=records
    )

@router.post("/update", response_model=AttendanceRecordOut)
async def update_attendance(
    req: AttendanceUpdateRequest,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    stmt = select(AttendanceCourse).where(
        AttendanceCourse.subject_id == req.subject_id,
        AttendanceCourse.user_id == user.id
    )
    res = await db.execute(stmt)
    course = res.scalars().first()
    if course:
        course.attended_lectures += req.attended_increment
        course.missed_lectures += req.missed_increment
        course.total_lectures += (req.attended_increment + req.missed_increment)
        await db.commit()
        await db.refresh(course)
        return AttendanceRecordOut(
            subject_id=course.subject_id,
            subject_name=course.subject_name,
            total_lectures=course.total_lectures,
            attended_lectures=course.attended_lectures,
            missed_lectures=course.missed_lectures,
            target_percentage=course.target_percentage,
            last_updated=course.last_updated.isoformat()
        )
    return (await get_attendance(user, db)).records[0]
