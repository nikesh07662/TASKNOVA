import pytest
from app.models.user import User
from app.api.v1.integrations import get_integration_health

@pytest.mark.asyncio
async def test_integration_health_dashboard_unconnected_user():
    user = User(
        id="user_test_health_1",
        email="student@university.edu",
        full_name="Academic Student",
        hashed_password="hash",
        is_google_connected=False,
        is_microsoft_connected=False
    )
    response = await get_integration_health(user=user)
    assert response.total_integrations == 10
    assert response.active_connected_count >= 0
    assert len(response.items) == 10

    # Ensure admin consent notes are clearly flagged
    classroom = next(it for it in response.items if it.id == "google_classroom")
    assert classroom.requires_admin_consent is True
    assert "administrator" in classroom.admin_consent_note.lower()

    teams = next(it for it in response.items if it.id == "microsoft_teams")
    assert teams.requires_admin_consent is True
    assert "admin" in teams.admin_consent_note.lower()

@pytest.mark.asyncio
async def test_integration_health_dashboard_connected_user():
    user = User(
        id="user_test_health_2",
        email="student2@university.edu",
        full_name="Connected Student",
        hashed_password="hash",
        google_access_token="mock_gtoken",
        is_google_connected=True,
        microsoft_access_token="mock_mstoken",
        is_microsoft_connected=True
    )
    response = await get_integration_health(user=user)
    assert response.total_integrations == 10
    assert response.active_connected_count >= 8

    classroom = next(it for it in response.items if it.id == "google_classroom")
    assert classroom.status.value == "WORKING"
    assert classroom.is_connected is True

    drive = next(it for it in response.items if it.id == "google_drive")
    assert drive.status.value == "WORKING"

    teams = next(it for it in response.items if it.id == "microsoft_teams")
    assert teams.status.value == "WORKING"
