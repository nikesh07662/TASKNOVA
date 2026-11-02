import pytest
from app.core.security import create_access_token, get_password_hash, verify_password
from jose import jwt
from app.core.config import settings

def test_password_hashing():
    raw_pass = "SecureAcademicPass123!"
    hashed = get_password_hash(raw_pass)
    assert hashed != raw_pass
    assert verify_password(raw_pass, hashed) is True
    assert verify_password("WrongPassword", hashed) is False

def test_jwt_token_generation_and_decode():
    user_id = "user_test_uuid_42"
    token = create_access_token(user_id)
    payload = jwt.decode(token, settings.SECRET_KEY, algorithms=[settings.ALGORITHM])
    assert payload["sub"] == user_id
    assert "exp" in payload
