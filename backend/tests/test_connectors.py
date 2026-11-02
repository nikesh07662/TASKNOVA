import pytest
from app.connectors.google_classroom import GoogleClassroomConnector
from app.connectors.microsoft_teams import MicrosoftTeamsConnector
from app.connectors.google_drive import GoogleDriveConnector
from app.connectors.microsoft_onedrive import MicrosoftOneDriveConnector
from app.connectors.google_calendar import GoogleCalendarConnector
from app.connectors.google_gmail import GmailConnector
from app.connectors.microsoft_calendar import MicrosoftCalendarConnector
from app.connectors.microsoft_outlook import MicrosoftOutlookConnector

@pytest.mark.asyncio
async def test_google_classroom_connector():
    connector = GoogleClassroomConnector(access_token=None)
    courses = await connector.list_courses()
    assert len(courses) >= 1
    assert any("Database" in c["name"] for c in courses)

    asgns = await connector.get_course_work("course_dbms")
    assert len(asgns) >= 1

    submission = await connector.get_student_submission("course_dbms", "cw_1", "sub_1")
    assert submission["state"] in ["TURNED_IN", "CREATED"]

    announcements = await connector.list_announcements("course_dbms")
    assert len(announcements) >= 1

    result = await connector.submit_student_submission("course_dbms", "cw_dbms_1", "sub_1")
    assert result["success"] is True
    assert result["status"] == "TURNED_IN"

@pytest.mark.asyncio
async def test_google_drive_connector():
    drive = GoogleDriveConnector(access_token=None)
    files = await drive.search_files()
    assert len(files) >= 1

    meta = await drive.get_file_metadata("gdrive_f1")
    assert meta["id"] == "gdrive_f1"

    downloaded = await drive.download_file("gdrive_f1")
    assert len(downloaded) > 0

    folder = await drive.create_folder("DBMS_Labs")
    assert "DBMS_Labs" in folder["name"]

    upload = await drive.upload_document("test.pdf", b"test pdf content")
    assert upload["name"] == "test.pdf"

@pytest.mark.asyncio
async def test_google_calendar_connector():
    calendar = GoogleCalendarConnector(access_token=None)
    events = await calendar.get_academic_events()
    assert len(events) >= 1

    event = await calendar.create_academic_event(
        title="DBMS Project Demo",
        start_iso="2026-11-25T10:00:00Z",
        end_iso="2026-11-25T11:00:00Z",
        description="Demo with Dr. Sharma"
    )
    assert event["summary"] == "DBMS Project Demo"

@pytest.mark.asyncio
async def test_google_gmail_connector():
    gmail = GmailConnector(access_token=None)
    emails = await gmail.get_academic_emails()
    assert len(emails) >= 1
    assert any(e["is_assignment_related"] for e in emails)

    # Deadline extraction check
    deadline = gmail.extract_deadlines_from_email("The DBMS assignment is due on Nov 10, 2026.")
    assert deadline is not None

@pytest.mark.asyncio
async def test_microsoft_teams_connector():
    connector = MicrosoftTeamsConnector(access_token=None)
    classes = await connector.list_classes()
    assert len(classes) >= 1

    asgns = await connector.get_assignments("ms_class_algo")
    assert len(asgns) >= 1

    submission = await connector.get_assignment_submission("ms_class_algo", "asgn_1", "sub_1")
    assert "status" in submission

    announcements = await connector.list_announcements("team_algo")
    assert len(announcements) >= 1

    result = await connector.submit_assignment("class_algo", "asgn_algo_1", "sub_2")
    assert result["success"] is True
    assert result["status"] == "SUBMITTED"

@pytest.mark.asyncio
async def test_microsoft_onedrive_connector():
    onedrive = MicrosoftOneDriveConnector(access_token=None)
    od_files = await onedrive.list_files()
    assert len(od_files) >= 1

    search_res = await onedrive.search_files("Algorithms")
    assert len(search_res) >= 1

    content = await onedrive.download_file("onedrive_f1")
    assert len(content) > 0

    folder = await onedrive.create_folder("Algorithms_Coursework")
    assert folder["name"] == "Algorithms_Coursework"

@pytest.mark.asyncio
async def test_microsoft_calendar_connector():
    cal = MicrosoftCalendarConnector(access_token=None)
    events = await cal.get_calendar_events()
    assert len(events) >= 1

    event = await cal.create_calendar_event(
        subject="Algorithms Viva",
        start_iso="2026-11-26T14:00:00Z",
        end_iso="2026-11-26T15:00:00Z"
    )
    assert event["subject"] == "Algorithms Viva"

@pytest.mark.asyncio
async def test_microsoft_outlook_connector():
    outlook = MicrosoftOutlookConnector(access_token=None)
    emails = await outlook.get_academic_emails()
    assert len(emails) >= 1
    assert any("CS201" in e["subject"] for e in emails)
