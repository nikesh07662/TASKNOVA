from enum import Enum
from typing import List, Optional
from app.schemas.base import CamelModel

class IntegrationStatus(str, Enum):
    CONNECTED = "CONNECTED"
    WORKING = "WORKING"
    AUTHENTICATION_EXPIRED = "AUTHENTICATION_EXPIRED"
    PERMISSION_MISSING = "PERMISSION_MISSING"
    CONFIGURATION_INCOMPLETE = "CONFIGURATION_INCOMPLETE"
    SYNCHRONIZATION_FAILED = "SYNCHRONIZATION_FAILED"
    NOT_YET_VERIFIED = "NOT_YET_VERIFIED"

class IntegrationHealthItem(CamelModel):
    id: str
    name: str
    category: str  # GOOGLE, MICROSOFT, AI
    status: IntegrationStatus
    status_display: str
    is_connected: bool
    requires_admin_consent: bool = False
    admin_consent_note: Optional[str] = None
    last_synced_at: Optional[str] = None
    account_email: Optional[str] = None
    scopes_granted: List[str] = []
    message: str

class IntegrationDashboardResponse(CamelModel):
    total_integrations: int
    active_connected_count: int
    needs_attention_count: int
    items: List[IntegrationHealthItem]
