from pydantic import EmailStr
from typing import Optional
from app.schemas.base import CamelModel

class UserBase(CamelModel):
    email: EmailStr
    full_name: str
    preferred_ai_model: str = "gpt-4o"
    attendance_target: float = 75.0

class UserCreate(CamelModel):
    email: EmailStr
    full_name: str
    password_hash: str

class UserLogin(CamelModel):
    email: EmailStr
    password_hash: str

class UserOut(CamelModel):
    id: str
    email: str
    full_name: str
    avatar_url: Optional[str] = None
    is_google_connected: bool = False
    is_microsoft_connected: bool = False
    preferred_ai_model: str = "gpt-4o"
    attendance_target: float = 75.0

class AuthResponse(CamelModel):
    access_token: str
    refresh_token: str
    user: UserOut

class OAuthExchangeRequest(CamelModel):
    provider: str
    auth_code: str
    redirect_uri: str
